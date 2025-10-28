package cz.matfyz.core.mapping;

import cz.matfyz.core.exception.AccessPathException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
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

    /** @deprecated build only */
    public ComplexProperty(Name name, Signature signature, Iterable<AccessPath> subpaths) {
        super(name, signature, List.of());

        this.subpaths = new TreeMap<>();
        subpaths.forEach(subpath -> this.subpaths.put(subpath.signature(), subpath));
    }

    ComplexProperty(Name name, Signature signature, List<@Nullable Signature> indexSignatures, Iterable<AccessPath> subpaths) {
        super(name, signature, indexSignatures);

        this.subpaths = new TreeMap<>();
        subpaths.forEach(subpath -> this.subpaths.put(subpath.signature(), subpath));
    }

    /** The property is auxiliary if and only if its signature is empty. */
    public boolean isAuxiliary() {
        return signature.isEmpty();
    }

    private final Map<Signature, AccessPath> subpaths;

    public Collection<AccessPath> subpaths() {
        return subpaths.values();
    }

    public AccessPath getDirectSubpath(Signature signature) {
        return subpaths.get(signature);
    }

    public @Nullable AccessPath getDirectSubpath(Name name) {
        final var optional = subpaths().stream().filter(subpath -> subpath.name.equals(name)).findAny();

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
    public @Nullable AccessPath getSubpathBySignature(Signature signature) {
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

        for (final var subpath : subpaths()) {
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

    @Override public @Nullable AccessPath tryGetSubpathForObjex(Key key, SchemaCategory schema) {
        if (signature instanceof BaseSignature base) {
            final SchemaMorphism morphism = schema.getMorphism(base);
            if (morphism.dom().key().equals(key))
                return this;
        }

        for (final var subpath : subpaths()) {
            final var subProperty = subpath.tryGetSubpathForObjex(key, schema);
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
        assert subpaths().stream().anyMatch(path -> path.equals(subpath)) : "Subpath not found in accessPath in minusSubtree";

        final List<AccessPath> newSubpaths = subpaths().stream().filter(path -> !path.equals(subpath)).toList();

        return new ComplexProperty(name, signature, indexSignatures, newSubpaths);
    }

    /**
     * Properties from auxiliary nodes are moved to their parents' paths.
     */
    public ComplexProperty copyWithoutAuxiliaryNodes() {
        List<AccessPath> newSubpaths = this.getContentWithoutAuxiliaryNodes();
        return new ComplexProperty(name, signature, indexSignatures, newSubpaths);
    }

    private List<AccessPath> getContentWithoutAuxiliaryNodes() {
        List<AccessPath> newSubpaths = new ArrayList<>();
        for (AccessPath path : subpaths()) {
            if (path instanceof SimpleProperty) {
                newSubpaths.add(path); // Not making a copy because the path is expected to be immutable.
                continue;
            }

            final var complexProperty = (ComplexProperty) path;
            if (complexProperty.isAuxiliary())
                newSubpaths.addAll(complexProperty.getContentWithoutAuxiliaryNodes());
            else
                newSubpaths.add(complexProperty.copyWithoutAuxiliaryNodes());
        }

        return newSubpaths;
    }


    public record ReplacementResult(
        ComplexProperty path,
        Map<DynamicName, DynamicNameReplacement> replacedNames
    ) {}

    public record DynamicNameReplacement(
        /** The longest common prefix of both name and value signatures. The actual map entry sits between the prefix and the name/value paths. */
        Signature prefix,
        /** The dynamic name's signature without the common prefix. */
        Signature name,
        /** The replaced property's original value. It still contains the original dynamic names. Only its name and signature were changed. */
        AccessPath value,
        /** The shortest path from the value to the name. It's here mostly for convenience. */
        Signature valueToName
    ) {}

    /**
     * Properties with dynamic names are replaced with new complex properties with name and value.
     * We also track all replacements so that we can use them to map the old path to the new one.
     */
    public ReplacementResult copyWithoutDynamicNames() {
        final var replacedNames = new TreeMap<DynamicName, DynamicNameReplacement>();
        final var path = copyForReplacement(name, signature, replacedNames);

        return new ReplacementResult(path, replacedNames);
    }

    @Override protected ComplexProperty copyForReplacement(Name name, Signature signature, @Nullable Map<DynamicName, DynamicNameReplacement> replacedNames) {
        final var newSubpaths = replacedNames == null
            ? subpaths()
            : subpaths.values().stream().map(subpath -> replaceDynamicName(subpath, replacedNames)).toList();

        // TODO check if indexSignatures needs to be updated as well
        return new ComplexProperty(name, signature, indexSignatures, newSubpaths);
    }

    /**
     * If this property has a dynamic name, the whole property is replaced by a complex property with a name and a value. The value holds the copy of this property.
     */
    private static AccessPath replaceDynamicName(AccessPath path, Map<DynamicName, DynamicNameReplacement> replacedNames) {
        if (!(path.name instanceof final DynamicName dynamicName))
            return path.copyForReplacement(path.name, path.signature, replacedNames);

        final Signature prefix = path.signature.longestCommonPrefix(dynamicName.signature);

        if (prefix.isEmpty())
            // This is illegal and punishable by death.
            throw AccessPathException.dynamicNameMissingCommonPrefix(dynamicName.signature, path.signature);

        final var valueSignature = path.signature.cutPrefix(prefix);
        final var partiallyReplacedValue = path.copyForReplacement(new StringName("_VALUE"), valueSignature, null);
        final var fullyReplacedValue = path.copyForReplacement(new StringName("_VALUE"), valueSignature, replacedNames);

        final var nameSignature = dynamicName.signature.cutPrefix(prefix);
        final var valueToName = valueSignature.dual().concatenate(nameSignature);
        replacedNames.put(dynamicName, new DynamicNameReplacement(prefix, nameSignature, partiallyReplacedValue, valueToName));

        // TODO check if indexSignatures needs to be updated as well
        return new ComplexProperty(new StringName("_DYNAMIC"), prefix, path.indexSignatures, List.of(
            new SimpleProperty(new StringName("_NAME"), nameSignature, List.of()),
            fullyReplacedValue
        ));
    }

    /**
     * Finds a direct subpath with the given name.
     * First, we search between the statically named ones. If none is found, we try to match the dynamically named ones using their patterns. Lastly, we take the dynamically named one without a pattern. If there isn't any, null is returned.
     */
    public @Nullable AccessPath findSubpathByName(String name) {
        if (nameMatcherCache == null)
            nameMatcherCache = new NameMatcher(subpaths());

        return nameMatcherCache.match(name);
    }

    private @Nullable NameMatcher nameMatcherCache;

    private static class NameMatcher {
        private final Map<String, AccessPath> staticSubpaths = new TreeMap<>();
        private final List<Pattern> dynamicPatterns = new ArrayList<>();
        private final List<AccessPath> dynamicSubpaths = new ArrayList<>();
        private final @Nullable AccessPath dynamicWithoutPattern;

        public NameMatcher(Collection<AccessPath> subpaths) {
            @Nullable AccessPath dynamicWithoutPattern = null;

            for (final var subpath : subpaths) {
                if (subpath.name instanceof final StringName stringName) {
                    staticSubpaths.put(stringName.value, subpath);
                    continue;
                }

                final var dynamicName = (DynamicName) subpath.name;
                final var pattern = dynamicName.compiledPattern();
                if (pattern == null) {
                    if (dynamicWithoutPattern != null)
                        throw new IllegalArgumentException("Only one dynamic subpath without pattern is allowed in a complex property.");

                    dynamicWithoutPattern = subpath;
                    continue;
                }

                dynamicPatterns.add(pattern);
                dynamicSubpaths.add(subpath);
            }

            this.dynamicWithoutPattern = dynamicWithoutPattern;
        }

        public @Nullable AccessPath match(String name) {
            final var staticMatch = staticSubpaths.get(name);
            if (staticMatch != null)
                return staticMatch;

            for (int i = 0; i < dynamicPatterns.size(); i++) {
                if (dynamicPatterns.get(i).matcher(name).matches())
                    return dynamicSubpaths.get(i);
            }

            return dynamicWithoutPattern;
        }
    }

    @Override public void printTo(Printer printer) {
        printer.append(name).append(": ");
        if (!isAuxiliary())
            printer.append(signature).append(" ");

        printer.append("{").down().nextLine();

        for (final var subpath : sortedSubpaths())
            printer.append(subpath).append(",").nextLine();

        printer.remove().up().nextLine()
            .append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    private List<AccessPath> sortedSubpaths() {
        final var list = new ArrayList<>(subpaths.values());
        Collections.sort(list, (a, b) -> Name.compareNamesLexicographically(a.name, b.name));
        return list;
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<ComplexProperty> {
        public Serializer() { this(null); }
        public Serializer(Class<ComplexProperty> t) { super(t); }

        @Override public void serialize(ComplexProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);

            generator.writeArrayFieldStart("indexSignatures");
            for (final var signature : property.indexSignatures())
                generator.writePOJO(signature);
            generator.writeEndArray();

            generator.writeArrayFieldStart("subpaths");
            for (final var subpath : property.subpaths())
                generator.writePOJO(subpath);
            generator.writeEndArray();

            generator.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<ComplexProperty> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public ComplexProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);

            final Name name = codec.treeToValue(node.get("name"), Name.class);
            final Signature signature = codec.treeToValue(node.get("signature"), Signature.class);
            final Signature[] indexSignatures = codec.treeToValue(node.get("indexSignatures"), Signature[].class);
            final AccessPath[] subpaths = codec.treeToValue(node.get("subpaths"), AccessPath[].class);

            return new ComplexProperty(name, signature, List.of(indexSignatures), List.of(subpaths));
        }
    }

    // #endregion

}
