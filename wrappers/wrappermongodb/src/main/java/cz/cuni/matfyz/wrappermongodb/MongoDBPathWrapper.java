package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class MongoDBPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();
    
    @Override
    public void addProperty(String hierarchy) {
        properties.add(hierarchy);
    }

    @Override
    public boolean check() {
        return properties.stream().anyMatch(property -> "_id".equals(property.substring(property.indexOf('/') + 1)));
    }

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return true; }
    @Override public boolean isGroupingAllowed() { return true; }
    @Override public boolean isDynamicNamingAllowed() { return true; }
    @Override public boolean isAnonymousNamingAllowed() { return true; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaLess() { return true; }
    // CHECKSTYLE:ON
}
