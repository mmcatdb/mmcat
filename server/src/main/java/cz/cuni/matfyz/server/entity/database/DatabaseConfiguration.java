package cz.cuni.matfyz.server.entity.database;

import cz.cuni.matfyz.abstractWrappers.AbstractPathWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseConfiguration {

    public boolean isRootObjectAllowed;
    public boolean isRootMorphismAllowed;
	public boolean isPropertyToOneAllowed;
	public boolean isPropertyToManyAllowed;
	public boolean isInliningToOneAllowed;
	public boolean isInliningToManyAllowed;
	public boolean isGrouppingAllowed;
	public boolean isDynamicNamingAllowed;
	public boolean isAnonymousNamingAllowed;
	public boolean isReferenceAllowed;
    public boolean isComplexPropertyAllowed;

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
