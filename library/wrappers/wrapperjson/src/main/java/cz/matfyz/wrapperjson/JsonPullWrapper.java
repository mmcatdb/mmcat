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
import java.util.List;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A pull wrapper implementation for JSON files that implements the {@link AbstractPullWrapper} interface.
 * This class provides methods for pulling data from JSON files and converting it into a {@link ForestOfRecords}.
 */
public class JsonPullWrapper implements AbstractPullWrapper {

    private final JsonProvider provider;

    /**
     * Constructs a new {@code JsonPullWrapper} with the specified JSON provider.
     *
     * @param provider the JSON provider used to access JSON files.
     */
    public JsonPullWrapper(JsonProvider provider) {
        this.provider = provider;
    }

    /**
     * Pulls a forest of records from a JSON file based on a complex property path and query content.
     *
     * @param path the complex property path specifying the structure of the records.
     * @param query the query content used to filter or modify the records pulled.
     * @return a {@link ForestOfRecords} containing the pulled records.
     * @throws PullForestException if an error occurs while pulling the forest of records.
     */
    @Override
    public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        System.out.println("change propagated");
        final var forest = new ForestOfRecords();

        try {
            List<String> fileNames = provider.getJsonFileNames();
            for (String fileName : fileNames) {
                try (InputStream inputStream = provider.getInputStream(fileName)) {
                    processJsonStream(inputStream, forest, path);
                } catch (IOException e) {
                    System.err.println("Error processing file: " + fileName + " - " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw PullForestException.innerException(e);
        }

        return forest;
    }

    /**
     * Processes a JSON input stream and populates a {@link ForestOfRecords} with data parsed from the stream.
     *
     * @param inputStream the input stream of the JSON file.
     * @param forest the forest of records to populate.
     * @param path the complex property path specifying the structure of the records.
     * @throws IOException if an error occurs while processing the input stream.
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
     *
     * @param parentRecord the parent record to populate with data.
     * @param jsonNode the JSON node to extract data from.
     * @param path the complex property path specifying the structure of the records.
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
     *
     * @param parentRecord the parent record to populate.
     * @param arrayNode the JSON array node.
     * @param complexSubpath the complex property subpath describing the array structure.
     * @param fieldName the field name associated with the array.
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
     *
     * @param parentRecord the parent record to populate.
     * @param valueNode the JSON node representing the property value.
     * @param simpleSubpath the simple property subpath describing the property.
     * @param fieldName the field name associated with the property.
     */
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

    /**
     * Converts a {@link Name} object to a {@link RecordName} based on its type (static or dynamic).
     *
     * @param name the name object to convert.
     * @param valueIfDynamic the value to use if the name is dynamic.
     * @return the converted {@link RecordName}.
     */
    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    /**
     * Executes a query statement. This method is currently not implemented.
     *
     * @param statement the query statement to execute.
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override
    public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("Unimplemented method 'executeQuery'");
    }

}
