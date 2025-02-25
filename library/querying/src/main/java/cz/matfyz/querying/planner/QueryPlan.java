package cz.matfyz.querying.planner;

import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.QueryNode;

public class QueryPlan {

    public QueryNode root;
    public final QueryContext context;

    public QueryPlan(QueryNode root, QueryContext context) {
        this.root = root;
        this.context = context;
    }

}
