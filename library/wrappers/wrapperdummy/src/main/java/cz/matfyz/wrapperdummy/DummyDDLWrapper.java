package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;

public class DummyDDLWrapper implements AbstractDDLWrapper {

    private List<String> methods = new ArrayList<>();

    public List<String> methods() {
        return methods;
    }

    @Override public void setKindName(String name) {
        methods.add("setKindName(" + name + ")");
    }

    @Override public boolean isSchemaless() {
        methods.add("isSchemaless()");
        return false;
    }

    @Override public boolean addSimpleProperty(String path, boolean required) {
        methods.add("addSimpleProperty(" + path + ", " + required + ")");
        return true;
    }

    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
        methods.add("addSimpleArrayProperty(" + path + ", " + required + ")");
        return true;
    }

    @Override public boolean addComplexProperty(String path, boolean required) {
        methods.add("addComplexProperty(" + path + ", " + required + ")");
        return true;
    }

    @Override public boolean addComplexArrayProperty(String path, boolean required) {
        methods.add("addComplexArrayProperty(" + path + ", " + required + ")");
        return true;
    }

    @Override public DummyStatement createDDLStatement() {
        methods.add("createDDLStatement()");
        return new DummyStatement("");
    }

}
