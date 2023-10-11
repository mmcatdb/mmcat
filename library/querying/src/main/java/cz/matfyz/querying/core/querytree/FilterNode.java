package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.parsing.ParserNode.Filter;

public class FilterNode extends QueryNode {

    public final QueryNode child;
    public final Filter filter;

    public FilterNode(QueryNode child, Filter filter) {
        this.child = child;
        this.filter = filter;

        child.setParent(this);
    }

    @Override
    public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
