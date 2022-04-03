package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.*;

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
	public ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws Exception
    {
        String resourceFileName = options.hasCommand() ? options.getCommand() : options.getKindName();
        String jsonString = Files.readString(Path.of(resourceFileName));
        var json = new JSONArray(jsonString);
        
        var forest = new ForestOfRecords();
        
        int offset = options.hasOffset() ? options.getOffset() : 0;
        for (int i = offset; i < json.length(); i++)
        {
            if (options.hasLimit() && i >= options.getLimit())
                break;

            JSONObject object = json.getJSONObject(i);
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
            if (subpath.name() instanceof StaticName staticName)
                getFieldWithKeyForSubpathFromObject(parentRecord, object, staticName.getStringName(), subpath);
            else
                hasSubpathWithDynamicName = true;
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
            if (subpath.name() instanceof StaticName staticName)
                otherSubpathNames.add(staticName.getStringName());
            else
                subpathWithDynamicName = subpath;
                
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
                
                parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), key), simpleSubpath.value().signature(), values);
            }
            else
            {
                RecordName recordName = toRecordName(simpleSubpath.name(), key);
                parentRecord.addSimpleValueRecord(recordName, simpleSubpath.value().signature(), value.toString());
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
            ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexProperty.name(), key), complexProperty.signature());
            getDataFromObject(childRecord, value, complexProperty);
            
            // Dynamic complex property is just a normal complex property with additional simple property for name.
            /*
            if (complexProperty.name() instanceof DynamicName dynamicName)
                childRecord.addSimpleValueRecord(RecordName.LeftDynamic(), dynamicName.signature(), childRecord.name().value());
            */
        }
    }

    private RecordName toRecordName(Name name, String valueIfDynamic)
    {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);
        
        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }
}
