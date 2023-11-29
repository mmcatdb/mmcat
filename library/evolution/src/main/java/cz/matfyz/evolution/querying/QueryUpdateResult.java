package cz.matfyz.evolution.querying;

import java.io.Serializable;
import java.util.List;

public class QueryUpdateResult {

    public final String nextContent;
    public final List<QueryUpdateError> errors;

    public QueryUpdateResult(String nextContent, List<QueryUpdateError> errors) {
        this.nextContent = nextContent;
        this.errors = errors;
    }

    public static record QueryUpdateError(
        ErrorType type,
        String message,
        Serializable data
    ) {}

    public enum ErrorType {
        ParseError,
        UpdateWarning,
        UpdateError,
    }

}
