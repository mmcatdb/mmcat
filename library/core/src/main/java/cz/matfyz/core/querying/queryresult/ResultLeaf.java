package cz.matfyz.core.querying.queryresult;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = ResultLeaf.Serializer.class)
public class ResultLeaf implements ResultNode {

    public final String value;

    public ResultLeaf(String value) {
        this.value = value;
    }

    @Override public void printTo(Printer printer) {
        printer.append("\"" + value + "\"");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    public static class Serializer extends StdSerializer<ResultLeaf> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ResultLeaf> t) {
            super(t);
        }

        @Override public void serialize(ResultLeaf resultMap, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(resultMap.value);
        }

    }

}
