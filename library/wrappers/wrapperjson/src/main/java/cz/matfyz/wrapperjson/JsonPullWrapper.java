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
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ComplexRecord.ArrayCollector;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pull wrapper implementation for JSON files that implements the {@link AbstractPullWrapper} interface.
 * This class provides methods for pulling data from JSON files and converting it into a {@link ForestOfRecords}.
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
            InputStream inputStream = provider.getInputStream();
            Stream<ObjectNode> jsonStream = JsonParsedIterator.toStream(inputStream);
        ) {
            return processJsonStream(jsonStream, path);
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    /**
     * Processes a JSON input stream and populates a {@link ForestOfRecords} with data parsed from the stream.
     */
    private ForestOfRecords processJsonStream(Stream<ObjectNode> stream, ComplexProperty path) throws IOException {
        final var forest = new ForestOfRecords();;

        stream.forEach(object -> {
            final RootRecord rootRecord = new RootRecord();

            addKeysToRecord(rootRecord, path, object);
            forest.addRecord(rootRecord);
        });

        return forest;
    }

    private void addKeysToRecord(ComplexRecord record, ComplexProperty path, ObjectNode object) {
        for (final var entry : object.properties()) {
            final var key = entry.getKey();
            final var value = entry.getValue();
            if (value == null || value.isNull())
                continue;

            final var property = path.findSubpathByName(key);
            if (property == null)
                continue;

            if (property.name() instanceof DynamicName) {
                final var dynamicRecord = record.addDynamicRecord(property, key);
                final var valueProperty = ((ComplexProperty) property).getTypedSubpath(TypedName.VALUE);
                addValueToRecord(dynamicRecord, valueProperty, value);
                continue;
            }

            addValueToRecord(record, property, value);
        }
    }

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, JsonNode value) {
        if (property.signature().hasDual())
            addArrayToRecord(parentRecord, property, value);
        else
            addScalarValueToRecord(parentRecord, property, value);
    }

    private void addScalarValueToRecord(ComplexRecord parentRecord, AccessPath property, JsonNode value) {
        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.asText());
            return;
        }

        final ComplexRecord childRecord = parentRecord.addComplexRecord(property.signature());
        addKeysToRecord(childRecord, (ComplexProperty) property, (ObjectNode) value);
    }

    private void addArrayToRecord(ComplexRecord parentRecord, AccessPath property, JsonNode array) {
        if (!(property instanceof final ComplexProperty complexProperty) || complexProperty.getIndexSubpaths().isEmpty()) {
            for (final var element : array)
                addScalarValueToRecord(parentRecord, property, element);
            return;
        }

        final var collector = new ArrayCollector(parentRecord, complexProperty);
        processArrayDimension(collector, array);
    }

    private void processArrayDimension(ArrayCollector collector, JsonNode array) {
        final var isValueDimension = collector.nextDimension();
        int i = 0;
        for (final var element : array) {
            collector.setIndex(i);
            if (isValueDimension)
                addValueToRecord(collector.addIndexedRecord(), collector.valueSubpath, element);
            else
                processArrayDimension(collector, element);
            i++;
        }
        collector.prevDimension();
    }

    // #region Querying

    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("JsonPullWrapper.executeQuery not implemented.");
    }

    @Override public List<String> getKindNames() {
        return List.of(provider.getKindName());
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

    // #endregion

}
