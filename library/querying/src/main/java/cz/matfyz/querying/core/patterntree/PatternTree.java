package cz.matfyz.querying.core.patterntree;

import cz.matfyz.abstractwrappers.database.Kind;

/**
 * This class represents part of a query pattern that is mapped to a particular kind.
 * It's also mapped to a schema category.
 * Because each kind has a mappings which is a tree, this class is also a tree.
 */
public class PatternTree {
    
    public final Kind kind;
    public final PatternObject root;

    public PatternTree(Kind kind, PatternObject root) {
        this.kind = kind;
        this.root = root;
    }

}
