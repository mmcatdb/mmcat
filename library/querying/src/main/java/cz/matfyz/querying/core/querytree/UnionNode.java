package cz.matfyz.querying.core.querytree;

import java.util.List;

public class UnionNode extends QueryNode {

    public final List<QueryNode> children;

    public UnionNode(List<QueryNode> children) {
        this.children = children;
        
        children.forEach(c -> setParent(this));
    }

}
