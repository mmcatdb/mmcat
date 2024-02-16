package cz.matfyz.evolution;

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

@JsonSerialize(using = Version.Serializer.class)
@JsonDeserialize(using = Version.Deserializer.class)
public class Version implements java.io.Serializable, java.lang.Comparable<Version>, java.lang.CharSequence {
    
    private final String value;
    private final int integerValue;

    //private static int stringSize = 8;
    //private static String stringFormat = "%0" + stringSize + "d";
    private static String stringFormat = "%d";

    private Version(int integerValue) {
        this.value = String.format(stringFormat, integerValue);
        this.integerValue = integerValue;
    }

    public Version(String value) {
        //if (value.length() != stringSize)
        //    throw new NumberFormatException("Value: " + value + " is not valid version.");

        this.integerValue = Integer.parseInt(value);
        if (this.integerValue < 0)
            throw new NumberFormatException("Value: " + value + " is not valid version.");

        this.value = value;
    }

    public Version generateNext() {
        return new Version(integerValue + 1);
    }

    public static Version generateInitial() {
        return new Version(0);
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
    
            return new Version(node.asText());
        }

    }

}
