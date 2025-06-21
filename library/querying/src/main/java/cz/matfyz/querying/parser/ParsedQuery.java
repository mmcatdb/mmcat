package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Expression.ExpressionScope;

public class ParsedQuery implements ParserNode {

    public final SelectClause select;
    public final WhereClause where;
    public final ExpressionScope scope;

    public ParsedQuery(SelectClause select, WhereClause where, ExpressionScope scope) {
        this.select = select;
        this.where = where;
        this.scope = scope;
    }

}
