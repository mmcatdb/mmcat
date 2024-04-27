package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;

import java.io.Serializable;

public class QueryException extends WrapperException {

    private QueryException(String name, Serializable data) {
        super("query." + name, data, null);
    }

    // TODO categorize by messages
    public static QueryException message(String message) {
        return new QueryException("message", message);
    }

    public static QueryException unsupportedOperator(ComparisonOperator operator) {
        return new QueryException("unsupportedOperator", operator);
    }

    public static QueryException unsupportedOperator(AggregationOperator operator) {
        return new QueryException("unsupportedOperator", operator);
    }

    public static QueryException propertyNotFoundInMapping(Property property) {
        return new QueryException("propertyNotFoundInMapping", property);
    }

}
