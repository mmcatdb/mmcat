/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.statements.DDLStatement;
import java.util.Set;
import java.util.ArrayList;

class Property
{
    public String name;
    public String command;
    
    public Property(String name, String command)
    {
        this.name = name;
        this.command = command;
    }
}

/**
 *
 * @author jachym.bartik
 */
public class PostgreSQLDDLWrapper implements AbstractDDLWrapper
{    
    private String kindName = null;
    private ArrayList<Property> properties = new ArrayList<Property>();
    
    public void setKindName(String name)
    {
        kindName = name;
    }

    public boolean isSchemaLess() { return false; }

    public boolean addSimpleProperty(Set<String> names, boolean optional) throws UnsupportedOperationException
    {
        for (String name : names)
        {
            if (properties.stream().anyMatch(property -> property.name == name)) // Indicate error if the name is already present
                return false;
            
            String command = name + " TEXT" + (optional ? "" : " NOT NULL");
            properties.add(new Property(name, command));
        }
        
        return true;
    }

    public boolean addSimpleArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException
    {
        for (String name : names)
        {
            if (properties.stream().anyMatch(property -> property.name == name)) // Indicate error if the name is already present
                return false;
            
            String command = name + " text[]" + (optional ? "" : " NOT NULL");
            properties.add(new Property(name, command));
        }
        
        return true;
    }

    public boolean addComplexProperty(Set<String> names, boolean optional) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(); // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    public boolean addComplexArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(); // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    public DDLStatement createDDLStatement()
    {
        String commands = String.join(",", properties.stream().map(property -> "\n" + property.command).toList());
        String content = String.format("CREATE TABLE %s (%s\n);", kindName, commands);
        
        return new PostgreSQLDDLStatement(content);
    }
}

class PostgreSQLDDLStatement implements DDLStatement
{
    private String content;
    
    public PostgreSQLDDLStatement(String content) {
        this.content = content;
    }
}
