package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Variable.VariableScope;

public class ParsedQuery implements ParserNode {

    public final SelectClause select;
    public final WhereClause where;
    public final VariableScope variableScope;

    public ParsedQuery(SelectClause select, WhereClause where, VariableScope variableScope) {
        this.select = select;
        this.where = where;
        this.variableScope = variableScope;
    }

}
