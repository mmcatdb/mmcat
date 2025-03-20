package cz.matfyz.inference.adminer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cz.matfyz.core.adminer.GraphResponse.GraphElement;
import cz.matfyz.core.adminer.GraphResponse.GraphNode;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationship;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.driver.Record;

public final class Neo4jAlgorithms implements AdminerAlgorithmsInterface {
    private static final Neo4jAlgorithms INSTANCE = new Neo4jAlgorithms();

    private Neo4jAlgorithms() {}

    public static Neo4jAlgorithms getInstance() {
        return INSTANCE;
    }

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
            return Neo4jAlgorithms.getNodeProperties(value);
        } else if (value.hasType(TypeSystem.getDefault().RELATIONSHIP())) {
            return Neo4jAlgorithms.getRelationshipProperties(value);
        } else {
            throw new IllegalArgumentException("Unexpected value type: " + value.type());
        }
    }

    /**
     * Retrieves a set of distinct property names based on a specified match clause.
     *
     * @param session The Neo4j session to use for the query.
     * @param kindName The name of the kind.
     * @param forNode If {@code true}, retrieves properties for nodes; if {@code false}, retrieves properties for relationships.
     * @return A {@link Set} of property names.
     */
    private static Set<String> getPropertyNames(Session session, String kindName, boolean forNode) {
        StringBuilder matchClause = new StringBuilder("MATCH ");

        if (forNode) {
            matchClause.append(kindName != null ? "(a: " + kindName + ")" : "(a)");
        } else {
            matchClause.append("()-[a]-()");

            if (kindName != null) {
                matchClause.append(" WHERE type(a) = '" + kindName + "'");
            }
        }

        Set<String> properties = new HashSet<>();

        Result queryResult = session.run(String.format("""
            %s
            UNWIND keys(a) AS key
            RETURN DISTINCT key
            ORDER BY key;
            """, matchClause.toString()));
        while (queryResult.hasNext()) {
            Record queryRecord = queryResult.next();
            properties.add(queryRecord.get("key").asString());
        }
        properties.add("#elementId");

        if (forNode) {
            properties.add("#labels");
        } else {
            properties.add("#startNodeId");
            properties.add("#endNodeId");
        }

        return properties;
    }

    /**
     * Retrieves a set of distinct property names for all nodes.
     *
     * @param session The Neo4j session to use for the query.
     * @param kindName The name of the kind.
     * @return A {@link Set} of node property names (keys).
     */
    public static Set<String> getNodePropertyNames(Session session, String kindName) {
        return getPropertyNames(session, kindName, true);
    }

    /**
     * Retrieves a set of distinct property names for all relationships.
     *
     * @param session The Neo4j session to use for the query.
     * @param kindName The name of the kind.
     * @return A {@link Set} of relationship property names (keys).
     */
    public static Set<String> getRelationshipPropertyNames(Session session, String kindName) {
        return getPropertyNames(session, kindName, false);
    }

    /**
     * Defines a mapping of comparison operators to their Cypher equivalents.
     *
     * @return A {@link Map} containing operator mappings.
     */
    private static Map<String, String> defineOperators() {
        final var ops = new TreeMap<String, String>();
        ops.put("Equal", "=");
        ops.put("NotEqual", "<>");
        ops.put("Less", "<");
        ops.put("LessOrEqual", "<=");
        ops.put("Greater", ">");
        ops.put("GreaterOrEqual", ">=");

        ops.put("IsNull", "IS NULL");
        ops.put("IsNotNull", "IS NOT NULL");

        ops.put("MatchRegEx", "=~");
        ops.put("StartsWith", "STARTS WITH");
        ops.put("EndsWith", "ENDS WITH");
        ops.put("Contains", "CONTAINS");

        ops.put("In", "IN");

        return ops;
    }

    /**
     * A map of operator names to Neo4j operators.
     */
    private static final Map<String, String> OPERATORS = defineOperators();

    /**
     * A list of Neo4j unary operators.
     */
    private static final List<String> UNARY_OPERATORS = Arrays.asList("IS NULL", "IS NOT NULL");

    /**
     * A list of Neo4j operators used with string values.
     */
    private static final List<String> STRING_OPERATORS = Arrays.asList("=~", "STARTS WITH", "ENDS WITH", "CONTAINS");

    /**
     * A list of Neo4j quantifiers.
     */
    private static final List<String> QUANTIFIERS = Arrays.asList("ANY", "ALL", "NONE", "SINGLE");

    /**
     * Returns a map of operator names to Neo4j operators.
     */
    public Map<String, String> getOperators() {
        return OPERATORS;
    }

    /**
     * Returns a list of Neo4j unary operators.
     */
    public List<String> getUnaryOperators() {
        return UNARY_OPERATORS;
    }

    /**
     * Returns a list of Neo4j operators used with string values.
     */
    public List<String> getStringOperators() {
        return STRING_OPERATORS;
    }

    /**
     * Returns a list of Neo4j quantifiers.
     */
    public static List<String> getQuantifiers() {
        return QUANTIFIERS;
    }
}
