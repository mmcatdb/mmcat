package cz.matfyz.core.identifiers;

import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

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
 * This class represents a 'key' of a {@link cz.matfyz.core.schema.SchemaObjex} as is described in the paper. It's basically just a number with extra steps.
 */
@JsonSerialize(using = Key.Serializer.class)
@JsonDeserialize(using = Key.Deserializer.class)
public class Key implements Serializable, Comparable<Key> {

    private final int value;

    public int getValue() {
        return value;
    }

    public Key(int value) {
        this.value = value;
    }

    @Override public int compareTo(Key key) {
        return value - key.value;
    }

    @Override public String toString() {
        return value + "";
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Key key && compareTo(key) == 0;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     */
    @Override public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.value;
        return hash;
    }

    public static class Serializer extends StdSerializer<Key> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Key> t) {
            super(t);
        }

        @Override public void serialize(Key key, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeNumber(key.value);
        }

    }

    public static class Deserializer extends StdDeserializer<Key> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final int value = node.has("value") ? node.get("value").asInt() : node.asInt();
            return new Key(value);
        }

    }

    public static class KeyGenerator {

        private final UniqueSequentialGenerator idGenerator;

        private KeyGenerator(UniqueSequentialGenerator idGenerator) {
            this.idGenerator = idGenerator;
        }

        public static KeyGenerator create() {
            return new KeyGenerator(UniqueSequentialGenerator.create());
        }

        public static KeyGenerator create(Collection<Key> current) {
            final var currentIds = current.stream().map(key -> key.value).toList();
            return new KeyGenerator(UniqueSequentialGenerator.create(currentIds));
        }

        public Key next() {
            return new Key(idGenerator.next());
        }

    }

}
