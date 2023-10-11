package cz.matfyz.querying.parsing;

import cz.matfyz.querying.core.QueryContext;

public class Query implements ParserNode {

    public final SelectClause select;
    public final WhereClause where;
    public final QueryContext context;

    Query(SelectClause select, WhereClause where, QueryContext context) {
        this.select = select;
        this.where = where;
        this.context = context;
    }

}