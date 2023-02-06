package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

import java.io.IOException;

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
 * A simple value node in the access path tree. Its context is undefined (null).
 * @author jachymb.bartik
 */
@JsonSerialize(using = SimpleProperty.Serializer.class)
@JsonDeserialize(using = SimpleProperty.Deserializer.class)
public class SimpleProperty extends AccessPath {

    public SimpleProperty(Name name, Signature signature) {
        super(name, signature);
    }
    
    public SimpleProperty(String name, Signature signature) {
        this(new StaticName(name), signature);
    }
    
    public SimpleProperty(Signature name, Signature signature) {
        this(new DynamicName(name), signature);
    }
    
    @Override
    protected boolean hasSignature(Signature signature) {
        if (signature == null)
            return this.signature.isEmpty();
        
        return this.signature.equals(signature);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ").append(signature);
        
        return builder.toString();
    }
    
    @Override
    public Signature signature() {
        return signature;
    }

    public static class Serializer extends StdSerializer<SimpleProperty> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SimpleProperty> t) {
            super(t);
        }

        @Override
        public void serialize(SimpleProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<SimpleProperty> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader nameJSONReader = new ObjectMapper().readerFor(Name.class);
        private static final ObjectReader signatureJSONReader = new ObjectMapper().readerFor(Signature.class);
    
        @Override
        public SimpleProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Name name = nameJSONReader.readValue(node.get("name"));
            final Signature signature = signatureJSONReader.readValue(node.get("signature"));

            return new SimpleProperty(name, signature);
        }

    }

}
