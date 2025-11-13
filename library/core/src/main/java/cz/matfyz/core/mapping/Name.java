package cz.matfyz.core.mapping;

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

            if (type.equals(DynamicName.TYPE))
                return new DynamicName(node.hasNonNull("pattern") ? node.get("pattern").asText() : null);
            if (type.equals(IndexName.TYPE))
                return new IndexName(node.get("dimension").asInt());

            return new TypedName(type);
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
     * A name with a special meaning in the given datasource (or in general). E.g., the root of the complex property tree, or nodes in a Neo4j relationship.
     */
    public static class TypedName extends Name {

        public final String type;

        public TypedName(String type) {
            this.type = type;
        }

        @Override public String toString() {
            return type;
        }

        @Override public boolean equals(Object object) {
            return object instanceof TypedName name && type.equals(name.type);
        }

        /** The property is a root of the access path tree, the name doesn't mean anything. */
        public static final String ROOT = "$root";
        /** The key corresponding to the {@link TypedName#VALUE}. */
        public static final String KEY = "$key";
        /** The actual value of the map/array property. */
        public static final String VALUE = "$value";

    }

    /**
     * Name that is mapped to a key in an object / map / dictionary / etc.
     * The actual value of the name is stored in a child property with the name {@link TypedName#KEY}.
     */
    public static class DynamicName extends TypedName implements Comparable<DynamicName> {

        static final String TYPE = "$dynamic";

        /**
         * If provided, only the matching names will be considered when retrieving records.
         * There can only be only one subpath with dynamic name with null pattern in any complex property.
         * The * character represents a wildcard. Other allowed characters are "a-zA-Z0-9._-".
         * If a value can be matched by multiple patterns, the first one will be used.
         */
        @JsonProperty
        private final @Nullable String pattern;

        public DynamicName(@Nullable String pattern) {
            super(TYPE);
            this.pattern = pattern;
        }

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
            final String patternString = pattern == null ? "" : "(" + pattern + ")";
            return TYPE + patternString;
        }

        @Override public boolean equals(Object object) {
            return object instanceof DynamicName dynamicName &&
                (pattern == null ? dynamicName.pattern == null : pattern.equals(dynamicName.pattern));
        }

        @Override public int compareTo(DynamicName dynamicName) {
            if (pattern == null)
                return dynamicName.pattern == null ? 0 : -1;

            return dynamicName.pattern == null ? 1 : pattern.compareTo(dynamicName.pattern);
        }

    }

    /**
     * Stores the value of the index in an array.
     */
    public static class IndexName extends TypedName implements Comparable<IndexName> {

        static final String TYPE = "$index";

        /**
         * An array can be multi-dimensional. This tells us for which dimension this index name is used. Zero based.
         */
        @JsonProperty
        public final int dimension;

        public IndexName(int dimension) {
            super(TYPE);
            this.dimension = dimension;
        }

        @Override public String toString() {
            return TYPE + "(" + dimension + ")";
        }

        @Override public boolean equals(Object object) {
            return object instanceof IndexName dynamicName && dimension == dynamicName.dimension;
        }

        @Override public int compareTo(IndexName dynamicName) {
            return dimension - dynamicName.dimension;
        }

    }

    /** Compares names without the use of signatures. Used for sorting properties independently on schema category (mostly in tests). */
    public static int compareNamesLexicographically(Name a, Name b) {
        // String names first, typed later, specials like dynamic and index last.
        if (a instanceof StringName aString) {
            return b instanceof StringName bString
                ? aString.value.compareTo(bString.value)
                : -1;
        }

        if (b instanceof StringName)
            return 1;

        // Now both have to be typed ...
        final var typeComparison = ((TypedName) a).type.compareTo(((TypedName) b).type);
        if (typeComparison != 0)
            return typeComparison;

        // They are the same type. We compare specific types now.
        if (a instanceof DynamicName dynamic)
            return dynamic.compareTo((DynamicName) b);

        if (a instanceof IndexName index)
            return index.compareTo((IndexName) b);

        return 0;
    }

}
