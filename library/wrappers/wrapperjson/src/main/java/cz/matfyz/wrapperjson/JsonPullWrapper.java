package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
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
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ComplexRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

/**
 * A pull wrapper implementation for JSON files that implements the {@link AbstractPullWrapper} interface.
 * This class provides methods for pulling data from JSON files and converting it into a {@link ForestOfRecords}.
 * This wrapper also supports the JSON Lines format (newline-delimited JSON).
 */
public class JsonPullWrapper implements AbstractPullWrapper {

    private final JsonProvider provider;

    /**
     * Constructs a new {@code JsonPullWrapper} with the specified JSON provider.
     */
    public JsonPullWrapper(JsonProvider provider) {
        this.provider = provider;
    }

    /**
     * Pulls a forest of records from a JSON file based on a complex property path and query content.
     */
    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) {
        final var forest = new ForestOfRecords();

        try (InputStream inputStream = provider.getInputStream()) {
            processJsonStream(inputStream, forest, path);
        } catch (IOException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    /**
     * Processes a JSON input stream and populates a {@link ForestOfRecords} with data parsed from the stream.
     */
    private void processJsonStream(InputStream inputStream, ForestOfRecords forest, ComplexProperty path) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);
        ObjectMapper objectMapper = new ObjectMapper();

        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                JsonNode jsonNode = objectMapper.readTree(parser);
                RootRecord rootRecord = new RootRecord();
                getDataFromJsonNode(rootRecord, jsonNode, path);
                forest.addRecord(rootRecord);
            }
        }
    }

    /**
     * Recursively extracts data from a {@link JsonNode} and populates a {@link ComplexRecord}.
     */
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

    /**
     * Handles JSON arrays and populates a {@link ComplexRecord} with array elements.
     */
    private void handleJsonArray(ComplexRecord parentRecord, JsonNode arrayNode, ComplexProperty complexSubpath, String fieldName) {
        for (JsonNode itemNode : arrayNode) {
            if (itemNode.isObject()) {
                ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexSubpath.name(), fieldName), complexSubpath.signature());
                getDataFromJsonNode(childRecord, itemNode, complexSubpath);
            }
        }
    }

    /**
     * Handles simple properties in the JSON data and populates a {@link ComplexRecord} with the property values.
     */
    private void handleSimpleProperty(ComplexRecord parentRecord, JsonNode valueNode, SimpleProperty simpleSubpath, String fieldName) {
        if (valueNode.isArray()) {
            final ArrayList<String> values = new ArrayList<>();
            for (final JsonNode itemNode : valueNode)
                values.add(itemNode.asText());

            parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), values);
        } else {
            parentRecord.addSimpleValueRecord(toRecordName(simpleSubpath.name(), fieldName), simpleSubpath.signature(), valueNode.asText());
        }
    }

    /**
     * Converts a {@link Name} object to a {@link RecordName} based on its type (static or dynamic).
     */
    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    /**
     * Executes a query statement. This method is currently not implemented.
     */
    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("Unimplemented method 'executeQuery'");
    }

    @Override public JSONObject getKindNames(String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.getKindNames not implemented.");
    }

    @Override public JSONObject getKind(String kindName, String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.getKind not implemented.");
    }

    @Override public JSONObject getRows(String kindName, List<AdminerFilter> filter, String limit, String offset) {
        throw new UnsupportedOperationException("JsonPullWrapper.getRow not implemented.");
    }

}
