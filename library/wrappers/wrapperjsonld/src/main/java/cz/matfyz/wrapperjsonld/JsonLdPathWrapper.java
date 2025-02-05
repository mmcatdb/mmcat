package cz.matfyz.wrapperjsonld;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.core.mapping.ComplexProperty;

public class JsonLdPathWrapper implements AbstractPathWrapper {

    // CHECKSTYLE:OFF
    // TODO check the correctness of the following methods:
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return true; }
    @Override public boolean isGroupingAllowed() { return true; }
    @Override public boolean isAnonymousNamingAllowed() { return true; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaless() { return true; }
    // CHECKSTYLE:ON

    @Override public boolean isPathValid(ComplexProperty accessPath) {
        throw new UnsupportedOperationException("JsonLdPathWrapper.isPathValid not implemented.");
    }

}
