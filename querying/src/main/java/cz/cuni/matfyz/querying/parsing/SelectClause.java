package cz.cuni.matfyz.querying.parsing;

import cz.cuni.matfyz.querying.exception.GeneralException;

import java.util.List;

public class SelectClause extends QueryNode {

    @Override SelectClause asSelectClause() {
        return this;
    }

    public final List<SelectTriple> triples;
    public final List<Variable> variables;
    
    public SelectClause(List<SelectTriple> triples, List<Variable> variables) {
        this.triples = triples;
        this.variables = variables;
    }
    
    public Variable getRootVar() {
        for (SelectTriple triple : triples) {
            Variable variable = triple.subject;
            boolean isRootVar = true;

            for (SelectTriple t : triples) {
                if (t.object.equals(variable)) {
                    isRootVar = false;
                    break;
                }
            }

            if (isRootVar) {
                return variable;
            }
        }

        throw GeneralException.message("Cannot determine root variable of SELECT clause");
    }

}