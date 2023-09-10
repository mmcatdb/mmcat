package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Kind;

import java.util.Set;

public abstract class Filter implements HasKinds {
    
    /**
     * Which kinds are needed by this filter.
     */
    public abstract Set<Kind> kinds();

}
