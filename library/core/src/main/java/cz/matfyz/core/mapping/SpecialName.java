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
 * A name with a special meaning in the given datasource. E.g., the root of the complex property tree, or nodes in a Neo4j relationship.
 */
@JsonSerialize(using = SpecialName.Serializer.class)
@JsonDeserialize(using = SpecialName.Deserializer.class)
public class SpecialName extends Name {

    private final String type;

    public SpecialName(String type) {
        super();
        this.type = type;
    }

    @Override public String toString() {
        return "<" + type + ">";
    }

    @Override public boolean equals(Object object) {
        return object instanceof SpecialName name && type.equals(name.type);
    }

    /** @deprecated Maybe */
    public static class Serializer extends StdSerializer<SpecialName> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SpecialName> t) {
            super(t);
        }

        @Override public void serialize(SpecialName name, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("type", name.type);
            generator.writeEndObject();
        }

    }

    /** @deprecated */
    public static class Deserializer extends StdDeserializer<SpecialName> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public SpecialName deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final String type = node.get("type").asText();

            return new SpecialName(type);
        }
    }

}
