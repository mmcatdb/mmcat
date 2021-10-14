package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import java.util.*;

/**
 *
 */
public class PostgreSQLPathWrapper implements AbstractPathWrapper
{
    private final List<String> properties = new ArrayList<>();
    
    @Override
	public void addProperty(String hierarchy)
    {
        this.properties.add(hierarchy);
    }

	@Override
    public boolean check()
    {
        return true; // This should be ok
    }
    
    @Override public boolean isRootObjectAllowed() { return true; }
	@Override public boolean isPropertyToOneAllowed() { return true; }
	@Override public boolean isPropertyToManyAllowed() { return false; }
	@Override public boolean isInliningToOneAllowed() { return true; }
	@Override public boolean isInliningToManyAllowed() { return false; }
	@Override public boolean isGrouppingAllowed() { return false; }
	@Override public boolean isDynamicNamingAllowed() { return false; }
	@Override public boolean isAnonymousNamingAllowed() { return false; }
	@Override public boolean isReferenceAllowed() { return true; }
}
