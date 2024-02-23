package cz.matfyz.core.mapping;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author pavel.koupil, jachym.bartik
 */
@JsonDeserialize(using = Mapping.Deserializer.class)
public class Mapping implements Comparable<Mapping> {

    private final SchemaCategory category;
    private final SchemaObject rootObject;

    private String kindName;
    private ComplexProperty accessPath;
    private Collection<Signature> primaryKey;

    public Mapping(SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        this.category = category;
        this.rootObject = category.getObject(rootKey);
        this.kindName = kindName;
        this.accessPath = accessPath;
        this.primaryKey = primaryKey;
    }

    public static Mapping create(SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath) {
        final var rootObject = category.getObject(rootKey);

        return new Mapping(category, rootKey, kindName, accessPath, defaultPrimaryKey(rootObject));
    }

    public static List<Signature> defaultPrimaryKey(SchemaObject object) {
        return object.ids().isSignatures()
            ? object.ids().toSignatureIds().first().signatures().stream().toList()
            : List.of(Signature.createEmpty());
    }

    public Mapping clone() {
        throw new UnsupportedOperationException("Mapping.clone not implemented");
    }

    public SchemaCategory category() {
        return category;
    }

    public SchemaObject rootObject() {
        return rootObject;
    }

    public ComplexProperty accessPath() {
        return accessPath;
    }

    /**
     * Find the path to the given signature in and return the properties along the way (without the root property).
     * If the signature isn't found, null is returned.
     */
    public List<AccessPath> getPropertyPath(Signature signature) {
        final var fullPath = this.accessPath.getPropertyPath(signature);
        if (fullPath == null)
            return null;

        fullPath.remove(0);

        return fullPath;
    }

    public String kindName() {
        return kindName;
    }

    public Collection<Signature> primaryKey() {
        return primaryKey;
    }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;

        return other instanceof Mapping otherMapping && compareTo(otherMapping) == 0;
    }

    @Override public int compareTo(Mapping other) {
        // This guarantees uniqueness in one logical model, however mappings between different logical models are never compared.
        return kindName.compareTo(other.kindName);
    }

    /*
    private final List<Reference> references = new ArrayList<Reference>();

    public List<Reference> references() {
        return references;
    }

    public void setReferences(Iterable<Reference> references) {
        this.references.clear();
        references.forEach(this.references::add);
    }
    */

    public static class Deserializer extends StdDeserializer<Mapping> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader keyJsonReader = new ObjectMapper().readerFor(Key.class);
        private static final ObjectReader rootPropertyJsonReader = new ObjectMapper().readerFor(ComplexProperty.class);
        private static final ObjectReader signaturesJsonReader = new ObjectMapper().readerFor(Signature[].class);

        @Override public Mapping deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final var category = (SchemaCategory) context.getAttribute("category");
            final Key rootObjectKey = keyJsonReader.readValue(node.get("rootObjectKey"));

            final var kindName = node.get("kindName").asText();
            final List<Signature> primaryKey = List.of(signaturesJsonReader.readValue(node.get("primaryKey")));
            final ComplexProperty accessPath = rootPropertyJsonReader.readValue(node.get("accessPath"));

            return new Mapping(
                category,
                rootObjectKey,
                kindName,
                accessPath,
                primaryKey
            );
        }

    }

}
