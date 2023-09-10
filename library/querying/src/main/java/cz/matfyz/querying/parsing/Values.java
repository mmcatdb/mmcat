package cz.matfyz.querying.parsing;

import java.util.List;

public class Values extends ParserNode implements Statement {

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