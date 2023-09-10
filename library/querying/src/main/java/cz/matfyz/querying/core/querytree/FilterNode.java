package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.core.filter.Filter;

public class FilterNode extends QueryNode {

    public final QueryNode child;
    public final Filter filter;

    private FilterNode(QueryNode child, Filter filter) {
        this.child = child;
        this.filter = filter;

        child.setParent(this);
    }

}
