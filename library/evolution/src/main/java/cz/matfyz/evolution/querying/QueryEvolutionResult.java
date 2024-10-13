package cz.matfyz.evolution.querying;

import java.io.Serializable;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class QueryEvolutionResult {

    public final String nextContent;
    public final List<QueryEvolutionError> errors;

    public QueryEvolutionResult(String nextContent, List<QueryEvolutionError> errors) {
        this.nextContent = nextContent;
        this.errors = errors;
    }

    public record QueryEvolutionError(
        ErrorType type,
        String message,
        @Nullable Serializable data
    ) {}

    public enum ErrorType {
        ParseError,
        UpdateWarning,
        UpdateError,
    }

}
