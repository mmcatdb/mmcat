package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Kind;

import java.util.Set;

/**
 * This class represents a pattern:
 *  {
 *      parent
 *      
 *      Operation { group }
 *  }
 */
public abstract class OperationNode extends QueryNode implements HasKinds {
    
    static enum Operation {
        Optional,
        Minus,
        Union,
        Join,
    }

    public abstract Operation operation();

    private final GroupNode group;

    public GroupNode group() {
        return group;
    }

    protected OperationNode(GroupNode group) {
        this.group = group;
    }

    public abstract OperationNode updateGroup(GroupNode group);

    /**
     * Which kinds are needed for this operation.
     */
    public Set<Kind> kinds() {
        return group.pattern.kinds;
    }

}
