package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

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
    
    private ComplexProperty accessPath;
    private String kindName;
    private Collection<Signature> primaryKey;
    
    private Mapping(SchemaCategory category, SchemaObject rootObject) {
        this.category = category;
        this.rootObject = rootObject;
    }

    public static Mapping fromArguments(SchemaCategory category, SchemaObject rootObject, ComplexProperty accessPath, String kindName, Collection<Signature> primaryKey) {
        var mapping = new Mapping(category, rootObject);
        mapping.accessPath = accessPath;
        mapping.kindName = kindName;
        mapping.primaryKey = primaryKey;
        return mapping;
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

    public String kindName() {
        return kindName;
    }

    public Collection<Signature> primaryKey() {
        return primaryKey;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        return other instanceof Mapping otherMapping && compareTo(otherMapping) == 0;
    }

    @Override
    public int compareTo(Mapping other) {
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

        private static ObjectReader rootPropertyJSONReader = new ObjectMapper().readerFor(ComplexProperty.class);
        private static ObjectReader signaturesJSONReader = new ObjectMapper().readerFor(Signature[].class);
    
        @Override
        public Mapping deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final var category = (SchemaCategory) context.getAttribute("category");
            final var rootObject = (SchemaObject) context.getAttribute("rootObject");
            final var mapping = new Mapping(category, rootObject);

            mapping.kindName = node.get("kindName").asText();
            mapping.primaryKey = List.of(signaturesJSONReader.readValue(node.get("primaryKey")));
            mapping.accessPath = rootPropertyJSONReader.readValue(node.get("accessPath"));
    
            return mapping;
        }

    }

}
