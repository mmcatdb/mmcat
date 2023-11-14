package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;

import java.util.Map;
import java.util.TreeMap;

public class MongoDBQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return false; }
    @Override public boolean isRecursiveJoinSupported() { return false; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean IsFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    @Override
    protected Map<ComparisonOperator, String> defineComparisonOperators() {
        final var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.Equal, "$eq");
        output.put(ComparisonOperator.NotEqual, "$ne");
        output.put(ComparisonOperator.Less, "$lt");
        output.put(ComparisonOperator.LessOrEqual, "$lte");
        output.put(ComparisonOperator.Greater, "$gt");
        output.put(ComparisonOperator.GreaterOrEqual, "$gte");
        return output;
    }

    @Override
    protected Map<AggregationOperator, String> defineAggregationOperators() {
        // TODO fix
        return new TreeMap<>();
    }

    public QueryStatement createDSLStatement() {
        throw new UnsupportedOperationException("MongoDBQueryWrapper.createDSLStatement not implemented.");
    }

}