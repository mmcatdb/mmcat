package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.abstractWrappers.AbstractPathWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseConfiguration {

    public Boolean isRootObjectAllowed;
	public Boolean isPropertyToOneAllowed;
	public Boolean isPropertyToManyAllowed;
	public Boolean isInliningToOneAllowed;
	public Boolean isInliningToManyAllowed;
	public Boolean isGrouppingAllowed;
	public Boolean isDynamicNamingAllowed;
	public Boolean isAnonymousNamingAllowed;
	public Boolean isReferenceAllowed;

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
