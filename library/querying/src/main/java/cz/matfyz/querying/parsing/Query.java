package cz.matfyz.querying.parsing;

public class Query implements ParserNode {

    public final SelectClause select;
    public final WhereClause where;

    Query(SelectClause select, WhereClause where) {
        this.select = select;
        this.where = where;
    }

}