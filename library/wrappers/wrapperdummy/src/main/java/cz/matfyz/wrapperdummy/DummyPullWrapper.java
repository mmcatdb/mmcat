package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RecordName;
import cz.matfyz.core.record.RootRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author pavel.koupil, jachym.bartik
 */
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

    private ForestOfRecords innerPullForest(ComplexProperty path, StringQuery query) throws JSONException {
        var json = new JSONArray(query.content);
        var forest = new ForestOfRecords();

        for (int i = 0; i < json.length(); i++) {
            JSONObject object = json.getJSONObject(i);
            var rootRecord = new RootRecord();

            getDataFromObject(rootRecord, object, path);
            forest.addRecord(rootRecord);
        }

        return forest;
    }

    private void getDataFromObject(ComplexRecord parentRecord, JSONObject object, ComplexProperty path) throws JSONException {
        boolean hasSubpathWithDynamicName = false;

        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                getFieldWithKeyForSubpathFromObject(parentRecord, object, staticName.getStringName(), subpath);
            else
                hasSubpathWithDynamicName = true;
        }

        if (hasSubpathWithDynamicName)
            getDataFromDynamicFieldsOfObject(parentRecord, object, path);
    }

    private void getDataFromDynamicFieldsOfObject(ComplexRecord parentRecord, JSONObject object, ComplexProperty path) throws JSONException {
        // First we find all names that belong to the subpaths with non-dynamic names and also the subpath with the dynamic name
        AccessPath subpathWithDynamicName = null;
        Set<String> otherSubpathNames = new TreeSet<>();

        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                otherSubpathNames.add(staticName.getStringName());
            else
                subpathWithDynamicName = subpath;

        }

        // For all keys in the object where the key is not a known static name do ...
        Iterator<?> iterator = object.keys();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof String key && !otherSubpathNames.contains(key)) {
                getFieldWithKeyForSubpathFromObject(parentRecord, object, key, subpathWithDynamicName);
            }
        }
    }

    private void getFieldWithKeyForSubpathFromObject(ComplexRecord parentRecord, JSONObject object, String key, AccessPath subpath) throws JSONException {
        if (object.isNull(key)) // Returns if the value is null or if the value doesn't exist.
            return;

        var value = object.get(key);

        if (subpath instanceof ComplexProperty complexSubpath) {
            if (value instanceof JSONArray childArray) {
                for (int i = 0; i < childArray.length(); i++)
                    addComplexValueToRecord(parentRecord, childArray.getJSONObject(i), key, complexSubpath);
            }
            else if (value instanceof JSONObject childObject)
                addComplexValueToRecord(parentRecord, childObject, key, complexSubpath);
        }
        else if (subpath instanceof SimpleProperty simpleSubpath) {
            if (value instanceof JSONArray simpleArray) {
                var values = new ArrayList<String>();

                for (int i = 0; i < simpleArray.length(); i++)
                    values.add(simpleArray.get(i).toString());

                parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), key), simpleSubpath.signature(), values);
            }
            else {
                RecordName recordName = toRecordName(simpleSubpath.name(), key);
                parentRecord.addSimpleValueRecord(recordName, simpleSubpath.signature(), value.toString());
            }
        }
    }

    private void addComplexValueToRecord(ComplexRecord parentRecord, JSONObject value, String key, ComplexProperty complexProperty) throws JSONException {
        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        if (complexProperty.isAuxiliary())
            getDataFromObject(parentRecord, value, complexProperty);
        else {
            ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexProperty.name(), key), complexProperty.signature());
            getDataFromObject(childRecord, value, complexProperty);

            // Dynamic complex property is just a normal complex property with additional simple property for name.
            /*
            if (complexProperty.name() instanceof DynamicName dynamicName)
                childRecord.addSimpleValueRecord(RecordName.LeftDynamic(), dynamicName.signature(), childRecord.name().value());
            */
        }
    }

    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);

        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    @Override public QueryResult executeQuery(QueryStatement statement) {
        // TODO
        throw new UnsupportedOperationException("DummyPullWrapper.executeQuery not implemented.");
    }
}
