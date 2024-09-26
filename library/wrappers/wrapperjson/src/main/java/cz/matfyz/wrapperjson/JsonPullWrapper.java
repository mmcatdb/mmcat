package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RecordName;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.record.ComplexRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

public class JsonPullWrapper implements AbstractPullWrapper {

    private final JsonProvider provider;

    public JsonPullWrapper(JsonProvider provider) {
        this.provider = provider;
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        System.out.println("json pullwrapper");
        System.out.println("query: " + query);
        final var forest = new ForestOfRecords();

        try (InputStream inputStream = provider.getInputStream()) {
            processJsonStream(inputStream, forest, path);
        } catch (IOException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    private void processJsonStream(InputStream inputStream, ForestOfRecords forest, ComplexProperty path) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);
        ObjectMapper objectMapper = new ObjectMapper();

        //while (parser.nextToken() != JsonToken.END_ARRAY) {
        // TODO: check for the usulal structure of Json files - should there be an array or not
        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                System.out.println("current token in processJsonStream: " + parser.currentToken());
                JsonNode jsonNode = objectMapper.readTree(parser);
                RootRecord rootRecord = new RootRecord();
                getDataFromJsonNode(rootRecord, jsonNode, path);
                forest.addRecord(rootRecord);
            }
        }
    }

    public void getDataFromJsonNode(ComplexRecord parentRecord, JsonNode jsonNode, ComplexProperty path) {
        for (AccessPath subpath : path.subpaths()) {
            Name name = subpath.name();
            if (name instanceof StaticName) {
                StaticName staticName = (StaticName) name;
                String fieldName = staticName.getStringName();
                JsonNode valueNode = jsonNode.get(fieldName);

                if (valueNode != null) {
                    if (subpath instanceof ComplexProperty complexSubpath) {
                        if (valueNode.isObject()) {
                            ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexSubpath.name(), fieldName), complexSubpath.signature());
                            getDataFromJsonNode(childRecord, valueNode, complexSubpath);
                        } else if (valueNode.isArray()) {
                            handleJsonArray(parentRecord, valueNode, complexSubpath, fieldName);
                        }
                    } else if (subpath instanceof SimpleProperty simpleSubpath) {
                        handleSimpleProperty(parentRecord, valueNode, simpleSubpath, fieldName);
                    }
                }
            }
        }
    }

    private void handleJsonArray(ComplexRecord parentRecord, JsonNode arrayNode, ComplexProperty complexSubpath, String fieldName) {
        for (JsonNode itemNode : arrayNode) {
            if (itemNode.isObject()) {
                ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexSubpath.name(), fieldName), complexSubpath.signature());
                getDataFromJsonNode(childRecord, itemNode, complexSubpath);
            }
        }
    }

    private void handleSimpleProperty(ComplexRecord parentRecord, JsonNode valueNode, SimpleProperty simpleSubpath, String fieldName) {
        if (valueNode.isArray()) {
            ArrayList<String> values = new ArrayList<>();
            for (JsonNode itemNode : valueNode) {
                values.add(itemNode.asText());
            }
            parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), values);
        } else {
            parentRecord.addSimpleValueRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), valueNode.asText());
        }
    }

    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }


    @Override
    public QueryResult executeQuery(QueryStatement statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeQuery'");
    }

    @Override public JSONObject getTableNames(String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.getTableNames not implemented.");
    }

    @Override public JSONObject getTable(String tableName, String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.gatTable not implemented.");
    }

    @Override public JSONObject getRows(String tableName, String columnName, String columnValue, String operator, String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.getRow not implemented.");
    }

}
