package cz.matfyz.core.querying;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = LeafResult.Serializer.class)
public class LeafResult extends ResultNode {

    public final String value;

    public LeafResult(String value) {
        this.value = value;
    }

    @Override public void printTo(Printer printer) {
        printer.append("\"" + value + "\"");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    // TODO We should use types but for now, strings are enough.
    public final static String NULL_STRING = "null";
    public final static String TRUE_STRING = "true";
    public final static String FALSE_STRING = "false";

    public static String getBooleanString(boolean value) {
        return value ? TRUE_STRING : FALSE_STRING;
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<LeafResult> {
        public Serializer() { this(null); }
        public Serializer(Class<LeafResult> t) { super(t); }

        @Override public void serialize(LeafResult result, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(result.value);
        }
    }

    // #endregion

}
