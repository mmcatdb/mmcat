package cz.matfyz.core.identifiers;

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
 * This class represents a 'key' of an object as is described in the paper. It's basically just a number with extra steps.
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
     * @return
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

            return new Key(node.asInt());
        }

    }


    public static class Generator {

        private int max;

        public Generator(Collection<Key> current) {
            max = current.stream().map(key -> key.value).reduce(0, Math::max);
        }

        public Key next() {
            max++;
            return new Key(max);
        }

    }

}
