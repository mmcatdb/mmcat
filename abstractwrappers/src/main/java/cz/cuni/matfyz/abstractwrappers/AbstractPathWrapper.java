package cz.cuni.matfyz.abstractwrappers;

/**
 * @author pavel.koupil
 */
public interface AbstractPathWrapper {

    public abstract void addProperty(String hierarchy);

    public abstract boolean check();

    public abstract boolean isPropertyToOneAllowed();

    public abstract boolean isPropertyToManyAllowed();

    public abstract boolean isInliningToOneAllowed();

    public abstract boolean isInliningToManyAllowed();

    public abstract boolean isGroupingAllowed();

    public abstract boolean isDynamicNamingAllowed();

    public abstract boolean isAnonymousNamingAllowed();

    public abstract boolean isReferenceAllowed();

    public abstract boolean isComplexPropertyAllowed();

    public abstract boolean isSchemaLess();
    
}
