package cz.matfyz.evolution.propagation;

import cz.matfyz.abstractwrappers.AbstractStatement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Result of propagating evolution operations to a single datasource.
 * Used for both incremental (per-SMO) and re-export (DROP+CREATE+INSERT) approaches.
 */
public record PropagationResult(
    String datasourceLabel,
    String datasourceType,
    List<AbstractStatement> statements,
    long executionTimeMs,
    boolean success,
    String error
) {

    public int statementsGenerated() {
        return statements.size();
    }

    public String statementsAsString() {
        return statements.stream()
            .map(AbstractStatement::getContent)
            .collect(Collectors.joining("\n"));
    }

    public String summary() {
        if (!success) {
            return String.format("  %s (%s): FAILED - %s", datasourceLabel, datasourceType, error);
        }
        return String.format("  %s (%s): %d statements, %dms",
            datasourceLabel, datasourceType, statementsGenerated(), executionTimeMs);
    }

    public static PropagationResult success(
        String datasourceLabel, String datasourceType,
        List<AbstractStatement> statements, long executionTimeMs
    ) {
        return new PropagationResult(datasourceLabel, datasourceType, statements, executionTimeMs, true, null);
    }

    public static PropagationResult failure(
        String datasourceLabel, String datasourceType,
        List<AbstractStatement> statements, long executionTimeMs, String error
    ) {
        return new PropagationResult(datasourceLabel, datasourceType, statements, executionTimeMs, false, error);
    }

}
