package cz.matfyz.querying.parsing;

import java.util.List;

public class WhereClause extends QueryNode {

    @Override WhereClause asWhereClause() {
        return this;
    }

    public final List<WhereTriple> triples;
    public final List<Variable> variables;
    public final List<Filter> filters;
    public final List<Values> values;
    
    public WhereClause(List<WhereTriple> triples, List<Variable> variables, List<Filter> filters, List<Values> values) {
        this.triples = triples;
        this.variables = variables;
        this.filters = filters;
        this.values = values;
    }
    
}