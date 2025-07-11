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
import cz.matfyz.core.adminer.Reference.ReferenceKind;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.StringName;
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

    private static final String RELATIONSHIPS_COUNT = "COUNT(relationship) as recordCount";
    private static final String NODES_COUNT = "COUNT(node) as recordCount";

    /**
     * A {@link List} of Neo4j quantifiers.
     */
    private static final List<String> QUANTIFIERS = Arrays.asList("ANY", "ALL", "NONE", "SINGLE");

    /**
     * A {@link Map} of operator names to Neo4j operators.
     */
    private static final Map<String, String> OPERATORS = defineOperators();

    /**
     * A {@link List} of Neo4j unary operators.
     */
    private static final List<String> UNARY_OPERATORS = Arrays.asList("IS NULL", "IS NOT NULL");

    /**
     * A {@link List} of Neo4j operators used with string values.
     */
    private static final List<String> STRING_OPERATORS = Arrays.asList("=~", "STARTS WITH", "ENDS WITH", "CONTAINS");

    public Neo4jPullWrapper(Neo4jProvider provider) {
        this.provider = provider;
    }

    private String createRelationshipQueryString(QueryContent query, boolean countQuery) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (query instanceof KindNameFilterQuery knfQuery) {
            String returnQueryPart = countQuery ? RELATIONSHIPS_COUNT : "from_node, relationship, to_node" + getOffsetAndLimit(knfQuery.kindNameQuery);
            List<String> whereConditions = createWhereConditions(knfQuery.getFilters(), "relationship");

            return "MATCH (from_node)-[relationship: " + knfQuery.kindNameQuery.kindName + "]->(to_node) " + createWhereClause(whereConditions) + " RETURN " + returnQueryPart + ";";
        }

        if (query instanceof KindNameQuery knQuery) {
            String returnQueryPart = countQuery ? RELATIONSHIPS_COUNT : "from_node, relationship, to_node" + getOffsetAndLimit(knQuery);

            return "MATCH (from_node)-[relationship: " + knQuery.kindName + "]->(to_node) RETURN " + returnQueryPart + ";";
        }

        throw PullForestException.invalidQuery(this, query);
    }

    private String createNodeQueryString(QueryContent query, boolean countQuery) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (!(query instanceof KindNameFilterQuery) && !(query instanceof KindNameQuery))
            throw PullForestException.invalidQuery(this, query);

        KindNameQuery knQuery = query instanceof KindNameFilterQuery knfQuery ? knfQuery.kindNameQuery : (KindNameQuery) query;

        String basePart = knQuery.kindName.isEmpty() ? "MATCH (node)" : "MATCH (node:" + knQuery.kindName + ")";

        List<String> whereConditions = new ArrayList<>();
        if (query instanceof KindNameFilterQuery knfQuery)
            whereConditions = createWhereConditions(knfQuery.getFilters(), "node");
        if (knQuery.kindName.isEmpty())
            whereConditions.add("size(labels(node)) = 0");
        String wherePart = createWhereClause(whereConditions);

        String returnPart = countQuery ? NODES_COUNT : "node" + getOffsetAndLimit(knQuery);

        return basePart + " " + wherePart + " RETURN " + returnPart + ";";
    }

    private String createWhereClause(List<String> conditions) {
        if (conditions.size() == 0)
            return "";

        return "WHERE " + String.join(" AND ", conditions);
    }

    /**
     * Constructs a WHERE clause based on a list of filters.
     *
     * @param alias The alias assigned to the graph element in the query.
     */
    private List<String> createWhereConditions(List<AdminerFilter> filters, String alias) {
        List<String> conditions = new ArrayList<>();

        for (AdminerFilter filter : filters) {
            StringBuilder condition = new StringBuilder();

            String propertyName = filter.propertyName();
            Double doubleValue = this.parseNumeric(filter.propertyValue());

            if (propertyName.contains(Neo4jUtils.LABELS)) {
                this.appendLabelsWhereClause(condition, alias, propertyName, filter.operator(), filter.propertyValue(), doubleValue);
                continue;
            }

            appendPropertyName(condition, alias, propertyName, doubleValue);

            String operator = OPERATORS.get(filter.operator());
            appendOperator(condition, operator);

            appendPropertyValue(condition, filter.propertyValue(), operator, doubleValue);

            conditions.add(condition.toString());
        }

        return conditions;
    }

    /**
     * Parses a numeric value from a given string.
     *
     * @return the parsed {@code Double} value if valid, or {@code null} if the input is {@code null} or not a valid number
     */
    private Double parseNumeric(String str) {
        if (str == null)
            return null;

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void appendLabelsWhereClause(StringBuilder whereClause, String alias, String propertyName, String operator, String propertyValue, Double doubleValue) {
        String function = this.getLabelFunction(propertyName);

        if (propertyName.startsWith(Neo4jUtils.FROM_NODE_PREFIX + Neo4jUtils.LABELS))
            alias = "from_node";

        if (propertyName.startsWith(Neo4jUtils.TO_NODE_PREFIX + Neo4jUtils.LABELS))
            alias = "to_node";

        boolean isQuantifier = QUANTIFIERS.contains(function);

        whereClause
            .append(function)
            .append("(");

        if (isQuantifier)
            whereClause.append("label IN ");

        whereClause
            .append("labels(")
            .append(alias)
            .append(")");

        if (isQuantifier)
            whereClause.append(" WHERE label ");
        else
            whereClause.append(")");

        operator = OPERATORS.get(operator);

        appendOperator(whereClause, operator);
        appendPropertyValue(whereClause, propertyValue, operator, doubleValue);

        if (isQuantifier)
            whereClause.append(")");
    }

    /**
     * Retrieves the Neo4j function associated with the specified property name.
     */
    private String getLabelFunction(String propertyName) {
        String nodeFunction = Neo4jUtils.NODE_LABEL_FUNCTIONS.get(propertyName);
        String function = nodeFunction != null ? nodeFunction : Neo4jUtils.RELATIONSHIP_LABEL_FUNCTIONS.get(propertyName);

        if (function == null)
            throw new InvalidParameterException("No function mapped for given property name.");

        return function;
    }

    private static void appendPropertyName(StringBuilder whereClause, String alias, String propertyName, Double doubleValue) {
        if (propertyName.contains(Neo4jUtils.ID)) {
            appendIdPropertyName(whereClause, alias, propertyName);
            return;
        }

        if (doubleValue != null)
            whereClause.append("toFloat(");

        if (alias != null
            && !propertyName.contains(Neo4jUtils.FROM_NODE_PREFIX)
            && !propertyName.contains(Neo4jUtils.TO_NODE_PREFIX)) {
            whereClause.append(alias)
                .append(".");
        }

        if (propertyName.startsWith(Neo4jUtils.FROM_NODE_PREFIX)) {
            whereClause.append("from_node.")
                .append(propertyName.substring(Neo4jUtils.FROM_NODE_PREFIX.length()));
        }
        else if (propertyName.startsWith(Neo4jUtils.TO_NODE_PREFIX)) {
            whereClause.append("to_node.")
                .append(propertyName.substring(Neo4jUtils.TO_NODE_PREFIX.length()));
        }
        else {
            whereClause.append(propertyName);
        }

        if (doubleValue != null)
            whereClause.append(")");
    }

    private static void appendIdPropertyName(StringBuilder whereClause, String alias, String propertyName) {
        boolean startNodeId = propertyName.equals(Neo4jUtils.FROM_NODE_PREFIX + Neo4jUtils.ID);
        boolean endNodeId = propertyName.equals(Neo4jUtils.TO_NODE_PREFIX + Neo4jUtils.ID);

        whereClause.append("elementId(");

        if (startNodeId)
            whereClause.append("startNode(");
        else if (endNodeId)
            whereClause.append("endNode(");

        whereClause.append(alias);

        if (startNodeId || endNodeId)
            whereClause.append(")");

        whereClause.append(") ");
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
                whereClause.append(doubleValue);
            } else {
                whereClause
                    .append("'")
                    .append(propertyValue)
                    .append("'");
            }
        }
    }

    /**
     * Defines a mapping of comparison operators to their Cypher equivalents.
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
            throw PullForestException.inner(e);
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
                subpath.name() instanceof final StringName stringName
                && stringName.value.startsWith(namePrefix)
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
                    final var queryCommand = new Query(createRelationshipQueryString(query, false));

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
                    final var queryCommand = new Query(createNodeQueryString(query, false));

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
        // TODO: Neo4J might be able to return nested results, but this implementation so far covers only flat relations.
        final var columns = statement.structure().children().stream().map(c -> c.name).toList();
        final var builder = new ListResult.TableBuilder();
        builder.addColumns(columns);

        try (
            Session session = provider.getSession()
        ) {
            session.executeRead(tx -> {
                final var query = new Query(statement.content().toString());
                tx.run(query).forEachRemaining(result -> {
                    final var row = new ArrayList<String>();
                    for (final var column : columns) {
                        final var columnValue = result.get(column).asString();
                        row.add(columnValue);
                    }

                    builder.addRow(row);
                });
                return null;
            });

            return new QueryResult(builder.build(), statement.structure());
        } catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    @Override public List<String> getKindNames() {
        try (
            Session session = provider.getSession()
        ) {
            final List<String> output = new ArrayList<>();

            final Result labelQueryResult = session.run("MATCH (n) UNWIND labels(n) AS label RETURN DISTINCT label;");
            while (labelQueryResult.hasNext()) {
                final Record labelQueryRecord = labelQueryResult.next();
                output.add(labelQueryRecord.get("label").asString());
            }

            final Result typeQueryResult = session.run("MATCH (n)-[r]-(m) UNWIND type(r) AS relationshipType RETURN DISTINCT relationshipType;");
            while (typeQueryResult.hasNext()) {
                final Record typeQueryRecord = typeQueryResult.next();
                output.add(typeQueryRecord.get("relationshipType").asString());
            }

            return output;
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    @Override public GraphResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filters) {
        KindNameQuery kindNameQuery = new KindNameQuery(kindName, limit, offset);
        if (filters == null)
            return getQueryResult(kindNameQuery);

        return getQueryResult(new KindNameFilterQuery(kindNameQuery, filters));
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        try (
            Session session = provider.getSession()
        ) {
            List<Reference> references = new ArrayList<>();

            Result result = session.run("MATCH (a)-[r]->(b) RETURN DISTINCT labels(a) as startNodeLabels, type(r) as relationshipType, labels(b) as endNodeLabels;");
            while (result.hasNext()) {
                Record reference = result.next();
                String relationshipType = reference.get("relationshipType").asString();

                List<String> startNodeLabels = reference.get("startNodeLabels").asList(Value::asString);

                for (String startNodeLabel: startNodeLabels)
                    references.add(new Reference(new ReferenceKind(datasourceId, relationshipType, Neo4jUtils.FROM_NODE_PREFIX + Neo4jUtils.ID), new ReferenceKind(datasourceId, startNodeLabel, Neo4jUtils.ID)));

                List<String> endNodeLabels = reference.get("endNodeLabels").asList(Value::asString);

                for (String endNodeLabel: endNodeLabels)
                    references.add(new Reference(new ReferenceKind(datasourceId, relationshipType, Neo4jUtils.TO_NODE_PREFIX + Neo4jUtils.ID), new ReferenceKind(datasourceId, endNodeLabel, Neo4jUtils.ID)));
            }

            return references;
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    @Override public GraphResponse getQueryResult(QueryContent query) {
        try (
            Session session = provider.getSession()
        ) {
            List<String> propertyNames = getPropertyNames(query, session);

            GraphData data = session.executeRead(tx -> {
                Query finalQuery = new Query(getQueryString(query, false));

                List<GraphNode> nodes = new ArrayList<>();
                List<GraphRelationship> relationships = new ArrayList<>();

                tx.run(finalQuery).stream()
                    .flatMap(rec -> rec.values().stream())
                    .forEach(element -> {
                        if (element.hasType(TypeSystem.getDefault().NODE()))
                            nodes.add(Neo4jUtils.getNodeProperties(element, propertyNames));
                        else if (element.hasType(TypeSystem.getDefault().RELATIONSHIP()))
                            relationships.add(Neo4jUtils.getRelationshipProperties(element, propertyNames));
                    });

                return new GraphData(nodes, relationships);
            });

            Result countQueryResult = session.run(new Query(getQueryString(query, true)));
            final long itemCount = query instanceof StringQuery
                ? data.relationships().isEmpty() ? data.nodes().size() : data.relationships().size()
                : countQueryResult.next().get("recordCount").asLong();

            return new GraphResponse(data, itemCount, propertyNames);
        } catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    private List<String> getPropertyNames(QueryContent query, Session session) {
        List<String> propertyNames = new ArrayList<>();
        Result propertyNamesQueryResult = session.run(getQueryString(query instanceof KindNameFilterQuery knfQuery ? knfQuery.kindNameQuery : query, false));

        while(propertyNamesQueryResult.hasNext()) {
            Record propertyNameRecord = propertyNamesQueryResult.next();
            propertyNameRecord.values().stream()
                .forEach(element -> {
                    if (element.hasType(TypeSystem.getDefault().NODE())) {
                        Neo4jUtils.getNodeProperties(element, propertyNames);
                    } else if (element.hasType(TypeSystem.getDefault().RELATIONSHIP())) {
                        Neo4jUtils.getRelationshipProperties(element, propertyNames);
                    }
                });
        }

        return propertyNames;
    }

    private String getQueryString(QueryContent query, boolean countQuery) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;
        if (query instanceof KindNameFilterQuery knfQuery)
            return createQueryString(knfQuery.kindNameQuery.kindName, query, countQuery);
        if (query instanceof KindNameQuery knQuery)
            return createQueryString(knQuery.kindName, query, countQuery);

        throw PullForestException.invalidQuery(this, query);
    }

    private String createQueryString(String kindName, QueryContent query, boolean countQuery) {
        if (kindName.isEmpty() || !kindName.equals(kindName.toUpperCase()))
            return createNodeQueryString(query, countQuery);

        return createRelationshipQueryString(query, countQuery);
    }

}
