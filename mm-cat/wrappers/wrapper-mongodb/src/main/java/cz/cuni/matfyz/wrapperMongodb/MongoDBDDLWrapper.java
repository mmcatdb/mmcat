package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import java.util.Set;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBDDLWrapper implements AbstractDDLWrapper
{    
    private String kindName = null;
    
    @Override
    public void setKindName(String name)
    {
        kindName = name;
    }

    @Override public boolean isSchemaLess() { return true; }
    @Override public boolean addSimpleProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    @Override public boolean addSimpleArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    @Override public boolean addComplexProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }
    @Override public boolean addComplexArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException { return false; }

    @Override
    public MongoDBDDLStatement createDDLStatement()
    {
        return new MongoDBDDLStatement("db.createCollection(" + kindName + ")");
    }
}