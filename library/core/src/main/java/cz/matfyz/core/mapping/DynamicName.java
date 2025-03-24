package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.io.IOException;
import java.util.regex.Pattern;

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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Name that is mapped to a key (in an object) or to an index (in an array).
 */
@JsonSerialize(using = DynamicName.Serializer.class)
@JsonDeserialize(using = DynamicName.Deserializer.class)
public class DynamicName extends Name implements Comparable<DynamicName> {

    private final Signature signature;

    public Signature signature() {
        return signature;
    }

    public DynamicName(Signature signature, @Nullable String pattern) {
        this.signature = signature;
        this.pattern = pattern;
    }

    /**
     * If provided, only the matching names will be considered when retrieving records.
     * There can only be only one subpath with dynamic name with null pattern in any complex property.
     * The * character represents a wildcard. Other allowed characters are "a-zA-Z0-9._-".
     * If a value can be matched by multiple patterns, the first one will be used.
     */
    private final @Nullable String pattern;

    public @Nullable Pattern compilePattern() {
        if (pattern == null)
            return null;

        if (!patternValidator.matcher(pattern).matches())
            throw new IllegalArgumentException("Invalid pattern: " + pattern);

        // Replace multiple * with a single *.
        final var normalized = pattern.replaceAll("\\*{2,}", pattern);
        if (normalized.equals("*"))
            return null;

        final var regex = "^" + normalized.replace("*", ".*") + "$";
        return Pattern.compile(regex);
    }

    private static final Pattern patternValidator = Pattern.compile("^[a-zA-Z0-9._\\-*]+$");

    @Override public String toString() {
        return "<" + signature.toString() + ">";
    }

    @Override public boolean equals(Object object) {
        // Just signature is enough since there can't be two dynamic names with the same signature.
        return object instanceof DynamicName dynamicName && signature.equals(dynamicName.signature);
    }

    // We don't need anything special here. All dynamic names should be unique.
    private static final UniqueSequentialGenerator comparableGenerator = UniqueSequentialGenerator.create();
    private final int comparable = comparableGenerator.next();

    @Override public int compareTo(DynamicName dynamicName) {
        return comparable - dynamicName.comparable;
    }

    /** @deprecated Maybe */
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

    /** @deprecated */
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
            final @Nullable String pattern = node.hasNonNull("pattern") ? node.get("pattern").asText() : null;

            return new DynamicName(signature, pattern);
        }

    }

}
