/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.statements.DDLStatement;
import java.util.Set;

/**
 *
 * @author jachym.bartik
 */
public class MongoDBDDLWrapper implements AbstractDDLWrapper
{    
    private String kindName = null;
    
    public void setKindName(String name)
    {
        kindName = name;
    }

    public boolean isSchemaLess() { return true; }
    public boolean addSimpleProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    public boolean addSimpleArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    public boolean addComplexProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    public boolean addComplexArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }

    public DDLStatement createDDLStatement()
    {
        return new MongoDBDDLStatement("db.createCollection(" + kindName + ")");
    }
}

class MongoDBDDLStatement implements DDLStatement {
    
    private String content;
    
    public MongoDBDDLStatement(String content) {
        this.content = content;
    }
}