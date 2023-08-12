package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
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

}
