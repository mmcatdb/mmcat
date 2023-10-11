package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.querying.parsing.ParserNode.Term;

public class Aggregation implements Term {

    @Override public Aggregation asAggregation() {
        return this;
    }

    public final AggregationOperator operator;
    public final Variable variable;
    public final boolean isDistinct;
    
    Aggregation(AggregationOperator operator, Variable variable, boolean isDistinct) {
        this.operator = operator;
        this.variable = variable;
        this.isDistinct = isDistinct;
    }

    @Override
    public String getIdentifier() {
        return "a_" + operator + "_" + variable.name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Aggregation aggregation && variable.equals(aggregation.variable) && operator == aggregation.operator;
    }

    @Override
    public String toString() {
        return operator.toString() + "(" + variable.toString() + ")";
    }

}