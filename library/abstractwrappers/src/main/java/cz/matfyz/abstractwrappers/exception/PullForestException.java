package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;

import java.io.Serializable;

public class PullForestException extends WrapperException {

    public PullForestException(String name, Serializable data, Exception exception) {
        super("pullForest." + name, data, exception);
    }

    public static PullForestException inner(Exception exception) {
        return exception instanceof PullForestException pullForestException
            ? pullForestException
            : new PullForestException("inner", exception.getMessage(), exception);
    }

    private record InvalidQueryData(
        String wrapper,
        String query
    ) implements Serializable {}

    public static PullForestException invalidQuery(AbstractPullWrapper wrapper, QueryContent query) {
        final var data = new InvalidQueryData(wrapper.getClass().getSimpleName(), query.getClass().getSimpleName());
        return new PullForestException("invalidQuery", data, null);
    }

}
