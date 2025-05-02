package cz.matfyz.wrapperneo4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cz.matfyz.core.adminer.GraphResponse.GraphNode;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationship;

import org.neo4j.driver.Value;

public class Neo4jUtils {
    private Neo4jUtils() {}

    public static final String ID = "#id";
    public static final String LABELS = "#labels";
    public static final String FROM_NODE_PREFIX = "from.";
    public static final String TO_NODE_PREFIX = "to.";

    /**
     * A {@link List} of Neo4j functions.
     */
    public static final List<String> FUNCTIONS = Arrays.asList("ANY", "ALL", "NONE", "SINGLE", "SIZE");

    /**
     * A {@link Map} of functions that can be used on Neo4j nodes.
     */
    public static final Map<String, String> NODE_LABEL_FUNCTIONS = defineNodeLabelFunctions();

    /**
     * A {@link Map} of functions that can be used on Neo4j relationships.
     */
    public static final Map<String, String> RELATIONSHIP_LABEL_FUNCTIONS = defineRelationshipLabelFunctions();

    /**
     * Defines a mapping of node labels with functions to Cypher functions.
     */
    private static Map<String, String> defineNodeLabelFunctions() {
        final var functions = new TreeMap<String, String>();

        for (String function: FUNCTIONS) {
            functions.put(Neo4jUtils.LABELS + " - " + function, function);
        }

        return functions;
    }

    /**
     * Defines a mapping of start end end node labels with functions to Cypher functions.
     */
    private static Map<String, String> defineRelationshipLabelFunctions() {
        final var functions = new TreeMap<String, String>();

        for (String function: FUNCTIONS) {
            functions.put(Neo4jUtils.FROM_NODE_PREFIX + Neo4jUtils.LABELS + " - " + function, function);
            functions.put(Neo4jUtils.TO_NODE_PREFIX + Neo4jUtils.LABELS + " - " + function, function);
        }

        return functions;
    }

    /**
     * Extracts the properties of a node.
     *
     * @param propertyNames A {@link List} to add propertyNames to.
     */
    public static GraphNode getNodeProperties(Value node, List<String> propertyNames) {
        String id = node.asNode().elementId();

        Map<String, Object> properties = new HashMap<>();
        node.asNode().asMap().forEach((propertyName, propertyValue) -> {
            properties.put(propertyName, propertyValue);

            if (!propertyNames.contains(propertyName)) {
                propertyNames.add(propertyName);
            }
        });

        List<String> labels = new ArrayList<>();
        for (final var label : node.asNode().labels()) {
            labels.add(label);
        }

        properties.put(LABELS, labels);

        for (String function: NODE_LABEL_FUNCTIONS.keySet()) {
            if (!propertyNames.contains(function)) {
                propertyNames.add(function);
            }
        }

        return new GraphNode(id, properties);
    }

    /**
     * Extracts the properties of a relationship.
     *
     * @param propertyNames A {@link List} to add propertyNames to.
     */
    public static GraphRelationship getRelationshipProperties(Value relationship, List<String> propertyNames) {
        String id = relationship.asRelationship().elementId();

        Map<String, Object> properties = new HashMap<>();
        relationship.asRelationship().asMap().forEach((propertyName, propertyValue) -> {
            properties.put(propertyName, propertyValue);

            if (!propertyNames.contains(propertyName)) {
                propertyNames.add(propertyName);
            }
        });

        String startNodeId = relationship.asRelationship().startNodeElementId();
        String endNodeId = relationship.asRelationship().endNodeElementId();

        for (String function: RELATIONSHIP_LABEL_FUNCTIONS.keySet()) {
            if (!propertyNames.contains(function)) {
                propertyNames.add(function);
            }
        }

        return new GraphRelationship(id, startNodeId, endNodeId, properties);
    }

}
