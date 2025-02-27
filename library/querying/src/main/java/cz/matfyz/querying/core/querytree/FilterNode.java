package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.Computation;

public class FilterNode extends QueryNode {

    public QueryNode child() { return children.get(0); }
    public QueryNode setChild(QueryNode node) { return children.set(0, node); }
    public final Computation filter;

    public FilterNode(QueryNode child, Computation filter) {
        this.children.add(child);
        this.filter = filter;

        child.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedFilterNode(
        SerializedQueryNode child,
        String filter
    ) implements SerializedQueryNode{

        @Override public String getType() { return "filter"; }

    }

}
