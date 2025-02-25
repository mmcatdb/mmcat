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

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
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
        String id = node.asNode().elementId().split(":")[2];

        Map<String, Object> properties = new HashMap<>();
        node.asNode().asMap().forEach(properties::put);

        List<String> labels = new ArrayList<>();
        for (final var label : node.asNode().labels()) {
            labels.add(label);
        }
        properties.put("labels", labels);

        return new GraphElement(id, properties);
    }

    /**
     * Extracts the properties of a relationship.
     *
     * @param relationship The relationship represented as a {@link Value}.
     * @return A {@link GraphElement} containing the relationship's ID and properties.
     */
    public static GraphElement getRelationshipProperties(Value relationship) {
        String id = relationship.asRelationship().elementId().split(":")[2];

        Map<String, Object> properties = new HashMap<>();
        properties.put("type", relationship.asRelationship().type());

        relationship.asRelationship().asMap().forEach(properties::put);

        return new GraphElement(id, properties);
    }

    /**
     * Retrieves a set of distinct property names based on a specified match clause.
     *
     * @param session The Neo4j session to use for the query.
     * @param matchClause The Cypher match clause to identify nodes or relationships.
     * @return A {@link Set} of property names.
     */
    private static Set<String> getPropertyNames(Session session, String matchClause) {
        Set<String> properties = new HashSet<>();

        Result queryResult = session.run(String.format("""
            %s
            UNWIND keys(a) AS key
            RETURN DISTINCT key
            ORDER BY key;
            """, matchClause));
        while (queryResult.hasNext()) {
            Record queryRecord = queryResult.next();
            properties.add(queryRecord.get("key").asString());
        }

        return properties;
    }

    /**
     * Retrieves a set of distinct property names for all nodes.
     *
     * @param session The Neo4j session to use for the query.
     * @return A {@link Set} of node property names (keys).
     */
    public static Set<String> getNodePropertyNames(Session session) {
        return getPropertyNames(session, "MATCH (a)");
    }

    /**
     * Retrieves a set of distinct property names for all relationships.
     *
     * @param session The Neo4j session to use for the query.
     * @return A {@link Set} of relationship property names (keys).
     */
    public static Set<String> getRelationshipPropertyNames(Session session) {
        return getPropertyNames(session, "MATCH ()-[a]->()");
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
}
