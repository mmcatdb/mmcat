package cz.matfyz.abstractwrappers;

/**
 * @author pavel.koupil
 */
public interface AbstractPathWrapper {

    void addProperty(String hierarchy);

    boolean check();

    /**
     * A property of the access path can have a signature of a base morphism with cardinality "something to 1".
     */
    boolean isPropertyToOneAllowed();

    /**
     * A property of the access path can have a signature of a base morphism with cardinality "something to n".
     */
    boolean isPropertyToManyAllowed();

    /**
     * A property of the access path can have a signature of a composed morphism with cardinality "something to 1".
     */
    boolean isInliningToOneAllowed();

    /**
     * A property of the access path can have a signature of a composed morphism with cardinality "something to n".
     */
    boolean isInliningToManyAllowed();

    /**
     * Multiple properties can be nested into an auxiliary property that does not correspond to any object in the schema category (so it is created just for the mapping).
     * Note that this rule cannot be true unless the complex properties are also allowed.
     */
    boolean isGroupingAllowed();

    /**
     * Names of properties can be dynamic, meaning that the name of the property is derived from the data (so it is basically just another value).
     */
    boolean isDynamicNamingAllowed();

    /**
     * Properties can be anonymous, meaning they have "no name". This is the case for, e.g., property representing elements in an array.
     * Note that the root property of an access path can be anonymous regardless of this rule (although it might be worthy to investigate if that makes sense).
     */
    boolean isAnonymousNamingAllowed();

    /**
     * References for the IC algorithm can be made.
     * Currently not used.
     */
    boolean isReferenceAllowed();

    /**
     * It is possible to create complex properties, i.e., nesting of multiple properties into a tree hierarchy (instead of just flat hierarchy known from the SQL world).
     */
    boolean isComplexPropertyAllowed();

    /**
     * The database system is schema less, meaning there are no such thinks like IC and DDL statements.
     * Although some DDL statements might still exist (e.g., create collection in MongoDB).
     */
    boolean isSchemaLess();

}
