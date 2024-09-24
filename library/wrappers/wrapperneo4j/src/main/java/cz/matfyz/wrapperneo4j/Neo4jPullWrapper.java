package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;

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

    private JSONObject nodeToJson(Value node) {
        try {
            JSONObject json = new JSONObject();

            // Add the element ID
            json.put("id", node.asNode().elementId().split(":")[2]);
            
            // Add labels
            JSONArray labelsArray = new JSONArray();
            for (final var label : node.asNode().labels()) {
                labelsArray.put(label);
            }
            json.put("labels", labelsArray);
    
            // Add properties
            JSONObject properties = new JSONObject();
            node.asNode().asMap().forEach((key, value) -> {
                try {
                    properties.put(key, value);
                } catch (JSONException e) {
                    throw QueryException.message("Error when getting properties.");
                }
            });
            json.put("properties", properties);
    
            return json;
        }
        catch (JSONException e){
            throw QueryException.message("Error when getting node data.");
        }
    }

    private JSONObject relationshipToJson(Value relationship) {
        try {
            JSONObject json = new JSONObject();

            // Add the element ID
            json.put("id", relationship.asRelationship().elementId().split(":")[2]);
            
            // Add type
            json.put("type", relationship.asRelationship().type());
            
            // Add start and end node IDs
            json.put("startNodeId", relationship.asRelationship().startNodeElementId().split(":")[2]);
            json.put("endNodeId", relationship.asRelationship().endNodeElementId().split(":")[2]);
            
            // Add properties
            JSONObject properties = new JSONObject();
            relationship.asRelationship().asMap().forEach((key, value) -> {
                try {
                    properties.put(key, value);
                } catch (JSONException e) {
                    throw QueryException.message("Error when getting properties.");
                }
            });
            json.put("properties", properties);

            return json; 
        }
        catch (JSONException e){
            throw QueryException.message("Error when getting relation data.");
        }
        
    }

    @Override public JSONArray getTableNames(String limit) {
        try {
            JSONArray result = new JSONArray();
            Result queryResult = provider.getSession().run("MATCH (n) UNWIND labels(n) AS label RETURN DISTINCT label LIMIT " + limit + ";");
            while (queryResult.hasNext()) {
                Record queryRecord = queryResult.next();
                result.put(queryRecord.get("label").asString());
            }

            return result; 
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        }
    }

    private JSONArray getNodes(String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH (a) RETURN a LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return nodeToJson(node.get("a"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result;
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        } 
    }

    private JSONArray getRelationships(String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH ()-[r]->() RETURN r LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return relationshipToJson(node.get("r"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result;  
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        }
    }

    private JSONArray getByLabel(String label, String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH (a:" + label + ") RETURN a LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return nodeToJson(node.get("a"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result;
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        }
    }

    @Override public JSONArray getTable(String tableName, String limit) {
        if (tableName.equals("nodes")) {
            return getNodes(limit);
        }
        
        if (tableName.equals("relationships")) {
            return getRelationships(limit);
        }

        return getByLabel(tableName, limit);
    }

    private JSONArray getNode(String id, String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH (a) WHERE id(a)="+ id +" RETURN a LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return nodeToJson(node.get("a"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result; 
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        }
    }

    private JSONArray getRelationship(String id, String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH ()-[r]->() WHERE id(r)=" + id + " RETURN r LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return relationshipToJson(node.get("r"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result; 
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        } 
    }

    private JSONArray getByLabelAndId(String label, String id, String limit) {
        try {
            JSONArray result = new JSONArray();

            var allNodes = provider.getSession().executeRead(tx -> {
                var query = new Query("MATCH (a:" + label + ") WHERE id(a)="+ id +" RETURN a LIMIT " + limit + ";");

                return tx.run(query).stream().map(node -> {
                    return nodeToJson(node.get("a"));
                }).toList();
            });
            allNodes.forEach(result::put);

            return result;
        }
        catch (Exception e){
            throw QueryException.message("Error when executing a Neo4j query.");
        }
    }

    @Override public JSONArray getRow(String tableName, String id, String limit) {
        if (tableName.equals("nodes")) {
            return getNode(id, limit);
        }
        
        if (tableName.equals("relationships")) {
            return getRelationship(id, limit);
        }

        return getByLabelAndId(tableName, id, limit);
    }

}
