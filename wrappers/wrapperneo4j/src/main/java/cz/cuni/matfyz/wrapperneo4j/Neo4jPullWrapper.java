package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.PullForestException;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.record.ComplexRecord;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

/**
 * @author jachymb.bartik
 */
public class Neo4jPullWrapper implements AbstractPullWrapper {

    private SessionProvider sessionProvider;
    
    public Neo4jPullWrapper(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    private String createRelationshipQueryString(PullQuery query) {
        if (query.hasStringContent())
            return query.getStringContent();

        return "MATCH (from_node)-[relationship: " + query.getKindName() + "]->(to_node) RETURN from_node, relationship, to_node" + getOffsetAndLimit(query) + ";";
    }

    private String createNodeQueryString(PullQuery query) {
        if (query.hasStringContent())
            return query.getStringContent();

        return "MATCH (node: " + query.getKindName() + ") RETURN node" + getOffsetAndLimit(query) + ";";
    }

    private String getOffsetAndLimit(PullQuery query) {
        String output = "";
        if (query.hasOffset())
            output += " SKIP " + query.getOffset();
        
        if (query.hasLimit())
            output += " LIMIT " + query.getLimit();

        return output;
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, PullQuery query) throws PullForestException {
        try {
            return innerPullForest(path, query);
        }
        catch (Exception e) {
            throw new PullForestException(e);
        }
    }

    private ForestOfRecords innerPullForest(ComplexProperty path, PullQuery query) {
        final var relationshipResult = tryProcessRelationshipPath(path, query);
        if (relationshipResult != null)
            return relationshipResult;

        return processNodePath(path, query);
    }

    private ForestOfRecords tryProcessRelationshipPath(ComplexProperty path, PullQuery query) {
        final var fromNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX);
        final var toNodeSubpath = findSubpathByPrefix(path, Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);

        if (fromNodeSubpath == null || toNodeSubpath == null)
            return null;

        return processRelationshipPath(path, fromNodeSubpath, toNodeSubpath, query);
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

    private ForestOfRecords processRelationshipPath(ComplexProperty path, ComplexProperty fromNodeSubpath, ComplexProperty toNodeSubpath, PullQuery query) {
        final var fromNodeRecordName = ((StaticName) fromNodeSubpath.name()).toRecordName();
        final var toNodeRecordName = ((StaticName) toNodeSubpath.name()).toRecordName();

        final var forest = new ForestOfRecords();

        try (
            final Session session = sessionProvider.getSession();
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

    private ForestOfRecords processNodePath(ComplexProperty path, PullQuery query) {
        final var forest = new ForestOfRecords();

        try (
            final Session session = sessionProvider.getSession();
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

    public String readAllAsStringForTests() {
        try (
            final Session session = sessionProvider.getSession();
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
                final var query = new Query("MATCH (a: order) RETURN a;");

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

}