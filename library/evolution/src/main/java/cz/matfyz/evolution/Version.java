package cz.matfyz.evolution;

import cz.matfyz.evolution.exception.VersionException;

import java.io.IOException;
import java.util.regex.Matcher;
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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents the system version of a project.
 * It has a global version and optionally a local version. The former is used for the whole project, the latter is specific for each entity.
 * In either case, the global version is unique. The local version is only an additional information, i.e., something like a comment or a description.
 * The format is `<global-version>` or `<global-version>:<local-version>`. The global version is a non-negative integer. Both versions can't contain the `:` character.
 */
@JsonSerialize(using = Version.Serializer.class)
@JsonDeserialize(using = Version.Deserializer.class)
public class Version implements java.io.Serializable, java.lang.Comparable<Version>, java.lang.CharSequence {

    private final String value;
    private final int integerValue;
    private final @Nullable String localValue;

    private static String stringFormat = "%d";
    private static String stringFormatWithLocal = "%d:%s";

    private static final Pattern pattern = Pattern.compile("^([\\d])+(:[^:]+)?$");

    private Version(int integerValue, @Nullable String localValue) {
        this.value = localValue == null
            ? String.format(stringFormat, integerValue)
            : String.format(stringFormatWithLocal, integerValue, localValue);

        this.integerValue = integerValue;
        this.localValue = localValue;
    }

    public static Version fromString(String value) {
        // This null check has been added just so I can run it w/o errors
        if (value == null) {
            return Version.generateInitial(null);
        }

        final Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
            throw VersionException.parse(value);

        final String globalValue = matcher.group(1);
        final @Nullable String rawLocalValue = matcher.group(2);
        final @Nullable String localValue = rawLocalValue == null ? null : rawLocalValue.substring(1);

        try {
            final int integerValue = Integer.parseInt(globalValue);
            return new Version(integerValue, localValue);
        }
        catch (NumberFormatException e) {
            // This might happen if the value is too large.
            throw VersionException.parse(value);
        }
    }

    public Version generateNext() {
        return generateNext(null);
    }

    public Version generateNext(@Nullable String localValue) {
        return new Version(integerValue + 1, localValue);
    }

    public static Version generateInitial(@Nullable String localValue) {
        return new Version(0, localValue);
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

    public int compareTo(Version another) {
        return value.compareTo(another.value);
    }

    @Override public boolean equals(Object object) {
        return object instanceof Version another && another != null && value.equals(another.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }

    public static class Serializer extends StdSerializer<Version> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Version> t) {
            super(t);
        }

        @Override public void serialize(Version id, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(id.value);
        }

    }

    public static class Deserializer extends StdDeserializer<Version> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override public Version deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return Version.fromString(node.asText());
        }

    }

}
