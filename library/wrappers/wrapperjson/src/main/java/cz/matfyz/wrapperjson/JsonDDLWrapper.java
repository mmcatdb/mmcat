package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

public class JsonDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;

    @Override
    public void setKindName(String name) {
        kindName = name;
    }

    @Override
    public boolean isSchemaless() {
        return true;
    }

    @Override
    public boolean addSimpleProperty(String path, boolean required) {
        return false;
    }

    @Override
    public boolean addSimpleArrayProperty(String path, boolean required) {
      return false;
    }

    @Override
    public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    @Override
    public boolean addComplexArrayProperty(String path, boolean required) {
       return false;
    }

    @Override
    public JsonCommandStatement createDDLStatement() {
        return new JsonCommandStatement("");
    }
}
