package cz.cuni.matfyz.querying.parsing;

import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;

public class Filter extends QueryNode implements Statement {

    @Override public Filter asFilter() {
        return this;
    }

    public final ValueNode lhs;
    public final ComparisonOperator operator;
    public final ValueNode rhs;
    
    public Filter(ValueNode lhs, ComparisonOperator operator, ValueNode rhs) {
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
    }

}