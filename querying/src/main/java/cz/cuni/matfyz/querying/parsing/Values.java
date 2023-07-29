package cz.cuni.matfyz.querying.parsing;

import java.util.List;

public class Values extends QueryNode implements Statement {

    @Override Values asValues() {
        return this;
    }

    public final Variable variable;
    public final List<String> allowedValues;
    
    public Values(Variable variable, List<String> allowedValues) {
        this.variable = variable;
        this.allowedValues = allowedValues;
    }
    
}