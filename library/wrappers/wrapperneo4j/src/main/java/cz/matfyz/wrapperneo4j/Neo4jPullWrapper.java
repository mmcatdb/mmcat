package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Query;
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
        // TODO
        throw new UnsupportedOperationException("Neo4jPullWrapper.executeQuery not implemented.");
    }

}
