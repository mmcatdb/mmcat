package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.GraphResponse;
import cz.matfyz.core.adminer.GraphResponse.GraphElement;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.adminer.ForeignKey;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import org.checkerframework.checker.nullness.qual.Nullable;

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

    private ForestOfRecords innerPullForest(ComplexProperty path, QueryContent query) {
        final var fromNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX);
        final var toNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
        final boolean isRelationship = fromNodeSubpath != null && toNodeSubpath != null;

        return isRelationship
            ? pullRelationshipPath(path, fromNodeSubpath, toNodeSubpath, query)
            : pullNodePath(path, query);
    }

    private static ComplexProperty findSubpathByPrefix(ComplexProperty path, String namePrefix) {
        final var foundSubpath = path.subpaths().stream().filter(subpath -> {
            if (!(subpath.name() instanceof StaticName staticName))
                return false;

            return staticName.getStringName().startsWith(namePrefix);
        })
            .findFirst();

        if (foundSubpath.isEmpty())
            return null;

        return foundSubpath.get() instanceof ComplexProperty complexSubpath
            ? complexSubpath
            : null;
    }

    private ForestOfRecords pullRelationshipPath(ComplexProperty path, ComplexProperty fromNodeSubpath, ComplexProperty toNodeSubpath, QueryContent query) {
        final var fromNodeRecordName = ((StaticName) fromNodeSubpath.name()).toRecordName();
        final var toNodeRecordName = ((StaticName) toNodeSubpath.name()).toRecordName();

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
                            addValuePropertiesToRecord(result.get("relationship"), path, rootRecord);

                            final var fromNodeRecord = rootRecord.addComplexRecord(fromNodeRecordName, fromNodeSubpath.signature());
                            addValuePropertiesToRecord(result.get("from_node"), fromNodeSubpath, fromNodeRecord);

                            final var toNodeRecord = rootRecord.addComplexRecord(toNodeRecordName, toNodeSubpath.signature());
                            addValuePropertiesToRecord(result.get("to_node"), toNodeSubpath, toNodeRecord);

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
                            addValuePropertiesToRecord(result.get("node"), path, rootRecord);
                            return rootRecord;
                        })
                        .toList();
                })
                .forEach(forest::addRecord);
        }

        return forest;
    }

    private void addValuePropertiesToRecord(Value value, ComplexProperty path, ComplexRecord complexRecord) {
        for (final AccessPath subpath : path.subpaths()) {
            if (
                !(subpath instanceof SimpleProperty simpleProperty)
                    || !(simpleProperty.name() instanceof StaticName staticName)
            )
                continue;

            final String name = staticName.getStringName();
            final String stringValue = value.get(name).asString();
            complexRecord.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.signature(), stringValue);
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

    private GraphElement getNodeProperties(Value node) {
        String id = node.asNode().elementId().split(":")[2];

        Map<String, Object> properties = new HashMap<>();
        node.asNode().asMap().forEach((key, value) -> {
            properties.put(key, value);
        });

        List<String> labels = new ArrayList<>();
        for (final var label : node.asNode().labels()) {
            labels.add(label);
        }
        properties.put("labels", labels);

        return new GraphElement(id, properties);
    }

    private GraphElement getRelationshipProperties(Value relationship) {
        String id = relationship.asRelationship().elementId().split(":")[2];

        Map<String, Object> properties = new HashMap<>();
        properties.put("type", relationship.asRelationship().type());

        // Add IDs of start and end node
        properties.put("startNodeId", relationship.asRelationship().startNodeElementId().split(":")[2]);
        properties.put("endNodeId", relationship.asRelationship().endNodeElementId().split(":")[2]);

        relationship.asRelationship().asMap().forEach((key, value) -> {
            properties.put(key, value);
        });

        return new GraphElement(id, properties);
    }

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

    private Set<String> getKeyNames(Session session, String matchClause) {
        Set<String> keys = new HashSet<>();

        Result queryResult = session.run(String.format("""
            %s
            UNWIND keys(a) AS key
            RETURN DISTINCT key
            ORDER BY key;
            """, matchClause));
        while (queryResult.hasNext()) {
            Record queryRecord = queryResult.next();
            keys.add(queryRecord.get("key").asString());
        }

        return keys;
    }

    private Set<String> getNodeKeyNames(Session session) {
        return getKeyNames(session, "MATCH (a)");
    }

    private Set<String> getRelationshipKeyNames(Session session) {
        return getKeyNames(session, "MATCH ()-[a]->()");
    }

    private String createWhereClause(List<AdminerFilter> filters, String name) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            if (i == 0) {
                whereClause.append(name).append(".").append(filter.columnName()).append(" ").append(filter.operator()).append(" '").append(filter.columnValue()).append("'");
            } else {
                whereClause.append(" AND ").append(name).append(".").append(filter.columnName()).append(" ").append(filter.operator()).append(" '").append(filter.columnValue()).append("'");
            }
        }

        return whereClause.toString();
    }

    private GraphResponse getNode(Session session, String queryBase, List<AdminerFilter> filters, String limit, String offset) {
        String whereClause = createWhereClause(filters, "a");
        List<GraphElement> data = session.executeRead(tx -> {
            var query = new Query(queryBase + whereClause + " RETURN a SKIP " + offset + " LIMIT " + limit + ";");

            return tx.run(query).stream()
                .map(node -> getNodeProperties(node.get("a")))
                .toList();
        });

        Result countQueryResult = session.run(queryBase + " RETURN COUNT(a) AS recordCount;");
        int itemCount = countQueryResult.next().get("recordCount").asInt();

        Set<String> keys = getNodeKeyNames(session);

        return new GraphResponse(data, itemCount, keys);
    }

    private GraphResponse getRelationship(Session session, List<AdminerFilter> filters, String limit, String offset) {
        String whereClause = createWhereClause(filters, "r");
        List<GraphElement> data = session.executeRead(tx -> {
            var query = new Query("MATCH ()-[r]->() " + whereClause + " RETURN r SKIP " + offset + " LIMIT " + limit + ";");

            return tx.run(query).stream()
                .map(relation -> getRelationshipProperties(relation.get("r")))
                .toList();
        });

        Result countQueryResult = session.run("MATCH ()-[r]->() " + whereClause + " RETURN COUNT(r) AS recordCount;");
        int itemCount = countQueryResult.next().get("recordCount").asInt();

        Set<String> keys = getRelationshipKeyNames(session);

        return new GraphResponse(data, itemCount, keys);
    }

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

    @Override public List<ForeignKey> getForeignKeys(String kindName) {
        throw new UnsupportedOperationException("Neo4jPullWrapper.getForeignKeys not implemented.");
    }

}
