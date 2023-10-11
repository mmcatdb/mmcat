package cz.matfyz.abstractwrappers.queryresult;

import cz.matfyz.core.utils.IndentedStringBuilder;

import java.util.Map;
import java.util.stream.Collectors;

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

}