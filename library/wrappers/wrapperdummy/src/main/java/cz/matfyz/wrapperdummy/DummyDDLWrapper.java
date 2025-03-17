package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DummyDDLWrapper implements AbstractDDLWrapper {

    private final List<String> methods = new ArrayList<>();

    public List<String> methods() {
        return methods;
    }

    @Override public boolean isSchemaless() {
        methods.add("isSchemaless()");
        return false;
    }

    @Override public void clear() {
        methods.add("clear()");
    }

    @Override public void setKindName(String name) {
        methods.add("setKindName(" + name + ")");
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        final String complex = isComplex ? "complex" : "simple";
        final String required = isRequired ? "required" : "optional";
        methods.add("addProperty(" + path.toString() + ", " + complex + ", " + required + ")");
    }

    @Override public AbstractStatement createDDLStatement() {
        methods.add("createDDLStatement()");
        return AbstractStatement.createEmpty();
    }

    @Override
    public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        throw new UnsupportedOperationException("Unimplemented method 'createDDLDeleteStatements'");
    }

}
