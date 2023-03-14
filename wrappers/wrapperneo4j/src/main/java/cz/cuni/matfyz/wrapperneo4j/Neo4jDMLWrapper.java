package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.WrapperException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class Neo4jDMLWrapper implements AbstractDMLWrapper {

    private record PropertyValue(String name, String value) {}
    
    private String kindName = null;
    private List<PropertyValue> propertyValues = new ArrayList<>();
    private String fromNodeLabel = null;
    private List<PropertyValue> fromNodeValues = new ArrayList<>();
    private String toNodeLabel = null;
    private List<PropertyValue> toNodeValues = new ArrayList<>();
    
    @Override
    public void setKindName(String name) {
        if (!nameIsValid(name))
            throw new WrapperException("Kind name \"" + name + "\" doesn't match the required pattern /^[\\w]+$/.");

        kindName = name;
    }

    @Override
    public void append(String name, Object value) {
        final String stringValue = value == null ? null : value.toString();

        final var split = name.split(AbstractDDLWrapper.PATH_SEPARATOR);
        if (split.length == 1) {
            if (!nameIsValid(name))
                throw new WrapperException("Property name \"" + name + "\" doesn't match the required pattern /^[\\w]+$/.");

            propertyValues.add(new PropertyValue(name, stringValue));
            return;
        }

        final var propertyName = split[1];
        if (!nameIsValid(propertyName))
            throw new WrapperException("Nested property name \"" + name + "\" doesn't match the required pattern /^[\\w]+$/.");

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
            throw new WrapperException("Nested property with name: " + name + " is not allowed.");
        }
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w.]+$");
    }

    @Override
    public Neo4jStatement createDMLStatement() {
        if (kindName == null)
            throw new WrapperException("Kind name is null.");

        if (fromNodeValues.isEmpty() || toNodeValues.isEmpty())
            return processNode();
        else
            return processRelationship();
    }

    private Neo4jStatement processNode() {
        return new Neo4jStatement(
            createMergeForNode("", kindName, propertyValues) + ";"
        );
    }

    private Neo4jStatement processRelationship() {
        if (fromNodeLabel == null)
            throw new WrapperException("From node label is null");

        if (toNodeLabel == null)
            throw new WrapperException("To node label is null");

        final String fromNodeMerge = createMergeForNode("from", fromNodeLabel, fromNodeValues);
        final String toNodeMerge = createMergeForNode("to", toNodeLabel, toNodeValues);
        final String relationshipMerge = String.format("MERGE (from)-[:%s %s]->(to)", kindName, propertiesToString(propertyValues));

        return new Neo4jStatement(
            fromNodeMerge + "\n"
            + toNodeMerge + "\n"
            + relationshipMerge + ";"
        );
    }

    private static String createMergeForNode(String boundVariable, String label, List<PropertyValue> properties) {
        return String.format("MERGE (%s:%s %s)", boundVariable, label, propertiesToString(properties));
    }

    private static String propertiesToString(List<PropertyValue> properties) {
        final var output = new StringBuilder();
        output.append("{");
        
        for (final var property : properties)
            output
            .append(" ")
            .append(property.name)
            .append(": ")
            .append(escapeString(property.value))
            .append(",");
        
        if (!properties.isEmpty()) // Remove the last comma
            output.deleteCharAt(output.length() - 1);
        
        output.append(" }");

        return output.toString();
    }
    
    private static String escapeString(String input) {
        return input == null
            ? "null"
            : "'" + input.replace("'", "\\'") + "'";
    }

    @Override
    public void clear() {
        kindName = null;
        propertyValues = new ArrayList<>();
        fromNodeLabel = null;
        fromNodeValues = new ArrayList<>();
        toNodeLabel = null;
        toNodeValues = new ArrayList<>();
    }

}
