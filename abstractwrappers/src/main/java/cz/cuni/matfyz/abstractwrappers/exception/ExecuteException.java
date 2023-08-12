package cz.cuni.matfyz.abstractwrappers.exception;

import cz.cuni.matfyz.abstractwrappers.AbstractStatement;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public class ExecuteException extends WrapperException {

    private record ExecuteStatementsData(
        String message,
        Collection<AbstractStatement> statements
    ) implements Serializable {}

    public ExecuteException(Exception exception, Collection<AbstractStatement> statements) {
        super("executeStatements", new ExecuteStatementsData(exception.getMessage(), statements), exception);
    }

    private record ExecuteScriptData(
        String message,
        String script
    ) implements Serializable {}

    public ExecuteException(Exception exception, Path script) {
        super("executeScript", new ExecuteScriptData(exception.getMessage(), script.toString()), exception);
    }

    // TODO - differentiate from "connection refused"
}
