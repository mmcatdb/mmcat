package cz.matfyz.querying.parsing;

public class Query extends ParserNode {

    @Override Query asQuery() {
        return this;
    }

    public final SelectClause select;
    public final WhereClause where;

    public Query(SelectClause select, WhereClause where) {
        this.select = select;
        this.where = where;
    }

}