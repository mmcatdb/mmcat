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

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        final String complex = isComplex ? "complex" : "simple";
        final String required = isRequired ? "required" : "optional";
        methods.add("addProperty(" + path.toString() + ", " + complex + ", " + required + ")");
    }

    @Override public DummyStatement createDDLStatement() {
        methods.add("createDDLStatement()");
        return new DummyStatement("");
    }

}
