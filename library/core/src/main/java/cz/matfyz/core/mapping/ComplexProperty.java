package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A complex value in the access path tree. Its context is a signature of a morphism (or undefined in case of an auxiliary property)
 * It has subpaths and it provides many methods needed by the algorithms described in the paper.
 */
@JsonSerialize(using = ComplexProperty.Serializer.class)
@JsonDeserialize(using = ComplexProperty.Deserializer.class)
public class ComplexProperty extends AccessPath {

    public ComplexProperty(Name name, Signature signature, List<AccessPath> subpaths) {
        super(name, signature);

        this.subpathsMap = new TreeMap<>();
        subpaths.forEach(subpath -> this.subpathsMap.put(subpath.signature(), subpath));
        this.subpaths = new ArrayList<>(this.subpathsMap.values());
    }

    /** A very specific thing for the MTC algorithm. */
    public static ComplexProperty createEmpty() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createEmpty(), List.of());
    }

    /** The property is auxiliary if and only if its signature is empty. */
    public boolean isAuxiliary() {
        return signature.isEmpty();
    }

    public boolean hasDynamicKeys() {
        return this.subpaths.size() == 1 && this.subpaths.get(0).name instanceof DynamicName;
    }

    private final ArrayList<AccessPath> subpaths;
    private final Map<Signature, AccessPath> subpathsMap;

    public List<AccessPath> subpaths() {
        return subpaths;
    }

    public AccessPath getDirectSubpath(Signature signature) {
        return subpathsMap.get(signature);
    }

    public AccessPath getDirectSubpath(Name name) {
        final var optional = subpathsMap.values().stream().filter(subpath -> subpath.name.equals(name)).findAny();

        return optional.isPresent() ? optional.get() : null;
    }

    /**
     * Given a signature M, this function finds such a direct subpath S of this path that for each of the leaves L of S holds:
     *      - L.context == M, or
     *      - L.value == M, or
     *      - exists an ancestor A of L in S where A.context == M.
     * If there are more such subpaths (i.e. when some of them are auxiliary), the closest one is returned.
     * If M == null, a leaf L with L.value == epsion is returned.
     * If none of above exists, a null is returned.
     * @param signature
     * @return the closest subpath with given signature (or null if none such exists).
     */
    @Nullable
    public AccessPath getSubpathBySignature(Signature signature) {
        /*
        if (this.signature.equals(signature))
            return this;
        */
        // TODO - the signature can't be null
        // If M = null, a leaf L with L.value = epsion is returned.
        // if (signature == null) {
        //     final var directSubpath = getDirectSubpath(Signature.createEmpty());
        //     if (directSubpath instanceof SimpleProperty simpleProperty)
        //         return simpleProperty;

        //     for (AccessPath subpath : subpaths())
        //         if (subpath instanceof ComplexProperty complexProperty) {
        //             AccessPath result = complexProperty.getSubpathBySignature(null);
        //             if (result != null)
        //                 return result;
        //         }

        //     return null;
        // }

        // If this is an auxiliary property, we must find if all of the descendats of this property have M in their contexts or values.
        // If so, this is returned even if this context is null.
        // This doesn't make sense - each subpath has different signature
        /*
        if (isAuxiliary()) {
            boolean returnThis = true;
            for (AccessPath subpath : subpaths()) {
                if (!subpath.hasSignature(signature)
                    && subpath instanceof ComplexProperty complexProperty
                    && complexProperty.getSubpathBySignature(signature) != complexProperty
                ) {
                    returnThis = false;
                    break;
                }
            }

            if (returnThis)
                return this;
        }
         */

        final var directSubpath = getDirectSubpath(signature);
        if (directSubpath != null)
            return directSubpath;

        for (AccessPath subpath : subpaths())
            if (subpath instanceof ComplexProperty complexProperty) {
                AccessPath result = complexProperty.getSubpathBySignature(signature);
                if (result != null)
                    return result;
            }

        return null;
    }

    @Override protected boolean hasSignature(Signature signature) {
        return this.signature.equals(signature);
    }

    /**
     * Finds the path with the given signature and returns the properties along the way. This property itself isn't included.
     * If the signature isn't found, null is returned.
     */
    public @Nullable List<AccessPath> getPropertyPath(Signature signature) {
        var path = this.getPropertyPathInternal(signature);
        if (path == null)
            return null;

        Collections.reverse(path);
        path.remove(0);

        return path;
    }

    @Override protected @Nullable List<AccessPath> getPropertyPathInternal(Signature signature) {
        if (signature.isEmpty())
            return new ArrayList<>(List.of(this));

        for (var subpath : subpaths) {
            final var subSignature = signature.cutPrefix(subpath.signature);
            if (subSignature == null)
                continue;

            final var output = subpath.getPropertyPathInternal(subSignature);
            if (output == null)
                continue;

            output.add(this);
            return output;
        }

        return null;
    }

    @Override public AccessPath tryGetSubpathForObject(Key key, SchemaCategory schema) {
        final SchemaMorphism morphism = schema.getMorphism(signature);
        if (morphism.dom().key().equals(key))
            return this;

        for (final var subpath : subpaths) {
            final var subProperty = subpath.tryGetSubpathForObject(key, schema);
            if (subProperty != null)
                return subProperty;
        }

        return null;
    }

    /**
     * Creates a copy of this access path and links it to all its subpaths except the one given subpath.
     * @param subpath
     * @return a copy of this without subpath.
     */
    public ComplexProperty minusSubpath(AccessPath subpath) {
        assert subpaths.stream().anyMatch(path -> path.equals(subpath)) : "Subpath not found in accessPath in minusSubtree";

        final List<AccessPath> newSubpaths = subpaths.stream().filter(path -> !path.equals(subpath)).toList();

        return new ComplexProperty(name, signature, newSubpaths);
    }

    @Override public void printTo(Printer printer) {
        printer.append(name).append(": ");
        if (!isAuxiliary())
            printer.append(signature).append(" ");

        printer.append("{").down().nextLine();

        for (int i = 0; i < subpaths.size(); i++)
            printer.append(subpaths.get(i)).append(",").nextLine();

        printer.remove().up().nextLine()
            .append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    /**
     * Properties from given synthetic nodes are moved to their parent paths
     * @return
     */
    public ComplexProperty copyWithoutAuxiliaryNodes() {
        List<AccessPath> newSubpaths = this.getContentWithoutAuxiliaryNodes();
        return new ComplexProperty(name, signature, newSubpaths);
    }

    private List<AccessPath> getContentWithoutAuxiliaryNodes() {
        List<AccessPath> newSubpaths = new ArrayList<>();
        for (AccessPath path : subpaths()) {
            if (path instanceof SimpleProperty) {
                newSubpaths.add(path); // Not making a copy because the path is expected to be immutable.
            }
            else if (path instanceof ComplexProperty complexProperty) {
                if (complexProperty.isAuxiliary())
                    newSubpaths.addAll(complexProperty.getContentWithoutAuxiliaryNodes());
                else
                    newSubpaths.add(complexProperty.copyWithoutAuxiliaryNodes());
            }
        }

        return newSubpaths;
    }

    public List<String> getSubpathNames() {
        return subpaths.stream()
                    .map(subpath -> subpath.name.toString())
                    .collect(Collectors.toList());
    }

    public static class Serializer extends StdSerializer<ComplexProperty> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ComplexProperty> t) {
            super(t);
        }

        @Override public void serialize(ComplexProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);

            generator.writeArrayFieldStart("subpaths");
            for (final var subpath : property.subpaths)
                generator.writePOJO(subpath);
            generator.writeEndArray();

            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<ComplexProperty> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader nameJsonReader = new ObjectMapper().readerFor(Name.class);
        private static final ObjectReader signatureJsonReader = new ObjectMapper().readerFor(Signature.class);
        private static final ObjectReader subpathsJsonReader = new ObjectMapper().readerFor(AccessPath[].class);

        @Override public ComplexProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Name name = nameJsonReader.readValue(node.get("name"));
            final Signature signature = signatureJsonReader.readValue(node.get("signature"));
            final AccessPath[] subpaths = subpathsJsonReader.readValue(node.get("subpaths"));

            return new ComplexProperty(name, signature, List.of(subpaths));
        }

    }

}
