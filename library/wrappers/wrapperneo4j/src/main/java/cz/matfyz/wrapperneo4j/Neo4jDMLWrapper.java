package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidNameException;

import java.util.ArrayList;
import java.util.List;

public class Neo4jDMLWrapper implements AbstractDMLWrapper {

    @Override public void clear() {
        kindName = null;
        propertyValues.clear();
        fromNodeLabel = null;
        fromNodeValues.clear();
        toNodeLabel = null;
        toNodeValues.clear();
    }

    private record PropertyValue(String name, String value) {}

    private String kindName = null;
    private final List<PropertyValue> propertyValues = new ArrayList<>();
    private String fromNodeLabel = null;
    private final List<PropertyValue> fromNodeValues = new ArrayList<>();
    private String toNodeLabel = null;
    private final List<PropertyValue> toNodeValues = new ArrayList<>();

    @Override public void setKindName(String name) {
        if (!nameIsValid(name))
            throw InvalidNameException.kind(name);

        kindName = name;
    }

    @Override public void append(String name, Object value) {
        final String stringValue = value == null ? null : value.toString();

        final var split = name.split(AbstractDDLWrapper.PATH_SEPARATOR);
        if (split.length == 1) {
            if (!nameIsValid(name))
                throw InvalidNameException.property(name);

            propertyValues.add(new PropertyValue(name, stringValue));
            return;
        }

        final var propertyName = split[1];
        if (!nameIsValid(propertyName))
            throw InvalidNameException.property(name);

        final var propertyValue = new PropertyValue(propertyName, stringValue);

        final var firstPart = split[0];

        if (firstPart.startsWith(Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX)) {
            fromNodeLabel = firstPart.substring(Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX.length());
            fromNodeValues.add(propertyValue);
        }
        else if (firstPart.startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX)) {
            toNodeLabel = firstPart.substring(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX.length());
            toNodeValues.add(propertyValue);
        }
        else {
            throw InvalidNameException.property(name);
        }
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w.]+$");
    }

    @Override public StringStatement createDMLStatement() {
        if (kindName == null)
            throw InvalidNameException.kind(null);

        if (fromNodeValues.isEmpty() || toNodeValues.isEmpty())
            return processNode();
        else
            return processRelationship();
    }

    private StringStatement processNode() {
        return StringStatement.create(
            createMergeForNode("", kindName, propertyValues) + ";"
        );
    }

    private StringStatement processRelationship() {
        if (fromNodeLabel == null || toNodeLabel == null)
            throw InvalidNameException.node(null);

        final String fromNodeMerge = createMergeForNode("from", fromNodeLabel, fromNodeValues);
        final String toNodeMerge = createMergeForNode("to", toNodeLabel, toNodeValues);
        final String relationshipMerge = String.format("MERGE (from)-[:%s %s]->(to)", kindName, propertiesToString(propertyValues));

        return StringStatement.create(
            fromNodeMerge + "\n"
            + toNodeMerge + "\n"
            + relationshipMerge + ";"
        );
    }

    private static String createMergeForNode(String boundVariable, String label, List<PropertyValue> properties) {
        return String.format("MERGE (%s:%s %s)", boundVariable, label, propertiesToString(properties));
    }

    private static String propertiesToString(List<PropertyValue> properties) {
        final var sb = new StringBuilder();
        sb.append("{");

        for (final var property : properties) {
            sb
                .append(" ")
                .append(property.name)
                .append(": ")
                .append(escapeString(property.value))
                .append(",");
        }

        if (!properties.isEmpty()) // Remove the last comma
            sb.deleteCharAt(sb.length() - 1);

        sb.append(" }");

        return sb.toString();
    }

    private static String escapeString(String input) {
        return input == null
            ? "null"
            : "'" + input.replace("'", "\\'") + "'";
    }

}
