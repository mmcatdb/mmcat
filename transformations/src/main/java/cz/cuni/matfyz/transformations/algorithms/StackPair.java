package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.mapping.AccessPath;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
class StackPair {

    Set<String> names;
    AccessPath accessPath;
    
    StackPair(Set<String> names, AccessPath accessPath) {
        this.names = names;
        this.accessPath = accessPath;
    }
}
