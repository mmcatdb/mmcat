package cz.matfyz.querying.core.filter;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.querying.parsing.ValueNode;

public class ConditionFilter extends Filter {
    
    public final ValueNode lhs;
    public final ComparisonOperator operator;
    public final ValueNode rhs;
    
    public ConditionFilter(ValueNode lhs, ComparisonOperator operator, ValueNode rhs) {
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
    }

}
