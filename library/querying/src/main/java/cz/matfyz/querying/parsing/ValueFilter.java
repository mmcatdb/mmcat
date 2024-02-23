package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Filter;

import java.util.List;

public class ValueFilter implements Filter, Statement {

    @Override public ValueFilter asValueFilter() {
        return this;
    }

    // TODO why this can't be an aggregation?
    public final Variable variable;
    public final List<String> allowedValues;

    ValueFilter(Variable variable, List<String> allowedValues) {
        this.variable = variable;
        this.allowedValues = allowedValues;
    }

}
