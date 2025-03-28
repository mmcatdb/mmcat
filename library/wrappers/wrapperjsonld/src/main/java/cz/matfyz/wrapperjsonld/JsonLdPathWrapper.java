package cz.matfyz.wrapperjsonld;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

public class JsonLdPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();

    @Override public void addProperty(String path) {
        properties.add(path);
    }

    @Override public boolean check() {
        throw new UnsupportedOperationException("JsonLdPathWrapper.check not implemented.");
    }

    // CHECKSTYLE:OFF
    // TODO check the correctness of the following methods:
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return true; }
    @Override public boolean isGroupingAllowed() { return true; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaless() { return true; }
    // CHECKSTYLE:ON
}
