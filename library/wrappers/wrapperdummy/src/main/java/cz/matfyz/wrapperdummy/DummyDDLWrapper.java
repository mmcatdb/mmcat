package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DummyDDLWrapper implements AbstractDDLWrapper {

    private List<String> methods = new ArrayList<>();

    public List<String> methods() {
        return methods;
    }

    @Override public void setKindName(String name) {
        methods.add("setKindName(" + name + ")");
    }

    @Override public boolean isSchemaLess() {
        methods.add("isSchemaLess()");
        return false;
    }

    @Override public boolean addSimpleProperty(Set<String> names, boolean required) {
        methods.add("addSimpleProperty(" + setToString(names) + ", " + required + ")");
        return true;
    }

    @Override public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
        methods.add("addSimpleArrayProperty(" + setToString(names) + ", " + required + ")");
        return true;
    }

    @Override public boolean addComplexProperty(Set<String> names, boolean required) {
        methods.add("addComplexProperty(" + setToString(names) + ", " + required + ")");
        return true;
    }

    @Override public boolean addComplexArrayProperty(Set<String> names, boolean required) {
        methods.add("addComplexArrayProperty(" + setToString(names) + ", " + required + ")");
        return true;
    }

    @Override public DummyStatement createDDLStatement() {
        methods.add("createDDLStatement()");
        return new DummyStatement("");
    }

    private String setToString(Set<String> strings) {
        var builder = new StringBuilder();

        builder.append("[");
        int index = 0;
        for (String string : strings) {
            if (index > 0)
                builder.append(",");
            index++;
            builder.append(" ").append(string);
        }

        if (index > 0)
            builder.append(" ");
        builder.append("]");

        return builder.toString();
    }
}
