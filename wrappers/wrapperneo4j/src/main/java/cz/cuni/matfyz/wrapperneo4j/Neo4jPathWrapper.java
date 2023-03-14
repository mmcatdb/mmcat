package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class Neo4jPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();
    
    @Override
    public void addProperty(String hierarchy) {
        properties.add(hierarchy);
    }

    @Override
    public boolean check() {
        throw new UnsupportedOperationException();
    }

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return false; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGroupingAllowed() { return false; }
    @Override public boolean isDynamicNamingAllowed() { return false; }
    @Override public boolean isAnonymousNamingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return false; }
    @Override public boolean isComplexPropertyAllowed() { return true; } // Just for the _from and _to nodes, false otherwise.
    @Override public boolean isSchemaLess() { return true; }
    // CHECKSTYLE:ON
}
