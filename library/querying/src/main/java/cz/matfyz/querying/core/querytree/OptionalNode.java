package cz.matfyz.querying.core.querytree;

public class OptionalNode extends QueryNode {
 
    public final QueryNode primaryChild;
    public final QueryNode optionalChild;

    public OptionalNode(QueryNode primaryChild, QueryNode optionalChild) {
        this.primaryChild = primaryChild;
        this.optionalChild = optionalChild;
        
        primaryChild.setParent(this);
        optionalChild.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
