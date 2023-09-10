package cz.matfyz.querying.parsing;

import cz.matfyz.querying.core.Clause;
import cz.matfyz.querying.core.Clause.ClauseType;

import java.util.List;

public class Query extends ParserNode {

    @Override Query asQuery() {
        return this;
    }

    public final SelectClause select;
    @Deprecated
    public final WhereClause where;

    public final Clause whereClause;
    
    public Query(SelectClause select, WhereClause where) {
        this.select = select;
        this.where = where;
        // TODO do properly
        this.whereClause = new Clause(ClauseType.WHERE, where.triples, List.of());
    }

}