package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.ComplexProperty;

/**
 * Defines which operations are allowed for an access path for the specific database system.
 * Also can be used to check whether a given access path is valid.
 */
public interface AbstractPathWrapper {

    // TODO Create a base check method that will use all of these "isAllowed" methods for a simple basic check. Then use the specific implementation for more detailed rules.
    // TODO Use some basic constraint for characters in the names of properties (although this probably needs to be specifc for each database system).
    // TODO Also check for uniqueness of the names.

    /**
     * Check whether the access path is valid.
     */
    boolean isPathValid(ComplexProperty accessPath);

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
    boolean isSchemaless();

}
