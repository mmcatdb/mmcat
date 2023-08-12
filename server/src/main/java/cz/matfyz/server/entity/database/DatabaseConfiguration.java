package cz.matfyz.server.entity.database;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;

/**
 * @author jachym.bartik
 */
public class DatabaseConfiguration {

    public final boolean isPropertyToOneAllowed;
    public final boolean isPropertyToManyAllowed;
    public final boolean isInliningToOneAllowed;
    public final boolean isInliningToManyAllowed;
    public final boolean isGroupingAllowed;
    public final boolean isDynamicNamingAllowed;
    public final boolean isAnonymousNamingAllowed;
    public final boolean isReferenceAllowed;
    public final boolean isComplexPropertyAllowed;
    public final boolean isSchemaLess;

    public DatabaseConfiguration(AbstractPathWrapper wrapper) {
        this.isPropertyToOneAllowed = wrapper.isPropertyToOneAllowed();
        this.isPropertyToManyAllowed = wrapper.isPropertyToManyAllowed();
        this.isInliningToOneAllowed = wrapper.isInliningToOneAllowed();
        this.isInliningToManyAllowed = wrapper.isInliningToManyAllowed();
        this.isGroupingAllowed = wrapper.isGroupingAllowed();
        this.isDynamicNamingAllowed = wrapper.isDynamicNamingAllowed();
        this.isAnonymousNamingAllowed = wrapper.isAnonymousNamingAllowed();
        this.isReferenceAllowed = wrapper.isReferenceAllowed();
        this.isComplexPropertyAllowed = wrapper.isComplexPropertyAllowed();
        this.isSchemaLess = wrapper.isSchemaLess();
    }

}
