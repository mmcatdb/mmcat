package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;

public class Neo4jDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public boolean addSimpleProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addComplexArrayProperty(String path, boolean required) {
        return false;
    }

    @Override public AbstractStatement createDDLStatement() {
        return AbstractStatement.createEmpty();
    }

}
