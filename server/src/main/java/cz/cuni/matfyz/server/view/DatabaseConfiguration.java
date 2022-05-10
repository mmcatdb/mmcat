package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.abstractWrappers.AbstractPathWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseConfiguration {

    public Boolean isRootObjectAllowed;
    public Boolean isRootMorphismAllowed;
	public Boolean isPropertyToOneAllowed;
	public Boolean isPropertyToManyAllowed;
	public Boolean isInliningToOneAllowed;
	public Boolean isInliningToManyAllowed;
	public Boolean isGrouppingAllowed;
	public Boolean isDynamicNamingAllowed;
	public Boolean isAnonymousNamingAllowed;
	public Boolean isReferenceAllowed;
    public Boolean isComplexPropertyAllowed;

    public DatabaseConfiguration(AbstractPathWrapper wrapper) {
        this.isRootObjectAllowed = wrapper.isRootObjectAllowed();
        this.isRootMorphismAllowed = wrapper.isRootMorphismAllowed();
        this.isPropertyToOneAllowed = wrapper.isPropertyToOneAllowed();
        this.isPropertyToManyAllowed = wrapper.isPropertyToManyAllowed();
        this.isInliningToOneAllowed = wrapper.isInliningToOneAllowed();
        this.isInliningToManyAllowed = wrapper.isInliningToManyAllowed();
        this.isGrouppingAllowed = wrapper.isGrouppingAllowed();
        this.isDynamicNamingAllowed = wrapper.isDynamicNamingAllowed();
        this.isAnonymousNamingAllowed = wrapper.isAnonymousNamingAllowed();
        this.isReferenceAllowed = wrapper.isReferenceAllowed();
        this.isComplexPropertyAllowed = wrapper.isComplexPropertyAllowed();
    }

}
