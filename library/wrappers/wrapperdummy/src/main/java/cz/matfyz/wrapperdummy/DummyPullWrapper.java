package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.util.List;
import java.util.Map;

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
            throw PullForestException.innerException(e);
        }
    }

    private Map<DynamicName, DynamicNameReplacement> replacedNames;

    private ForestOfRecords innerPullForest(ComplexProperty path, StringQuery query) throws JSONException {
        final var json = new JSONArray(query.content);
        final var forest = new ForestOfRecords();

        replacedNames = path.copyWithoutDynamicNames().replacedNames();

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

            final var subpath = path.findSubpathByName(key);
            if (subpath == null)
                continue;

            final var value = object.get(key);
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

    private void addValueToRecord(ComplexRecord parentRecord, AccessPath property, Object value) throws JSONException {
        if (value instanceof final JSONArray array) {
            // If it's array, we flatten it.
            for (int i = 0; i < array.length(); i++)
                addValueToRecord(parentRecord, property, array.get(i));
            return;
        }

        if (property instanceof final SimpleProperty simpleProperty) {
            // If it's a simple value, we add it to the record.
            parentRecord.addSimpleRecord(simpleProperty.signature(), value.toString());
            return;
        }

        final var complexProperty = (ComplexProperty) property;
        final var object = (JSONObject) value;

        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        final ComplexRecord childRecord = complexProperty.isAuxiliary()
            ? parentRecord
            : parentRecord.addComplexRecord(complexProperty.signature());

        addKeysToRecord(childRecord, complexProperty, object);
    }

    @Override public QueryResult executeQuery(QueryStatement statement) {
        // TODO
        throw new UnsupportedOperationException("DummyPullWrapper.executeQuery not implemented.");
    }

    @Override public KindNameResponse getKindNames(String limit, String offset) {
        throw new UnsupportedOperationException("DummyPullWrapper.getKindNames not implemented.");
    }

    @Override public DataResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filter) {
        throw new UnsupportedOperationException("DummyPullWrapper.getKind not implemented.");
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        throw new UnsupportedOperationException("DummyPullWrapper.getReferences not implemented.");
    }

}
