package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.serialization.Identified;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

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
 * @author pavel.koupil, jachymb.bartik
 */
@JsonSerialize(using = SchemaObject.Serializer.class)
@JsonDeserialize(using = SchemaObject.Deserializer.class)
public class SchemaObject implements Serializable, CategoricalObject, Identified<Key> {
    //private static final Logger LOGGER = LoggerFactory.getLogger(SchemaObject.class);
    
    private final Key key; // Identifies the object, in the paper it's a number >= 100
    private final String label;
    private final SignatureId superId; // Should be a union of all ids (super key).
    private final ObjectIds ids; // Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).

    public SchemaObject(Key key, String label, SignatureId superId, ObjectIds ids) {
        this(key, label, superId, ids, "", "");
    }

    public final String iri;
    public final String pimIri;

    public SchemaObject(Key key, String label, SignatureId superId, ObjectIds ids, String iri, String pimIri) {
        this.key = key;
        this.label = label;
        this.superId = superId;
        this.ids = ids;
        this.iri = iri;
        this.pimIri = pimIri;
    }

    @Override
    public Key identifier() {
        return key;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public SignatureId superId() {
        return superId;
    }

    /**
     * Immutable.
     */
    @Override
    public ObjectIds ids() {
        return ids;
    }

    @Override
    public int compareTo(CategoricalObject categoricalObject) {
        return key.compareTo(categoricalObject.key());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SchemaObject schemaObject && key.equals(schemaObject.key);
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public String toString() {
        return "SchemaObject TODO";
    }
    
    public static class Serializer extends StdSerializer<SchemaObject> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SchemaObject> t) {
            super(t);
        }

        @Override
        public void serialize(SchemaObject object, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("key", object.key);
            generator.writeStringField("label", object.label);
            generator.writePOJOField("superId", object.superId);
            generator.writePOJOField("ids", object.ids);
            generator.writeStringField("iri", object.iri);
            generator.writeStringField("pimIri", object.pimIri);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<SchemaObject> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static ObjectReader keyJSONReader = new ObjectMapper().readerFor(Key.class);
        private static ObjectReader superIdJSONReader = new ObjectMapper().readerFor(SignatureId.class);
        private static ObjectReader idsJSONReader = new ObjectMapper().readerFor(ObjectIds.class);
    
        @Override
        public SchemaObject deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Key key = keyJSONReader.readValue(node.get("key"));
            final var label = node.get("label").asText();
            final SignatureId superId = superIdJSONReader.readValue(node.get("superId"));
            final ObjectIds ids = idsJSONReader.readValue(node.get("ids"));
            final var iri = node.hasNonNull("iri") ? node.get("iri").asText() : "";
            final var pimIri = node.hasNonNull("pimIri") ? node.get("pimIri").asText() : "";

            return new SchemaObject(key, label, superId, ids, iri, pimIri);
        }

    }

}
