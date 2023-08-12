package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.Set;


/**
 * @author jachymb.bartik
 */
public class Neo4jDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    
    @Override
    public void setKindName(String name) {
        kindName = name;
    }

    @Override
    public boolean isSchemaLess() {
        return true;
    }

    @Override
    public boolean addSimpleProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public boolean addComplexProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public boolean addComplexArrayProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public Neo4jStatement createDDLStatement() {
        return Neo4jStatement.createEmpty();
    }
}