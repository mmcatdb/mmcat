package cz.cuni.matfyz.server.view;

import java.io.Serializable;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseConfiguration implements Serializable {

    public final Boolean isRootObjectAllowed;
	public final Boolean isPropertyToOneAllowed;
	public final Boolean isPropertyToManyAllowed;
	public final Boolean isInliningToOneAllowed;
	public final Boolean isInliningToManyAllowed;
	public final Boolean isGrouppingAllowed;
	public final Boolean isDynamicNamingAllowed;
	public final Boolean isAnonymousNamingAllowed;
	public final Boolean isReferenceAllowed;

    public DatabaseConfiguration(AbstractPathWrapper wrapper) {
        this.isRootObjectAllowed = wrapper.isRootObjectAllowed();
        this.isPropertyToOneAllowed = wrapper.isPropertyToOneAllowed();
        this.isPropertyToManyAllowed = wrapper.isPropertyToManyAllowed();
        this.isInliningToOneAllowed = wrapper.isInliningToOneAllowed();
        this.isInliningToManyAllowed = wrapper.isInliningToManyAllowed();
        this.isGrouppingAllowed = wrapper.isGrouppingAllowed();
        this.isDynamicNamingAllowed = wrapper.isDynamicNamingAllowed();
        this.isAnonymousNamingAllowed = wrapper.isAnonymousNamingAllowed();
        this.isReferenceAllowed = wrapper.isReferenceAllowed();
    }

}
