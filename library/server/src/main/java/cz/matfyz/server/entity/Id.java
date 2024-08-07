package cz.matfyz.server.entity;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JsonSerialize(using = Id.Serializer.class)
@JsonDeserialize(using = Id.Deserializer.class)
public class Id implements java.io.Serializable, java.lang.Comparable<Id>, java.lang.CharSequence {

    private final @NonNull String value;

    public Id(@NonNull String value) {
        this.value = value;
    }

    public static Id createNewUUID() {
        return new Id(UUID.randomUUID().toString());
    }

    @Override public String toString() {
        return value;
    }

    @Override public char charAt(int index) {
        return value.charAt(index);
    }

    @Override public int length() {
        return value.length();
    }

    @Override public CharSequence subSequence(int beginIndex, int endIndex) {
        return value.subSequence(beginIndex, endIndex);
    }

    @Override public boolean isEmpty() {
        return value.isEmpty();
    }

    public @Nullable Id toNullable() {
        return value.isEmpty() ? null : this;
    }

    private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public boolean isUuid() {
        return UUID_REGEX.matcher(value).matches();
    }

    public int compareTo(Id another) {
        return value.compareTo(another.value);
    }

    @Override public boolean equals(@Nullable Object object) {
        return object instanceof Id another && another != null && value.equals(another.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }

    public static class Serializer extends StdSerializer<Id> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Id> t) {
            super(t);
        }

        @Override public void serialize(Id id, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(id.value);
        }

    }

    public static class Deserializer extends StdDeserializer<Id> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public Id deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return new Id(node.asText());
        }

    }

}
