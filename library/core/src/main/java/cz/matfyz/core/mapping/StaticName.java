package cz.matfyz.core.mapping;

import cz.matfyz.core.record.StaticRecordName;

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

@JsonSerialize(using = StaticName.Serializer.class)
@JsonDeserialize(using = StaticName.Deserializer.class)
public class StaticName extends Name {
    private final String value;
    private final Type type;

    public StaticName(String name) {
        super();
        this.value = name;
        this.type = Type.STATIC;
        this.staticRecordNameFlyweight = new StaticRecordName(value, type);
    }

    // Anonymous name
    private StaticName() {
        super();
        this.value = "";
        this.type = Type.ANONYMOUS;
        this.staticRecordNameFlyweight = new StaticRecordName(value, type);
    }

    private static final StaticName anonymous = new StaticName();

    public static StaticName createAnonymous() {
        return anonymous;
    }

    public enum Type {
        STATIC,
        ANONYMOUS, // Also known as Empty
    }

    private final StaticRecordName staticRecordNameFlyweight;

    public StaticRecordName toRecordName() {
        return staticRecordNameFlyweight;
    }

    public String getStringName() {
        return switch (type) {
            case STATIC -> value;
            case ANONYMOUS -> "";
        };
    }

    @Override public String toString() {
        return switch (type) {
            case STATIC -> value;
            case ANONYMOUS -> "_";
        };
    }

    @Override public boolean equals(Object object) {
        return object instanceof StaticName staticName
            && type == staticName.type
            && value.equals(staticName.value);
    }

    public static class Serializer extends StdSerializer<StaticName> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<StaticName> t) {
            super(t);
        }

        @Override public void serialize(StaticName name, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("type", name.type.name());
            generator.writeStringField("value", name.value);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<StaticName> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public StaticName deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Type type = Type.valueOf(node.get("type").asText());
            final String value = node.get("value").asText();

            return type == Type.ANONYMOUS ? StaticName.createAnonymous() : new StaticName(value);
        }

    }

}
