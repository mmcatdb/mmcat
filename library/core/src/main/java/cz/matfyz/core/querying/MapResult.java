package cz.matfyz.core.querying;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = MapResult.Serializer.class)
public class MapResult extends ResultNode {

    private final Map<String, ResultNode> children;

    public MapResult(Map<String, ResultNode> children) {
        this.children = children;
    }

    public Map<String, ResultNode> children() {
        return children;
    }

    @Override public void printTo(Printer printer) {
        final var isMultilined = children.values().stream().anyMatch(child -> !(child instanceof LeafResult));

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

    // #region Serialization

    public static class Serializer extends StdSerializer<MapResult> {
        public Serializer() { this(null); }
        public Serializer(Class<MapResult> t) { super(t); }

        @Override public void serialize(MapResult result, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            for (final var child : result.children.entrySet())
                generator.writePOJOField(child.getKey(), child.getValue());
            generator.writeEndObject();
        }
    }

    // #endregion

    public static class Builder implements NodeBuilder {

        private Map<String, ResultNode> children = new TreeMap<>();

        public Builder put(String key, ResultNode node) {
            children.put(key, node);
            return this;
        }

        public MapResult build() {
            return new MapResult(children);
        }

    }

}
