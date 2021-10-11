/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.statements.DMLStatement;
import java.util.ArrayList;
import java.util.List;

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

/**
 *
 * @author jachym.bartik
 */
public class MongoDBPushWrapper implements AbstractPushWrapper
{
    private String kindName = null;
    private ArrayList<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
    
	public void setKindName(String name)
    {
        kindName = name;
    }

	public void append(String name, Object value)
    {
        propertyValues.add(new PropertyValue(name, value.toString()));
    }

	public DMLStatement createDMLStatement()
    {
        List<String> dataValues = propertyValues.stream().map(propertyValue -> String.format("%s: %s", propertyValue.name, escapeString(propertyValue.value))).toList();
        
        String content = String.format("db.%s.insert({ %s })", kindName, String.join(", ", dataValues));
        return new MongoDBDMLStatement(content);
    }
    
    private String escapeString(String input)
    {
        return "\"" + input.replaceAll("\\", "\\\\").replaceAll("\"", "\\\"") + "\"";
    }

	public void clear()
    {
        kindName = null;
        propertyValues = new ArrayList<PropertyValue>();
    }
}

class MongoDBDMLStatement implements DMLStatement
{
    private String content;
    
    public MongoDBDMLStatement(String content) {
        this.content = content;
    }
}
