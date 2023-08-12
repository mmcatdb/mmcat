package cz.matfyz.querying.parsing;

import java.util.List;

public class Query extends QueryNode {

    @Override Query asQuery() {
        return this;
    }

    public final SelectClause select;
    public final WhereClause where;
    public final List<Variable> variables;
    
    public Query(SelectClause select, WhereClause where, List<Variable> variables) {
        this.select = select;
        this.where = where;
        this.variables = variables;
    }

}