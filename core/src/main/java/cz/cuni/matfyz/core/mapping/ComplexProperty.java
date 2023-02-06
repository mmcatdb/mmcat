package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.IndentedStringBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

/**
 * A complex value in the access path tree. Its context is a signature of a morphism (or undefined in case of an auxiliary property)
 * It has subpaths and it provides many methods needed in the algorithms described in the paper.
 * @author jachymb.bartik
 */
@JsonSerialize(using = ComplexProperty.Serializer.class)
@JsonDeserialize(using = ComplexProperty.Deserializer.class)
public class ComplexProperty extends AccessPath {

    private final boolean isAuxiliary;
    
    public boolean isAuxiliary() {
        return isAuxiliary;
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
    
    public ComplexProperty(Name name, Signature signature, boolean isAuxiliary, List<AccessPath> subpaths) {
        super(name, signature);
        
        this.isAuxiliary = isAuxiliary;
        this.subpathsMap = new TreeMap<>();
        subpaths.forEach(subpath -> this.subpathsMap.put(subpath.signature(), subpath));
        this.subpaths = new ArrayList<>(this.subpathsMap.values());
    }
    /*
    public ComplexProperty(Name name, Signature signature, boolean isAuxiliary, AccessPath... subpaths) {
        this(name, signature, isAuxiliary, Arrays.asList(subpaths));
    }
    
    public ComplexProperty(String name, Signature signature, boolean isAuxiliary, AccessPath... subpaths) {
        this(new StaticName(name), signature, isAuxiliary, Arrays.asList(subpaths));
    }
    
    public ComplexProperty(Signature name, Signature signature, boolean isAuxiliary, AccessPath... subpaths) {
        this(new DynamicName(name), signature, isAuxiliary, Arrays.asList(subpaths));
    }
     */

    private static ComplexProperty create(Name name, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(name, signature, false, Arrays.asList(subpaths));
    }

    public static ComplexProperty create(String name, Signature signature, AccessPath... subpaths) {
        return create(new StaticName(name), signature, subpaths);
    }

    public static ComplexProperty create(Signature name, Signature signature, AccessPath... subpaths) {
        return create(new DynamicName(name), signature, subpaths);
    }
    
    public static ComplexProperty createEmpty() {
        return new ComplexProperty(null, Signature.createEmpty(), true, Collections.<AccessPath>emptyList());
    }

    public static ComplexProperty createAuxiliary(Name name, AccessPath... subpaths) {
        return new ComplexProperty(name, Signature.createEmpty(), true, Arrays.asList(subpaths));
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
    public AccessPath getSubpathBySignature(Signature signature) {
        /*
        if (this.signature.equals(signature))
            return this;
        */
        // If M = null, a leaf L with L.value = epsion is returned.
        if (signature == null) {
            final var directSubpath = getDirectSubpath(Signature.createEmpty());
            if (directSubpath instanceof SimpleProperty simpleProperty)
                return simpleProperty;
            
            for (AccessPath subpath : subpaths())
                if (subpath instanceof ComplexProperty complexProperty) {
                    AccessPath result = complexProperty.getSubpathBySignature(null);
                    if (result != null)
                        return result;
                }
    
            return null;
        }
        
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
    
    @Override
    protected boolean hasSignature(Signature signature) {
        if (signature == null)
            return false;
        
        return signature.equals(this.signature);
    }
    
    /**
     * Creates a copy of this access path and links it to all its subpaths except the one given subpath.
     * @param subpath
     * @return a copy of this without subpath.
     */
    public ComplexProperty minusSubpath(AccessPath subpath) {
        assert subpaths.stream().anyMatch(path -> path.equals(subpath)) : "Subpath not found in accessPath in minusSubtree";
        
        final List<AccessPath> newSubpaths = subpaths.stream().filter(path -> path.equals(subpath)).toList();
        
        return new ComplexProperty(name, signature, isAuxiliary, newSubpaths);
    }
    
    @Override
    public String toString() {
        final var subpathBuilder = new IndentedStringBuilder(1);
        
        if (!subpaths.isEmpty())
            subpathBuilder.append(subpaths.get(0));
        for (int i = 1; i < subpaths.size(); i++)
            subpathBuilder.append(",\n").append(subpaths.get(i));
        subpathBuilder.append("\n");
        
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ");
        if (!isAuxiliary())
            builder.append(signature).append(" ");
        
        builder.append("{\n").append(subpathBuilder).append("}");
        
        return builder.toString();
    }
    
    /**
     * Properties from given synthetic nodes are moved to their parent paths
     * @return
     */
    public ComplexProperty copyWithoutAuxiliaryNodes() {
        List<AccessPath> newSubpaths = this.getContentWithoutAuxiliaryNodes();
        return new ComplexProperty(name, signature, isAuxiliary, newSubpaths);
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

    public static class Serializer extends StdSerializer<ComplexProperty> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ComplexProperty> t) {
            super(t);
        }

        @Override
        public void serialize(ComplexProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);
            generator.writeBooleanField("isAuxiliary", property.isAuxiliary);

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

        private static final ObjectReader nameJSONReader = new ObjectMapper().readerFor(Name.class);
        private static final ObjectReader signatureJSONReader = new ObjectMapper().readerFor(Signature.class);
        private static final ObjectReader subpathsJSONReader = new ObjectMapper().readerFor(AccessPath[].class);
    
        @Override
        public ComplexProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Name name = nameJSONReader.readValue(node.get("name"));
            final Signature signature = signatureJSONReader.readValue(node.get("signature"));
            final var isAuxiliary = node.get("isAuxiliary").asBoolean();
            final AccessPath[] subpaths = subpathsJSONReader.readValue(node.get("subpaths"));

            return new ComplexProperty(name, signature, isAuxiliary, List.of(subpaths));
        }

    }

}
