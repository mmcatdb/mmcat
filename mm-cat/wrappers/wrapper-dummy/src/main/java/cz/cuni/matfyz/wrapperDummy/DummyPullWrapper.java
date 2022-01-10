package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ComplexRecord;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;

import java.nio.file.*;
import java.util.*;
import org.json.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class DummyPullWrapper implements AbstractPullWrapper
{

	@Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path) throws Exception
    {
        return pullForest(selectAll, path, false, 0, 0);
	}

	@Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception
    {
		return pullForest(selectAll, path, true, limit, offset);
	}
    
    private ForestOfRecords pullForest(String selectAll, ComplexProperty path, boolean doLimitAndOffset, int limit, int offset) throws Exception
    {
        String jsonString = Files.readString(Path.of(selectAll));
        var json = new JSONArray(jsonString);
        
        var forest = new ForestOfRecords();
        
        for (int i = 0; i < json.length(); i++)
        {
            JSONObject object = json.getJSONObject(i);
            
            if (doLimitAndOffset)
                if (i < offset || i >= limit)
                    continue;
                
            var record = new RootRecord();
            getDataFromObject(record, object, path);
            forest.addRecord(record);
        }
        
        return forest;
    }
    
    private void getDataFromObject(ComplexRecord parentRecord, JSONObject object, ComplexProperty path) throws Exception
    {
        boolean hasSubpathWithDynamicName = false;
        
        for (AccessPath subpath : path.subpaths())
        {
            if (subpath.name().type() == Name.Type.DYNAMIC_NAME)
                hasSubpathWithDynamicName = true;
            else
                getFieldWithKeyForSubpathFromObject(parentRecord, object, subpath.name().getStringName(), subpath);
        }
        
        if (hasSubpathWithDynamicName)
            getDataFromDynamicFieldsOfObject(parentRecord, object, path);
    }
    
    private void getDataFromDynamicFieldsOfObject(ComplexRecord parentRecord, JSONObject object, ComplexProperty path) throws Exception
    {
        // First we find all names that belong to the subpaths with non-dynamic names and also the subpath with the dynamic name
        AccessPath subpathWithDynamicName = null;
        Set<String> otherSubpathNames = new TreeSet<>();
        
        for (AccessPath subpath : path.subpaths())
        {
            if (subpath.name().type() == Name.Type.DYNAMIC_NAME)
                subpathWithDynamicName = subpath;
            else
                otherSubpathNames.add(subpath.name().getStringName());
        }
        
        // For all keys in the object where the key is not a known static name do ...
        Iterator<?> iterator = object.keys();
        while (iterator.hasNext())
        {
            if (iterator.next() instanceof String key)
            {
                if (!otherSubpathNames.contains(key))
                    getFieldWithKeyForSubpathFromObject(parentRecord, object, key, subpathWithDynamicName);
            }
        }
    }
    
    private void getFieldWithKeyForSubpathFromObject(ComplexRecord parentRecord, JSONObject object, String key, AccessPath subpath) throws Exception
    {
        if (object.isNull(key)) // Returns if the value is null or if the value doesn't exist.
            return;
        
        var value = object.get(key);
        
        if (subpath instanceof ComplexProperty complexSubpath)
        {
            if (value instanceof JSONArray childArray)
            {
                for (int i = 0; i < childArray.length(); i++)
                    addComplexValueToRecord(parentRecord, childArray.getJSONObject(i), key, complexSubpath);
            }
            else if (value instanceof JSONObject childObject)
                addComplexValueToRecord(parentRecord, childObject, key, complexSubpath);
        }
        else if (subpath instanceof SimpleProperty simpleSubpath)
        {
            if (value instanceof JSONArray simpleArray)
            {
                var values = new ArrayList<String>();
                
                for (int i = 0; i < simpleArray.length(); i++)
                    values.add(simpleArray.get(i).toString());
                    
                parentRecord.addSimpleArrayRecord(simpleSubpath.name().toRecordName(key), simpleSubpath.value().signature(), values);
            }
            else
            {
                if (simpleSubpath.name().type() == Name.Type.DYNAMIC_NAME)
                    parentRecord.addSimpleDynamicRecord(simpleSubpath.name().toRecordName(key), simpleSubpath.value().signature(), value.toString());
                else
                    parentRecord.addSimpleValueRecord(simpleSubpath.name().toRecordName(key), simpleSubpath.value().signature(), value.toString());
            }
        }
    }
    
    private void addComplexValueToRecord(ComplexRecord parentRecord, JSONObject value, String key, ComplexProperty complexProperty) throws Exception
    {
        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        if (complexProperty.isAuxiliary())
            getDataFromObject(parentRecord, value, complexProperty);
        else
        {
            ComplexRecord childRecord = parentRecord.addComplexRecord(complexProperty.name().toRecordName(key), complexProperty.signature());
            getDataFromObject(childRecord, value, complexProperty);
            
            if (complexProperty.name().type() == Name.Type.DYNAMIC_NAME)
                childRecord.addSimpleValueRecord(cz.cuni.matfyz.core.record.Name.LeftDynamic(), complexProperty.name().signature(), childRecord.name().value());
        }
    }
}
