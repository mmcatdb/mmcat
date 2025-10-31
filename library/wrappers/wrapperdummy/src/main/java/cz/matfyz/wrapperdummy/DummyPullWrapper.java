package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ComplexRecord.ArrayCollector;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DummyPullWrapper implements AbstractPullWrapper {

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        if (!(query instanceof StringQuery stringQuery))
            throw PullForestException.invalidQuery(this, query);

        try {
            return innerPullForest(path, stringQuery);
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    private ForestOfRecords innerPullForest(ComplexProperty path, StringQuery query) throws JSONException {
        final var json = new JSONArray(query.content);
        final var forest = new ForestOfRecords();

        for (int i = 0; i < json.length(); i++) {
            final JSONObject object = json.getJSONObject(i);
            final var rootRecord = new RootRecord();

            addKeysToRecord(rootRecord, path, object);
            forest.addRecord(rootRecord);
        }

        return forest;
    }

    private void addKeysToRecord(ComplexRecord record, ComplexProperty path, JSONObject object) throws JSONException {
        final var iterator = object.keys();
        while (iterator.hasNext()) {
            final var key = (String) iterator.next();
            if (object.isNull(key))
                continue;

            final var property = path.findSubpathByName(key);
            if (property == null)
                continue;

            final var value = object.get(key);
            if (property.name() instanceof DynamicName) {
                final var dynamicRecord = record.addDynamicRecord(property, key);
                final var valueProperty = ((ComplexProperty) property).getTypedSubpath(TypedName.VALUE);
                addValueToRecord(dynamicRecord, valueProperty, value);
                continue;
            }

            addValueToRecord(record, property, value);
        }
    }

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) throws JSONException {
        if (property.signature().hasDual())
            addArrayToRecord(parentRecord, property, (JSONArray) value);
        else
            addScalarValueToRecord(parentRecord, property, value);
    }

    private void addScalarValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) throws JSONException {
        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.toString());
            return;
        }

        final ComplexRecord childRecord = parentRecord.addComplexRecord(property.signature());
        addKeysToRecord(childRecord, (ComplexProperty) property, (JSONObject) value);
    }

    private void addArrayToRecord(ComplexRecord parentRecord, AccessPath property, JSONArray array) throws JSONException {
        if (!(property instanceof final ComplexProperty complexProperty) || complexProperty.getIndexSubpaths().isEmpty()) {
            for (int i = 0; i < array.length(); i++)
                addScalarValueToRecord(parentRecord, property, array.get(i));
            return;
        }

        final var collector = new ArrayCollector(parentRecord, complexProperty);
        processArrayDimension(collector, array);
    }

    private void processArrayDimension(ArrayCollector collector, JSONArray array) throws JSONException {
        final var isValueDimension = collector.nextDimension();
        for (int i = 0; i < array.length(); i++) {
            collector.setIndex(i);
            if (isValueDimension)
                addValueToRecord(collector.addIndexedRecord(), collector.valueSubpath, array.get(i));
            else
                processArrayDimension(collector, array.getJSONArray(i));
        }
        collector.prevDimension();
    }

    // #region Querying

    @Override public QueryResult executeQuery(QueryStatement statement) {
        throw new UnsupportedOperationException("DummyPullWrapper.executeQuery not implemented.");
    }

    @Override public List<String> getKindNames() {
        throw new UnsupportedOperationException("DummyPullWrapper.getKindNames not implemented.");
    }

    @Override public DataResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filter) {
        throw new UnsupportedOperationException("DummyPullWrapper.getRecords not implemented.");
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        throw new UnsupportedOperationException("DummyPullWrapper.getReferences not implemented.");
    }

    @Override public DataResponse getQueryResult(QueryContent query) {
        throw new UnsupportedOperationException("DummyPullWrapper.getQueryResult not implemented.");
    }

    // #endregion

}
