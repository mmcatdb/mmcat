package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameFilterQuery;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.GraphResponse;
import cz.matfyz.core.adminer.GraphResponse.GraphData;
import cz.matfyz.core.adminer.GraphResponse.GraphNode;
import cz.matfyz.core.adminer.GraphResponse.GraphRelationship;
import cz.matfyz.core.adminer.KindNamesResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.ReferenceKind;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;

public class Neo4jPullWrapper implements AbstractPullWrapper {

    private Neo4jProvider provider;

    public Neo4jPullWrapper(Neo4jProvider provider) {
        this.provider = provider;
    }

    private String createRelationshipQueryString(QueryContent query) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (query instanceof KindNameFilterQuery knfQuery)
            return "MATCH (from_node)-[relationship: " + knfQuery.kindNameQuery.kindName + "]->(to_node) " + createWhereClause(knfQuery.getFilters(), "relationship") + " RETURN from_node, relationship, to_node" + getOffsetAndLimit(knfQuery.kindNameQuery) + ";";

        if (!(query instanceof KindNameQuery knQuery))
            throw PullForestException.invalidQuery(this, query);

        return "MATCH (from_node)-[relationship: " + knQuery.kindName + "]->(to_node) RETURN from_node, relationship, to_node" + getOffsetAndLimit(knQuery) + ";";
    }

    private String createNodeQueryString(QueryContent query) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (query instanceof KindNameFilterQuery knfQuery) {
            String kindName = knfQuery.kindNameQuery.kindName;
            String queryBase = kindName.isEmpty() ? "MATCH (node) " : "MATCH (node:" + kindName + ") ";

            StringBuilder whereClause = new StringBuilder(createWhereClause(knfQuery.getFilters(), "node"));

            if (kindName.isEmpty()) {
                whereClause.append(whereClause.isEmpty() ? "WHERE" : " AND ");
                whereClause.append("size(labels(node)) = 0");
            }

            return queryBase + " " + whereClause.toString() + "RETURN node" + getOffsetAndLimit(knfQuery.kindNameQuery) + ";";
        }

        if (!(query instanceof KindNameQuery knQuery))
            throw PullForestException.invalidQuery(this, query);

        return "MATCH (node: " + knQuery.kindName + ") RETURN node" + getOffsetAndLimit(knQuery) + ";";
    }

    /**
     * Constructs a WHERE clause based on a list of filters.
     *
     * @param filters The filters to apply.
     * @param alias The alias assigned to the graph element in the query: 'n' for nodes, 'r' for relationships.
     * @return A WHERE clause as a {@link String}.
     */
    private String createWhereClause(List<AdminerFilter> filters, String alias) {
        if (filters.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String propertyName = filter.propertyName();

            if (i != 0) {
                whereClause.append(" AND ");
            }

            Double doubleValue = this.parseNumeric(filter.propertyValue());

            if (propertyName.startsWith("#labels")) {
                this.appendLabelsWhereClause(whereClause, alias, propertyName, filter.operator(), filter.propertyValue(), doubleValue);
                continue;
            }

            appendPropertyName(whereClause, alias, propertyName, doubleValue);

            String operator = OPERATORS.get(filter.operator());
            appendOperator(whereClause, operator);

            appendPropertyValue(whereClause, filter.propertyValue(), operator, doubleValue);
        }

        return whereClause.toString();
    }

    /**
     * Parses a numeric value from a given string.
     * If the string represents a valid number, it returns the parsed {@code Double}.
     * Otherwise, it returns {@code null}.
     */
    private Double parseNumeric(String str) {
        if (str == null) {
            return null;
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void appendLabelsWhereClause(StringBuilder whereClause, String alias, String propertyName, String operator, String propertyValue, Double doubleValue) {
        String function = this.getLabelFunction(propertyName);

        if (propertyName.startsWith("#labelsStartNode")){
            alias = "startNode";
        }

        if (propertyName.startsWith("#labelsEndNode")){
            alias = "endNode";
        }

        boolean isQuantifier = QUANTIFIERS.contains(function);

        whereClause
            .append(function)
            .append("(");

        if (isQuantifier){
            whereClause.append("label IN ");
        }

        whereClause
            .append("labels(")
            .append(alias)
            .append(")");

        if (isQuantifier){
            whereClause.append(" WHERE label ");
        } else {
            whereClause.append(")");
        }

        operator = OPERATORS.get(operator);

        appendOperator(whereClause, operator);
        appendPropertyValue(whereClause, propertyValue, operator, doubleValue);

        if (isQuantifier) {
            whereClause.append(")");
        }
    }

    /**
     * Retrieves the Neo4j function associated with the specified property name.
     *
     * @param propertyName The name of the property for which the function is retrieved.
     * @return A {@link String} representing the corresponding Neo4j function name.
     * @throws InvalidParameterException If no function is mapped to the given property name.
     */
    private String getLabelFunction(String propertyName) {
        String function = NODE_LABEL_FUNCTIONS.get(propertyName);

        if (function == null) {
            function = RELATIONSHIP_LABEL_FUNCTIONS.get(propertyName);
        }

        if (function == null) {
            throw new InvalidParameterException("No function mapped for given property name.");
        }

        return function;
    }

    /**
     * Defines a mapping of node labels with functions to Cypher functions.
     *
     * @return A {@link Map} containing functions mappings.
     */
    private static Map<String, String> defineNodeLabelFunctions() {
        final var functions = new TreeMap<String, String>();
        functions.put("#labels - SIZE", "SIZE");
        functions.put("#labels - ANY", "ANY");
        functions.put("#labels - ALL", "ALL");
        functions.put("#labels - NONE", "NONE");
        functions.put("#labels - SINGLE", "SINGLE");

        return functions;
    }

    /**
     * A {@link Map} of functions that can be used on Neo4j nodes.
     */
    private static final Map<String, String> NODE_LABEL_FUNCTIONS = defineNodeLabelFunctions();

    /**
     * Defines a mapping of start end end node labels with functions to Cypher functions.
     *
     * @return A {@link Map} containing functions mappings.
     */
    private static Map<String, String> defineRelationshipLabelFunctions() {
        final var functions = new TreeMap<String, String>();
        functions.put("#labelsStartNode - SIZE", "SIZE");
        functions.put("#labelsStartNode - ANY", "ANY");
        functions.put("#labelsStartNode - ALL", "ALL");
        functions.put("#labelsStartNode - NONE", "NONE");
        functions.put("#labelsStartNode - SINGLE", "SINGLE");

        functions.put("#labelsEndNode - SIZE", "SIZE");
        functions.put("#labelsEndNode - ANY", "ANY");
        functions.put("#labelsEndNode - ALL", "ALL");
        functions.put("#labelsEndNode - NONE", "NONE");
        functions.put("#labelsEndNode - SINGLE", "SINGLE");

        return functions;
    }

    /**
     * A {@link Map} of functions that can be used on Neo4j relationships.
     */
    private static final Map<String, String> RELATIONSHIP_LABEL_FUNCTIONS = defineRelationshipLabelFunctions();

    private static void appendIdPropertyName(StringBuilder whereClause, String alias, String propertyName) {
        boolean startNodeId = propertyName.equals("startNodeId");
        boolean endNodeId = propertyName.equals("endNodeId");

        whereClause.append("elementId(");

        if (startNodeId) {
            whereClause.append("startNode(");
        } else if (endNodeId) {
            whereClause.append("endNode(");
        }

        whereClause
            .append(alias);

        if (startNodeId || endNodeId)
            whereClause.append(")");

        whereClause
            .append(") ");
    }

    private static void appendPropertyName(StringBuilder whereClause, String alias, String propertyName, Double doubleValue) {
        if (propertyName.startsWith("#")) {
            propertyName = propertyName.substring(1); // Remove '#' prefix
            appendIdPropertyName(whereClause, alias, propertyName);

            return;
        }

        if (doubleValue != null) {
            whereClause.append("toFloat(");
        }

        if (alias != null && !propertyName.contains("startNode.") && !propertyName.contains("endNode.")) {
            whereClause.append(alias)
                .append(".");
        }
        whereClause.append(propertyName);

        if (doubleValue != null) {
            whereClause.append(")");
        }
    }

    private static void appendOperator(StringBuilder whereClause, String operator) {
        whereClause
            .append(" ")
            .append(operator)
            .append(" ");
    }

    private static void appendPropertyValue(StringBuilder whereClause, String propertyValue, String operator, Double doubleValue) {
        if (operator.equals("IN")) {
            whereClause
                .append("[")
                .append(propertyValue)
                .append("]");
        } else if (!UNARY_OPERATORS.contains(operator)) {
            if (doubleValue != null && !STRING_OPERATORS.contains(operator)) {
                whereClause
                    .append(doubleValue);
            } else {
                whereClause
                    .append("'")
                    .append(propertyValue)
                    .append("'");
            }
        }
    }

    private String getOffsetAndLimit(KindNameQuery query) {
        String output = "";
        if (query.hasOffset())
            output += " SKIP " + query.getOffset();
        if (query.hasLimit())
            output += " LIMIT " + query.getLimit();

        return output;
    }

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        try {
            return innerPullForest(path, query);
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    private ForestOfRecords innerPullForest(ComplexProperty path, QueryContent query) {
        replacedNames = path.copyWithoutDynamicNames().replacedNames();

        final @Nullable ComplexProperty fromNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX);
        final @Nullable ComplexProperty toNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
        final boolean isRelationship = fromNodeSubpath != null && toNodeSubpath != null;

        return isRelationship
            ? pullRelationshipPath(path, fromNodeSubpath, toNodeSubpath, query)
            : pullNodePath(path, query);
    }

    private static @Nullable ComplexProperty findSubpathByPrefix(ComplexProperty path, String namePrefix) {
        for (final var subpath : path.subpaths()) {
            if (
                (subpath.name() instanceof final StaticName staticName)
                && staticName.getStringName().startsWith(namePrefix)
                && subpath instanceof final ComplexProperty complexSubpath
            )
                return complexSubpath;
        }

        return null;
    }

    private ForestOfRecords pullRelationshipPath(ComplexProperty path, ComplexProperty fromNodeSubpath, ComplexProperty toNodeSubpath, QueryContent query) {
        final var forest = new ForestOfRecords();

        try (
            Session session = provider.getSession();
        ) {
            session
                .executeRead(tx -> {
                    final var queryCommand = new Query(createRelationshipQueryString(query));

                    return tx
                        .run(queryCommand)
                        .stream()
                        .map(result -> {
                            final var rootRecord = new RootRecord();
                            addValuePropertiesToRecord(rootRecord, path, result.get("relationship"));

                            final var fromNodeRecord = rootRecord.addComplexRecord(fromNodeSubpath.signature());
                            addValuePropertiesToRecord(fromNodeRecord, fromNodeSubpath, result.get("from_node"));

                            final var toNodeRecord = rootRecord.addComplexRecord(toNodeSubpath.signature());
                            addValuePropertiesToRecord(toNodeRecord, toNodeSubpath, result.get("to_node"));

                            return rootRecord;
                        })
                        .toList();
                })
                .forEach(forest::addRecord);
        }

        return forest;
    }

    private ForestOfRecords pullNodePath(ComplexProperty path, QueryContent query) {
        final var forest = new ForestOfRecords();

        try (
            Session session = provider.getSession();
        ) {
            session
                .executeRead(tx -> {
                    final var queryCommand = new Query(createNodeQueryString(query));

                    return tx
                        .run(queryCommand)
                        .stream()
                        .map(result -> {
                            final var rootRecord = new RootRecord();
                            addValuePropertiesToRecord(rootRecord, path, result.get("node"));
                            return rootRecord;
                        })
                        .toList();
                })
                .forEach(forest::addRecord);
        }

        return forest;
    }

    private void addValuePropertiesToRecord(ComplexRecord record, ComplexProperty path, Value parentValue) {
        for (final String key : parentValue.keys()) {
            final Value value = parentValue.get(key);

            if (value.isNull())
                continue;

            final var property = path.findSubpathByName(key);
            if (property == null)
                continue;

            if (!(property.name() instanceof final DynamicName dynamicName)) {
                record.addSimpleRecord(property.signature(), value.asString());
                continue;
            }

            final var replacement = replacedNames.get(dynamicName);
            final var replacer = record.addDynamicReplacer(replacement.prefix(), replacement.name(), key);
            replacer.addSimpleRecord(replacement.value().signature(), value.asString());
        }
    }

    public String readNodeAsStringForTests(String kindName) {
        try (
            Session session = provider.getSession();
        ) {
            /*
            final var results = session.executeRead(tx -> {
                final var query = new Query("MATCH (a)-[r]->(b) RETURN a, r, b;");

                return tx.run(query).stream().map(edge -> {
                    return nodeToString(edge.get("a"))
                        + "\n" + relationToString(edge.get("r"))
                        + "\n" + nodeToString(edge.get("b"))
                        + "\n\n";
                }).toList();
            });
            */

            final var results = session.executeRead(tx -> {
                final var query = new Query("MATCH (a:" + kindName + ") RETURN a;");

                return tx.run(query).stream().map(node -> {
                    return nodeToString(node.get("a"));
                }).toList();
            });

            return String.join("\n", results);
        }
    }

    private void addProperties(Value value, StringBuilder builder) {
        value.asMap().forEach((key, property) -> {
            builder
                .append("    ")
                .append(key)
                .append(": ")
                .append(property)
                .append("\n");
        });
    }

    private String nodeToString(Value node) {
        final var output = new StringBuilder();
        output
            .append(node.asNode().elementId().split(":")[2])
            .append(": (\n");

        for (final var label : node.asNode().labels())
            output.append("    :").append(label).append("\n");

        addProperties(node, output);

        output.append(")");

        return output.toString();
    }

    private String relationToString(Value relation) {
        final var output = new StringBuilder();
        output
            .append(relation.asRelationship().elementId().split(":")[2])
            .append(":")
            .append(relation.asRelationship().type())
            .append(": [\n");

        addProperties(relation, output);

        output.append("]");

        return output.toString();
    }

    @Override public QueryResult executeQuery(QueryStatement statement) {
        // TODO
        throw new UnsupportedOperationException("Neo4jPullWrapper.executeQuery not implemented.");
    }

    /**
     * Retrieves a list of distinct kind names (labels and relationship types).
     *
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @return A {@link KindNamesResponse} containing the list of kind names.
     * @throws PullForestException if an error occurs during database access.
     */
    @Override public KindNamesResponse getKindNames(String limit, String offset) {
        try (Session session = provider.getSession()) {
            List<String> data = new ArrayList<>();

            Result labelQueryResult = session.run("MATCH (n) UNWIND labels(n) AS label RETURN DISTINCT label SKIP " + offset + " LIMIT " + limit + ";");
            while (labelQueryResult.hasNext()) {
                Record labelQueryRecord = labelQueryResult.next();
                data.add(labelQueryRecord.get("label").asString());
            }

            Result typeQueryResult = session.run("MATCH (n)-[r]-(m) UNWIND type(r) AS relationshipType RETURN DISTINCT relationshipType SKIP " + offset + " LIMIT " + limit + ";");
            while (typeQueryResult.hasNext()) {
                Record typeQueryRecord = typeQueryResult.next();
                data.add(typeQueryRecord.get("relationshipType").asString());
            }

            return new KindNamesResponse(data);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Retrieves data of the specified kind from the graph.
     *
     * @param kindName The name of the kind.
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @param filters The filters to apply (optional).
     * @return A {@link GraphResponse} containing the data and metadata.
     * @throws PullForestException if an error occurs during query execution.
     */
    @Override public GraphResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filters) {
        KindNameQuery kindNameQuery = new KindNameQuery(kindName, Integer.parseInt(limit), Integer.parseInt(offset));

        if (filters == null){
            return getQueryResult(kindNameQuery);
        }

        return getQueryResult(new KindNameFilterQuery(kindNameQuery, filters));
    }

    /**
     * Retrieves a list of references for a specified kind.
     *
     * @param datasourceId ID of the datasource.
     * @param kindName     The name of the kind.
     */
    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        try (Session session = provider.getSession()) {
            List<Reference> references = new ArrayList<>();

            Result result = session.run("MATCH (a)-[r]->(b) RETURN DISTINCT labels(a) as startNodeLabels, type(r) as relationshipType, labels(b) as endNodeLabels;");
            while (result.hasNext()) {
                Record reference = result.next();
                String relationshipType = reference.get("relationshipType").asString();

                List<String> startNodeLabels = reference.get("startNodeLabels").asList(Value::asString);

                for (String startNodeLabel: startNodeLabels) {
                    references.add(new Reference(new ReferenceKind(datasourceId, relationshipType, "#startNodeId"), new ReferenceKind(datasourceId, startNodeLabel, "#elementId")));
                }

                List<String> endNodeLabels = reference.get("endNodeLabels").asList(Value::asString);

                for (String endNodeLabel: endNodeLabels) {
                    references.add(new Reference(new ReferenceKind(datasourceId, relationshipType, "#endNodeId"), new ReferenceKind(datasourceId, endNodeLabel, "#elementId")));
                }
            }

            return references;
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Retrieves the result of the given query.
     *
     * @param query the custom query.
     * @return a {@link GraphResponse} containing the data result of custom query.
     */
    @Override public GraphResponse getQueryResult(QueryContent query) {
        try (Session session = provider.getSession()) {
            List<String> nodePropertyNames = new ArrayList<>();
            List<String> relationshipPropertyNames = new ArrayList<>();

            GraphData data = session.executeRead(tx -> {
                Query finalQuery = new Query(getQueryString(query));

                List<GraphNode> nodes = new ArrayList<>();
                List<GraphRelationship> relationships = new ArrayList<>();

                tx.run(finalQuery).stream()
                    .flatMap(rec -> rec.values().stream())
                    .forEach(element -> {
                        if (element.hasType(TypeSystem.getDefault().NODE())) {
                            nodes.add(Neo4jUtils.getNodeProperties(element, nodePropertyNames));
                        } else if (element.hasType(TypeSystem.getDefault().RELATIONSHIP())) {
                            relationships.add(Neo4jUtils.getRelationshipProperties(element, relationshipPropertyNames));
                        }
                    });

                return new GraphData(nodes, relationships);
            });

            int itemCount = data.relationships().isEmpty() ? data.nodes().size() : data.relationships().size();
            List<String> propertyNames = data.relationships().isEmpty() ? nodePropertyNames : relationshipPropertyNames;

            return new GraphResponse(data, itemCount, propertyNames);
        } catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private String getQueryString(QueryContent query) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (query instanceof KindNameFilterQuery knfQuery) {
            String kindName = knfQuery.kindNameQuery.kindName;

            if (kindName.isEmpty() || !kindName.equals(kindName.toUpperCase()))
                return createNodeQueryString(query);

            return createRelationshipQueryString(query);
        }

        if (query instanceof KindNameQuery knQuery){
            String kindName = knQuery.kindName;

            if (kindName.isEmpty() || !kindName.equals(kindName.toUpperCase()))
                return createNodeQueryString(query);

            return createRelationshipQueryString(query);
        }

        throw PullForestException.invalidQuery(this, query);
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
     *
     * @return A {@link Map} of operator names to Neo4j operators.
     */
    private static final Map<String, String> OPERATORS = defineOperators();

    /**
     * A list of Neo4j unary operators.
     *
     * @return A {@link List} of Neo4j unary operators.
     */
    private static final List<String> UNARY_OPERATORS = Arrays.asList("IS NULL", "IS NOT NULL");

    /**
     * A list of Neo4j operators used with string values.
     *
     * @return A {@link List} of Neo4j operators used with string values.
     */
    private static final List<String> STRING_OPERATORS = Arrays.asList("=~", "STARTS WITH", "ENDS WITH", "CONTAINS");

    /**
     * A list of Neo4j quantifiers.
     *
     * @return A {@link List} of Neo4j quantifiers.
     */
    private static final List<String> QUANTIFIERS = Arrays.asList("ANY", "ALL", "NONE", "SINGLE");

}
