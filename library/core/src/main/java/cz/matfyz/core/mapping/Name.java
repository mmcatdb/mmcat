package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@JsonDeserialize(using = Name.Deserializer.class)
public abstract class Name implements Serializable {

    protected Name() {}

    // #region Serialization

    public static class Deserializer extends StdDeserializer<Name> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public Name deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);

            if (node.has("value"))
                return new StringName(node.get("value").asText());

            final String type = node.get("type").asText();
            if (!node.has("signature"))
                return new TypedName(node.get("type").asText());

            final Signature signature = codec.treeToValue(node.get("signature"), Signature.class);
            final @Nullable String pattern = node.hasNonNull("pattern") ? node.get("pattern").asText() : null;

            return new DynamicName(type, signature, pattern);
        }
    }

    // #endregion

    /**
     * A normal string name.
     */
    public static class StringName extends Name {

        public final String value;

        public StringName(String value) {
            this.value = value;
        }

        @Override public String toString() {
            return value;
        }

        @Override public boolean equals(Object object) {
            return object instanceof StringName name && value.equals(name.value);
        }

    }

    /**
     * A name with a special meaning in the given datasource. E.g., the root of the complex property tree, or nodes in a Neo4j relationship.
     */
    public static class TypedName extends Name {

        public final String type;

        public TypedName(String type) {
            super();
            this.type = type;
        }

        @Override public String toString() {
            return "<" + type + ">";
        }

        @Override public boolean equals(Object object) {
            return object instanceof TypedName name && type.equals(name.type);
        }

        /** The property is a root of the access path tree, the name doesn't mean anything. */
        public static final String ROOT = "root";
        /** The property is a value in an objex, the name represents its key. */
        public static final String KEY = "key";
        /** The property is an element of an array, the name represents its index. */
        public static final String INDEX = "index";

    }

    /**
     * Name that is mapped to a key (in an objex) or to an index (in an array).
     */
    public static class DynamicName extends TypedName implements Comparable<DynamicName> {

        public final Signature signature;

        public DynamicName(String type, Signature signature, @Nullable String pattern) {
            super(type);
            this.signature = signature;
            this.pattern = pattern;
        }

        /**
         * If provided, only the matching names will be considered when retrieving records.
         * There can only be only one subpath with dynamic name with null pattern in any complex property.
         * The * character represents a wildcard. Other allowed characters are "a-zA-Z0-9._-".
         * If a value can be matched by multiple patterns, the first one will be used.
         */
        @JsonProperty
        private final @Nullable String pattern;

        public @Nullable Pattern compiledPattern() {
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
            final String patternString = pattern == null ? "" : " (" + pattern + ")";
            return "<" + type + patternString + ": " + signature.toString() + ">";
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

    }

    /** Compares names without the use of signatures. Used for sorting properties independently on schema category (mostly in tests). */
    public static int compareNamesLexicographically(Name a, Name b) {
        // String names first, typed later, dynamic last.
        if (a instanceof StringName aString) {
            return b instanceof StringName bString
                ? aString.value.compareTo(bString.value)
                : -1;
        }

        if (b instanceof StringName)
            return 1;

        // At this point, both names are typed.
        if (a instanceof DynamicName aDynamic) {
            if (!(b instanceof DynamicName bDynamic))
                return 1;

            return aDynamic.type.compareTo(bDynamic.type);
        }

        if (b instanceof DynamicName)
            return -1;

        return ((TypedName) a).type.compareTo(((TypedName) b).type);
    }

}
