package cz.matfyz.core.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A normal string name.
 */
@JsonSerialize(using = StaticName.Serializer.class)
@JsonDeserialize(using = StaticName.Deserializer.class)
public class StaticName extends Name {

    private final String value;

    public StaticName(String value) {
        this.value = value;
    }

    public String getStringName() {
        return value;
    }

    @Override public String toString() {
        return value;
    }

    @Override public boolean equals(Object object) {
        return object instanceof StaticName name && value.equals(name.value);
    }

    /** @deprecated Maybe */
    public static class Serializer extends StdSerializer<StaticName> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<StaticName> t) {
            super(t);
        }

        @Override public void serialize(StaticName name, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("value", name.value);
            generator.writeEndObject();
        }

    }

    /** @deprecated */
    public static class Deserializer extends StdDeserializer<StaticName> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public StaticName deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final String value = node.get("value").asText();

            return new StaticName(value);
        }
    }

}
