package cz.matfyz.core.querying.queryresult;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = ResultMap.Serializer.class)
public class ResultMap implements ResultNode {

    private final Map<String, ResultNode> children;

    public ResultMap(Map<String, ResultNode> children) {
        this.children = children;
    }

    public Map<String, ResultNode> children() {
        return children;
    }

    @Override public void printTo(Printer printer) {
        final var isMultilined = children.values().stream().anyMatch(child -> !(child instanceof ResultLeaf));

        if (isMultilined) {
            printer.append("{").down().nextLine();

            for (final var child : children.entrySet()) {
                printer
                    .append("\"").append(child.getKey()).append("\": ")
                    .append(child.getValue())
                    .append(",").nextLine();
            }

            if (!children.isEmpty())
                printer.remove();

            printer.remove().up().nextLine().append("}");
        }
        else {
            printer.append("{ ");
            for (final var child : children.entrySet()) {
                printer
                    .append("\"").append(child.getKey()).append("\": ")
                    .append(child.getValue())
                    .append(", ");
            }

            if (!children.isEmpty())
                printer.remove().append(" ");

            printer.append("}");
        }
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    public static class Serializer extends StdSerializer<ResultMap> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ResultMap> t) {
            super(t);
        }

        @Override public void serialize(ResultMap resultMap, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            for (final var child : resultMap.children.entrySet())
                generator.writePOJOField(child.getKey(), child.getValue());
            generator.writeEndObject();
        }

    }

    public static class Builder implements NodeBuilder {

        private Map<String, ResultNode> children = new TreeMap<>();

        public Builder put(String key, ResultNode node) {
            children.put(key, node);
            return this;
        }

        public ResultMap build() {
            return new ResultMap(children);
        }

    }

}
