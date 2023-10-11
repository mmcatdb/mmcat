package cz.matfyz.abstractwrappers.queryresult;

import cz.matfyz.core.utils.IndentedStringBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = ResultMap.Serializer.class)
public class ResultMap implements ResultNode {

    public final Map<String, ResultNode> children;

    public ResultMap(Map<String, ResultNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        final var isMultilined = children.values().stream().anyMatch(child -> !(child instanceof ResultLeaf));
        final var builder = new StringBuilder();

        if (isMultilined) {
            builder.append("{\n");
            
            final var nestedBuilder = new IndentedStringBuilder(1);
            for (final var child : children.entrySet()) {
                nestedBuilder
                    .append("\"").append(child.getKey()).append("\": ")
                    .append(child.getValue())
                    .append(",\n");
            }

            builder
                .append(nestedBuilder)
                .append("\n}");
        }
        else {
            builder.append("{ ");
            final String childStrings = children.entrySet().stream()
                .map(child -> "\"" + child.getKey() + "\": " + child.getValue())
                .collect(Collectors.joining(", "));
            builder
                .append(childStrings)
                .append(" }");
        }

        return builder.toString();
    }

    public static class Serializer extends StdSerializer<ResultMap> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ResultMap> t) {
            super(t);
        }

        @Override
        public void serialize(ResultMap resultMap, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            for (final var child : resultMap.children.entrySet())
                generator.writePOJOField(child.getKey(), child.getValue());
            generator.writeEndObject();
        }

    }

}