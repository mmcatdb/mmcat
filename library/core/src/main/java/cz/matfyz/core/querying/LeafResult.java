package cz.matfyz.core.querying;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = LeafResult.Serializer.class)
public class LeafResult implements ResultNode {

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

    public static class Serializer extends StdSerializer<LeafResult> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<LeafResult> t) {
            super(t);
        }

        @Override public void serialize(LeafResult result, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(result.value);
        }

    }

}
