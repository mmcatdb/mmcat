package cz.cuni.matfyz.wrapperdummy;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.PullForestException;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.record.ComplexRecord;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RecordName;
import cz.cuni.matfyz.core.record.RootRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, PullQuery query) throws PullForestException {
        try {
            return innerPullForest(path, query);
        }
        catch (Exception e) {
            throw new PullForestException(e);
        }
    }

    private String getJsonString(PullQuery query) throws IOException {
        if (query.hasStringContent())
            return query.getStringContent();

        String resourceFileName = query.getKindName();
        return Files.readString(Path.of(resourceFileName));
    }

    private ForestOfRecords innerPullForest(ComplexProperty path, PullQuery query) throws IOException, JSONException {
        var json = new JSONArray(getJsonString(query));
        var forest = new ForestOfRecords();
        
        int offset = query.hasOffset() ? query.getOffset() : 0;
        for (int i = offset; i < json.length(); i++) {
            if (query.hasLimit() && i >= query.getLimit())
                break;

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
}
