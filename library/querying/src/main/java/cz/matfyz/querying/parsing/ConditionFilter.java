package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.querying.parsing.ParserNode.Filter;

public class ConditionFilter implements Filter, Statement {

    @Override public ConditionFilter asConditionFilter() {
        return this;
    }

    public final Term lhs;
    public final ComparisonOperator operator;
    public final Term rhs;

    ConditionFilter(Term lhs, ComparisonOperator operator, Term rhs) {
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
    }

}
