package cz.matfyz.querying.planner;

import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.QueryNode;

public class QueryPlan {

    public QueryNode root;
    public final QueryContext context;
    public final ExpressionScope scope;

    public QueryPlan(QueryNode root, QueryContext context, ExpressionScope scope) {
        this.root = root;
        this.context = context;
        this.scope = scope.clone();
    }

}
