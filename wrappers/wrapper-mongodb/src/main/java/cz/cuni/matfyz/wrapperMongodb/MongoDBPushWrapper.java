package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBPushWrapper implements AbstractPushWrapper
{
    private String kindName = null;
    private List<PropertyValue> propertyValues = new ArrayList<>();
    
    @Override
	public void setKindName(String name)
    {
        kindName = name;
    }

    @Override
	public void append(String name, Object value)
    {
        propertyValues.add(new PropertyValue(name, value.toString()));
    }

    @Override
	public MongoDBDMLStatement createDMLStatement()
    {
        List<String> dataValues = propertyValues.stream().map(propertyValue -> String.format("%s%s: %s",
            AbstractDDLWrapper.INTENDATION,
            propertyValue.name,
            escapeString(propertyValue.value)
        )).toList();
        
        String content = String.format("db.%s.insert({\n%s\n});", kindName, String.join(",\n", dataValues));
        return new MongoDBDMLStatement(content);
    }
    
    private String escapeString(String input)
    {
        return "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Override
	public void clear()
    {
        kindName = null;
        propertyValues = new ArrayList<>();
    }
}

class PropertyValue
{
    public String name;
    public String value;
    
    public PropertyValue(String name, String value)
    {
        this.name = name;
        this.value = value;
    }
}