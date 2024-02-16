package cz.matfyz.core.mapping;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.record.DynamicRecordName;

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
 * @author jachym.bartik
 */
@JsonSerialize(using = DynamicName.Serializer.class)
@JsonDeserialize(using = DynamicName.Deserializer.class)
public class DynamicName extends Name {
    
    private final Signature signature;

    public Signature signature() {
        return signature;
    }
    
    public DynamicName(Signature signature) {
        this.signature = signature;
    }
    
    public DynamicRecordName toRecordName(String dynamicNameValue) {
        return new DynamicRecordName(dynamicNameValue, signature);
    }
    
    @Override public String toString() {
        return signature.toString();
    }

    @Override public boolean equals(Object object) {
        return object instanceof DynamicName dynamicName && signature.equals(dynamicName.signature);
    }

    public static class Serializer extends StdSerializer<DynamicName> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<DynamicName> t) {
            super(t);
        }

        @Override public void serialize(DynamicName name, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("signature", name.signature);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<DynamicName> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }
    
        private static final ObjectReader signatureJsonReader = new ObjectMapper().readerFor(Signature.class);
    
        @Override public DynamicName deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Signature signature = signatureJsonReader.readValue(node.get("signature"));

            return new DynamicName(signature);
        }

    }

}
