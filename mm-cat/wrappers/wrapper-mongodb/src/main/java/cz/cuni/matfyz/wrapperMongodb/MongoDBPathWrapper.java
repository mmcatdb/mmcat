package cz.cuni.matfyz.wrapperMongoDB;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBPathWrapper implements AbstractPathWrapper
{
    private final List<String> properties = new ArrayList<>();
    
    @Override
	public void addProperty(String hierarchy)
    {
        properties.add(hierarchy);
    }

	@Override
    public boolean check()
    {
        return properties.stream().anyMatch(property -> property.substring(property.indexOf('/') + 1) == "_id");
    }

	@Override public boolean isRootObjectAllowed() { return true; }
	@Override public boolean isPropertyToOneAllowed() { return true; }
	@Override public boolean isPropertyToManyAllowed() { return true; }
	@Override public boolean isInliningToOneAllowed() { return true; }
	@Override public boolean isInliningToManyAllowed() { return true; }
	@Override public boolean isGrouppingAllowed() { return true; }
	@Override public boolean isDynamicNamingAllowed() { return true; }
	@Override public boolean isAnonymousNamingAllowed() { return true; }
	@Override public boolean isReferenceAllowed() { return true; }
}
