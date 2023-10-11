package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;

import java.util.Map;
import java.util.TreeMap;

public class Neo4jQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return true; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isRecursiveJoinSupported() { return true; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean IsFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    @Override
    protected Map<ComparisonOperator, String> defineComparisonOperators() {
        final var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.Equal, "=");
        output.put(ComparisonOperator.NotEqual, "<>");
        output.put(ComparisonOperator.Less, "<");
        output.put(ComparisonOperator.LessOrEqual, "<=");
        output.put(ComparisonOperator.Greater, ">");
        output.put(ComparisonOperator.GreaterOrEqual, ">=");
        return output;
    }

    @Override
    protected Map<AggregationOperator, String> defineAggregationOperators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryStatement createDSLStatement() {
        throw new UnsupportedOperationException();
    }

}