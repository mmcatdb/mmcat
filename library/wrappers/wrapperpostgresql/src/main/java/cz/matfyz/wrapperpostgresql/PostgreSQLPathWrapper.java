package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();
    
    @Override
    public void addProperty(String hierarchy) {
        this.properties.add(hierarchy);
    }

    @Override
    public boolean check() {
        throw new UnsupportedOperationException("PostgreSQLPathWrapper.check not implemented.");
    }

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return false; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGroupingAllowed() { return false; }
    @Override public boolean isDynamicNamingAllowed() { return false; }
    @Override public boolean isAnonymousNamingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return false; }
    @Override public boolean isSchemaLess() { return false; }
    // CHECKSTYLE:ON
}
