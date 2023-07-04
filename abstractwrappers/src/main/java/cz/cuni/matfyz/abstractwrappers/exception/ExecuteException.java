package cz.cuni.matfyz.abstractwrappers.exception;

import cz.cuni.matfyz.abstractwrappers.AbstractStatement;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public class ExecuteException extends WrapperException {

    private record ExecuteData(
        String message,
        Collection<AbstractStatement> statements
    ) implements Serializable {}

    public ExecuteException(Exception exception, Collection<AbstractStatement> statements) {
        super("execute", new ExecuteData(exception.getMessage(), statements), exception);
    }

    // TODO - differentiate from "connection refused"
}
