package cz.matfyz.wrapperneo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.matfyz.core.adminer.GraphResponse.GraphElement;
import cz.matfyz.core.adminer.GraphResponse.GraphNode;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationship;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationshipExtended;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;

public class Neo4jUtils {
    private Neo4jUtils() {}

    /**
     * Extracts the properties of a node.
     *
     * @param node The node represented as a {@link Value}.
     * @return A {@link GraphElement} containing the node's ID and properties.
     */
    public static GraphElement getNodeProperties(Value node) {
        String id = node.asNode().elementId();

        Map<String, Object> properties = new HashMap<>();
        node.asNode().asMap().forEach(properties::put);

        List<String> labels = new ArrayList<>();
        for (final var label : node.asNode().labels()) {
            labels.add(label);
        }

        return new GraphNode(id, properties, labels);
    }

    /**
     * Extracts the properties of a relationship and its start and end node.
     *
     * @param relationship The relationship represented as a {@link Value}.
     * @param startNode The start node of the relationship represented as a {@link Value}.
     * @param endNode The end node of the relationship represented as a {@link Value}.
     * @return A {@link GraphElement} containing the relationship's ID and properties.
     */
    public static GraphElement getRelationshipProperties(Value relationship, Value startNode, Value endNode) {
        String id = relationship.asRelationship().elementId();

        Map<String, Object> properties = new HashMap<>();
        relationship.asRelationship().asMap().forEach(properties::put);

        String startNodeId = relationship.asRelationship().startNodeElementId();
        GraphNode startNodeProperties = (GraphNode) getNodeProperties(startNode);
        String endNodeId = relationship.asRelationship().endNodeElementId();
        GraphNode endNodeProperties = (GraphNode) getNodeProperties(endNode);

        return new GraphRelationshipExtended(id, properties, startNodeId, startNodeProperties.properties(), startNodeProperties.labels(), endNodeId, endNodeProperties.properties(), endNodeProperties.labels());
    }

    /**
     * Extracts the properties of a relationship.
     *
     * @param relationship The relationship represented as a {@link Value}.
     * @return A {@link GraphElement} containing the relationship's ID and properties.
     */
    public static GraphElement getRelationshipProperties(Value relationship) {
        String id = relationship.asRelationship().elementId();

        Map<String, Object> properties = new HashMap<>();
        relationship.asRelationship().asMap().forEach(properties::put);

        String startNodeId = relationship.asRelationship().startNodeElementId();
        String endNodeId = relationship.asRelationship().endNodeElementId();

        return new GraphRelationship(id, properties, startNodeId, endNodeId);
    }

    /**
     * Converts a Neo4j {@link Value} to a {@link GraphElement}, which can be either a {@link GraphNode} or a {@link GraphRelationship}.
     *
     * @param value The Neo4j {@link Value} to be converted.
     * @return A {@link GraphElement}, which is either a {@link GraphNode} if the value represents a node,
     *         or a {@link GraphRelationship} if the value represents a relationship.
     * @throws IllegalArgumentException if the value is neither a node nor a relationship.
     */
    public static GraphElement getGraphElementProperties(Value value) {
        if (value.hasType(TypeSystem.getDefault().NODE())) {
            return Neo4jUtils.getNodeProperties(value);
        } else if (value.hasType(TypeSystem.getDefault().RELATIONSHIP())) {
            return Neo4jUtils.getRelationshipProperties(value);
        } else {
            throw new IllegalArgumentException("Unexpected value type: " + value.type());
        }
    }

}
