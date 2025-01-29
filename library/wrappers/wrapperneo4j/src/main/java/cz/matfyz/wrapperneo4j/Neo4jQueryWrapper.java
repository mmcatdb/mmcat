package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.querying.Expression.Operator;

public class Neo4jQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return true; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isRecursiveJoinSupported() { return true; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean isFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    private static final Operators operators = new Operators();

    static {

        operators.define(Operator.Equal, "=");
        operators.define(Operator.NotEqual, "<>");
        operators.define(Operator.Less, "<");
        operators.define(Operator.LessOrEqual, "<=");
        operators.define(Operator.Greater, ">");
        operators.define(Operator.GreaterOrEqual, ">=");

        // TODO Aggragation operators.

        operators.define(Operator.In, "IN");
        operators.define(Operator.NotIn, "NOT IN");

    }

    @Override public QueryStatement createDSLStatement() {
        throw new UnsupportedOperationException("Neo4jQueryWrapper.createDSLStatement not implemented.");
    }

}
