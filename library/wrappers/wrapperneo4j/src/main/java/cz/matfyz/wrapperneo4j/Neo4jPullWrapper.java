package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.GraphResponse;
import cz.matfyz.core.adminer.GraphResponse.GraphElement;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.inference.adminer.Neo4jAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

public class Neo4jPullWrapper implements AbstractPullWrapper {

    private Neo4jProvider provider;

    public Neo4jPullWrapper(Neo4jProvider provider) {
        this.provider = provider;
    }

    private String createRelationshipQueryString(QueryContent query) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (!(query instanceof KindNameQuery knQuery))
            throw PullForestException.invalidQuery(this, query);

        return "MATCH (from_node)-[relationship: " + knQuery.kindName + "]->(to_node) RETURN from_node, relationship, to_node" + getOffsetAndLimit(knQuery) + ";";
    }

    private String createNodeQueryString(QueryContent query) {
        if (query instanceof StringQuery stringQuery)
            return stringQuery.content;

        if (!(query instanceof KindNameQuery knQuery))
            throw PullForestException.invalidQuery(this, query);

        return "MATCH (node: " + knQuery.kindName + ") RETURN node" + getOffsetAndLimit(knQuery) + ";";
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
        // TODO: Neo4J might be able to return nested results, but this implementation so far covers only flat relations.
        final var columns = statement.structure().children().stream().map(c -> c.name).toList();
        final var builder = new ListResult.TableBuilder();
        builder.addColumns(columns);

        try (Session session = provider.getSession()) {
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
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Retrieves a list of distinct kind names (labels).
     *
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @return A {@link KindNameResponse} containing the list of kind names.
     * @throws PullForestException if an error occurs during database access.
     */
    @Override public KindNameResponse getKindNames(String limit, String offset) {
        try (Session session = provider.getSession()) {
            List<String> data = new ArrayList<>();

            Result queryResult = session.run("MATCH (n) UNWIND labels(n) AS label RETURN DISTINCT label SKIP " + offset + " LIMIT " + limit + ";");
            while (queryResult.hasNext()) {
                Record queryRecord = queryResult.next();
                data.add(queryRecord.get("label").asString());
            }

            return new KindNameResponse(data);
        }
        catch (Exception e){
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Constructs a Cypher WHERE clause based on a list of filters.
     *
     * @param filters The filters to apply.
     * @param name The alias for the graph element in the query ('a' for nodes and 'r' for relationships).
     * @return A Cypher WHERE clause as a {@link String}.
     */
    private String createWhereClause(List<AdminerFilter> filters, String name) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String operator = Neo4jAlgorithms.OPERATORS.get(filter.operator());

            if (i != 0) {
                whereClause.append(" AND ");
            }

            whereClause.append(name)
                .append(".")
                .append(filter.columnName())
                .append(" ")
                .append(operator);

            if (operator.equals("IN")) {
                whereClause
                    .append(" ")
                    .append(Arrays.stream(filter.columnValue().split(";"))
                        .map(String::trim)
                        .map(value -> "'" + value + "'")
                        .collect(Collectors.joining(", ", "[", "]")))
                    .append("");
            } else if (!Neo4jAlgorithms.UNARY_OPERATORS.contains(operator)) {
                whereClause
                    .append(" '")
                    .append(filter.columnValue())
                    .append("'");
            }
        }

        return whereClause.toString();
    }

    /**
     * Retrieves node data from the graph based on the specified query, filters, and pagination parameters.
     *
     * @param session The Neo4j session to use for the query.
     * @param queryBase The base Cypher query to match nodes.
     * @param filters The filters to apply.
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @return A {@link GraphResponse} containing the nodes and metadata.
     */
    private GraphResponse getNode(Session session, String queryBase, List<AdminerFilter> filters, String limit, String offset) {
        String whereClause = createWhereClause(filters, "a");

        List<GraphElement> data = session.executeRead(tx -> {
            var query = new Query(queryBase + whereClause + " RETURN a SKIP " + offset + " LIMIT " + limit + ";");

            return tx.run(query).stream()
                .map(node -> Neo4jAlgorithms.getNodeProperties(node.get("a")))
                .toList();
        });

        Result countQueryResult = session.run(queryBase + " RETURN COUNT(a) AS recordCount;");
        int itemCount = countQueryResult.next().get("recordCount").asInt();

        Set<String> properties = Neo4jAlgorithms.getNodePropertyNames(session);

        return new GraphResponse(data, itemCount, properties);
    }

    /**
     * Retrieves relationship data from the graph based on the specified filters and pagination parameters.
     *
     * @param session The Neo4j session to use for the query.
     * @param filters The filters to apply.
     * @param limit The maximum number of results to return.
     * @param offset The number of results to skip.
     * @return A {@link GraphResponse} containing the relationships and metadata.
     */
    private GraphResponse getRelationship(Session session, List<AdminerFilter> filters, String limit, String offset) {
        String whereClause = createWhereClause(filters, "r");
        List<GraphElement> data = session.executeRead(tx -> {
            var query = new Query("MATCH ()-[r]->() " + whereClause + " RETURN r SKIP " + offset + " LIMIT " + limit + ";");

            return tx.run(query).stream()
                .map(relation -> Neo4jAlgorithms.getRelationshipProperties(relation.get("r")))
                .toList();
        });

        Result countQueryResult = session.run("MATCH ()-[r]->() " + whereClause + " RETURN COUNT(r) AS recordCount;");
        int itemCount = countQueryResult.next().get("recordCount").asInt();

        Set<String> properties = Neo4jAlgorithms.getRelationshipPropertyNames(session);

        return new GraphResponse(data, itemCount, properties);
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
        try (Session session = provider.getSession()) {
            if (kindName.equals("relationships") || kindName.equals("unlabeled")) {
                return getRelationship(session, filters, limit, offset);
            }

            String queryBase = kindName.equals("nodes") ? "MATCH (a) " : "MATCH (a:" + kindName + ") ";
            return getNode(session, queryBase, filters, limit, offset);
        } catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Unsupported method for fetching foreign keys in Neo4j.
     *
     * @param datasourceId ID of the datasource.
     * @param kindName     The name of the kind.
     * @throws UnsupportedOperationException as this operation is not implemented.
     */
    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        // TODO
        throw new UnsupportedOperationException("Neo4jPullWrapper.getReferences not implemented.");
    }

}
