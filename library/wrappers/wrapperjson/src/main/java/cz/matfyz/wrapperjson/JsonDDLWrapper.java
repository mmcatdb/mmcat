package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.Set;

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
    public JsonCommandStatement createDDLStatement() {
        return new JsonCommandStatement("");
    }
}
