package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.parsing.Filter;

public class FilterNode extends QueryNode {

    public final QueryNode child;
    public final Filter filter;

    public FilterNode(QueryNode child, Filter filter) {
        this.child = child;
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
