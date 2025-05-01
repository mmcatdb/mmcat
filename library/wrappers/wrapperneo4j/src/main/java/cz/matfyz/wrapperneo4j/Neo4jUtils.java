package cz.matfyz.wrapperneo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.matfyz.core.adminer.GraphResponse.GraphNode;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationship;

import org.neo4j.driver.Value;

public class Neo4jUtils {
    private Neo4jUtils() {}

    /**
     * Extracts the properties of a node.
     *
     * @param node The node represented as a {@link Value}.
     * @param propertyNames A {@link List} to add propertyNames to.
     * @return A {@link GraphNode} containing the node's ID and properties.
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
        String labelsPropertyName = "labels";
        for (final var label : node.asNode().labels()) {
            labels.add(label);
        }

        properties.put(labelsPropertyName, labels);

        if (!propertyNames.contains(labelsPropertyName)) {
            propertyNames.add(labelsPropertyName);
        }

        return new GraphNode(id, properties);
    }

    /**
     * Extracts the properties of a relationship.
     *
     * @param relationship The relationship represented as a {@link Value}.
     * @param propertyNames A {@link List} to add propertyNames to.
     * @return A {@link GraphRelationship} containing the relationship's ID and properties.
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

        return new GraphRelationship(id, startNodeId, endNodeId, properties);
    }

}
