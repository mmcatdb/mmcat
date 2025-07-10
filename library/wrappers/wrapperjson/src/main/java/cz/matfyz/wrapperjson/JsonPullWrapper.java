package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.record.ComplexRecord;

import java.io.IOException;
import java.util.List;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.checkerframework.checker.nullness.qual.Nullable;

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
        try (
            InputStream inputStream = provider.getInputStream()
        ) {
            return processJsonStream(inputStream, path);
        } catch (IOException e) {
            throw PullForestException.inner(e);
        }
    }

    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    /**
     * Processes a JSON input stream and populates a {@link ForestOfRecords} with data parsed from the stream.
     */
    private ForestOfRecords processJsonStream(InputStream inputStream, ComplexProperty path) throws IOException {
        final var forest = new ForestOfRecords();
        final var parser =  new JsonFactory().createParser(inputStream);
        final var objectMapper = new ObjectMapper();

        replacedNames = path.copyWithoutDynamicNames().replacedNames();

        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                final JsonNode jsonNode = objectMapper.readTree(parser);
                final RootRecord rootRecord = new RootRecord();

                addKeysToRecord(rootRecord, path, jsonNode);
                forest.addRecord(rootRecord);
            }
        }

        return forest;
    }

    private void addKeysToRecord(ComplexRecord record, ComplexProperty path, JsonNode object) {
        final var iterator = object.fields();
        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var key = entry.getKey();
            final var value = entry.getValue();
            if (value == null || value.isNull())
                continue;

            final var subpath = path.findSubpathByName(key);
            if (subpath == null)
                continue;

            if (!(subpath.name() instanceof final DynamicName dynamicName)) {
                addValueToRecord(record, subpath, value);
                continue;
            }

            // Replace the dynamically named property with an object containing both name and value properties.
            final var replacement = replacedNames.get(dynamicName);
            final var replacer = record.addDynamicReplacer(replacement.prefix(), replacement.name(), key);
            addValueToRecord(replacer, replacement.value(), value);
        }
    }

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, JsonNode value) {
        if (value.isArray()) {
            // If it's array, we flatten it.
            for (final JsonNode arrayItem : value)
                addValueToRecord(parentRecord, property, arrayItem);
            return;
        }

        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.asText());
            return;
        }

        final var complexProperty = (ComplexProperty) property;

        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        final ComplexRecord childRecord = complexProperty.isAuxiliary()
            ? parentRecord
            : parentRecord.addComplexRecord(complexProperty.signature());

        addKeysToRecord(childRecord, complexProperty, value);
    }

    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("JsonPullWrapper.executeQuery not implemented.");
    }

    @Override public List<String> getKindNames() {
        throw new UnsupportedOperationException("JsonPullWrapper.getKindNames not implemented.");
    }

    @Override public DataResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filter) {
        throw new UnsupportedOperationException("JsonPullWrapper.getRow not implemented.");
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        throw new UnsupportedOperationException("JsonPullWrapper.getReferences not implemented.");
    }

    @Override public DataResponse getQueryResult(QueryContent query) {
        throw new UnsupportedOperationException("JsonPullWrapper.getQueryResult not implemented.");
    }

}
