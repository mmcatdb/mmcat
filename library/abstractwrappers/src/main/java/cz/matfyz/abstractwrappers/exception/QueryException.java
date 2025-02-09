package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.core.querying.Computation.Operator;

import java.io.Serializable;

public class QueryException extends WrapperException {

    private QueryException(String name, Serializable data) {
        super("query." + name, data, null);
    }

    // TODO categorize by messages
    public static QueryException message(String message) {
        return new QueryException("message", message);
    }

    public static QueryException unsupportedOperator(Operator operator) {
        return new QueryException("unsupportedOperator", operator);
    }

    public static QueryException unsupportedOperator(String operator) {
        return new QueryException("unsupportedOperator", operator);
    }

    public static QueryException propertyNotFoundInMapping(Property property) {
        return new QueryException("propertyNotFoundInMapping", property);
    }

}
