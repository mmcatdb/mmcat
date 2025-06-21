package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.core.mapping.ComplexProperty;

public class PostgreSQLPathWrapper implements AbstractPathWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return false; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGroupingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return false; }
    @Override public boolean isSchemaless() { return false; }
    // CHECKSTYLE:ON

    @Override public boolean isPathValid(ComplexProperty accessPath) {
        // Has to be flat.
        for (final var subpath: accessPath.subpaths()) {
            if (subpath instanceof ComplexProperty)
                return false;
        }

        return true;
    }

}
