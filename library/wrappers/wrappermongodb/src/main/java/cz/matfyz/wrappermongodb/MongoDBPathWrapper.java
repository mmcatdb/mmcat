package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.core.mapping.ComplexProperty;

public class MongoDBPathWrapper implements AbstractPathWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return true; }
    @Override public boolean isGroupingAllowed() { return true; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaless() { return true; }
    // CHECKSTYLE:ON

    @Override public boolean isPathValid(ComplexProperty accessPath) {
        throw new UnsupportedOperationException("MongoDBPathWrapper.isPathValid not implemented.");
    }

}
