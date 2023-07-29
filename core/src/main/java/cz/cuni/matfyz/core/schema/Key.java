package cz.cuni.matfyz.core.schema;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * This class represents a 'key' of an object as is described in the paper. It's basically just a number with extra steps.
 * @author pavel.koupil, jachym.bartik
 */
@JsonSerialize(using = Key.Serializer.class)
public class Key implements Serializable, Comparable<Key> {

    private final int value;
    
    public int getValue() {
        return value;
    }

    @JsonCreator
    public Key(@JsonProperty("value") int value) {
        this.value = value;
    }

    @Override
    public int compareTo(Key key) {
        return value - key.value;
    }
    
    @Override
    public String toString() {
        return value + "";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key key && compareTo(key) == 0;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return
     */
    @Override
    public int hashCode() {
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

        @Override
        public void serialize(Key key, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("value", key.value);
            generator.writeEndObject();
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
