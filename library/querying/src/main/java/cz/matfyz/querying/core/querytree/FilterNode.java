package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.Expression.FunctionExpression;

public class FilterNode extends QueryNode {

    public final QueryNode child;
    public final FunctionExpression filter;

    public FilterNode(QueryNode child, FunctionExpression filter) {
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
