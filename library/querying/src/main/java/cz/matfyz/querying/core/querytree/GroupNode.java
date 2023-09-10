package cz.matfyz.querying.core.querytree;

import java.util.List;

/**
 * This class represents a query part. Generally, it can be:
 *  {
 *      pattern
 *      Operation { ... }
 *      FILTER( ... )
 *  }
 */
public class GroupNode extends QueryNode {

    public boolean isRoot() {
        return parent == null;
    }
    
    public final PatternNode pattern;

    public final List<OperationNode> operations;

    public final List<Filter> filters;

    public GroupNode(PatternNode pattern, List<OperationNode> operations, List<Filter> filters) {
        this.pattern = pattern;
        this.operations = operations;
        this.filters = filters;
    }

}
