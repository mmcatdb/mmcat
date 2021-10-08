/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperPostgresql;

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
public class PostgreSQLPushWrapper implements AbstractPushWrapper
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
        List<String> names = propertyValues.stream().map(propertyValue -> propertyValue.name).toList();
        List<String> values = propertyValues.stream().map(propertyValue -> escapeString(propertyValue.value)).toList();
        
        String content = String.format("INSERT INTO %s(%s)\nVALUES (%s)", kindName, String.join(", ", names), String.join(", ", values));
        return new PostgreSQLDMLStatement(content);
    }
    
    private String escapeString(String input)
    {
        return "'" + input.replaceAll("'", "''") + "'";
    }

	public void clear()
    {
        kindName = null;
        propertyValues = new ArrayList<PropertyValue>();
    }
}

class PostgreSQLDMLStatement implements DMLStatement
{
    private String content;
    
    public PostgreSQLDMLStatement(String content) {
        this.content = content;
    }
}
