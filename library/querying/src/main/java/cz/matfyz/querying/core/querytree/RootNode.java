package cz.matfyz.querying.core.querytree;

public class RootNode extends QueryNode {
    
    public final QueryNode child;

    public RootNode(QueryNode child) {
        this.child = child;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}
