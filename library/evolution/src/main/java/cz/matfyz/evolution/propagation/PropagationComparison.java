package cz.matfyz.evolution.propagation;

/**
 * Comparison of incremental vs re-export propagation for a single datasource.
 * Provides thesis-ready metrics showing the efficiency of incremental migration.
 */
public record PropagationComparison(
    PropagationResult incremental,
    PropagationResult reExport,
    String datasourceLabel
) {

    public long timeSaved() {
        return reExport.executionTimeMs() - incremental.executionTimeMs();
    }

    public double speedupRatio() {
        if (incremental.executionTimeMs() == 0)
            return reExport.executionTimeMs() > 0 ? Double.POSITIVE_INFINITY : 1.0;
        return (double) reExport.executionTimeMs() / incremental.executionTimeMs();
    }

    public int statementsReduced() {
        return reExport.statementsGenerated() - incremental.statementsGenerated();
    }

    public String summary() {
        final var sb = new StringBuilder();
        sb.append(String.format("Datasource: %s (%s)%n",
            datasourceLabel, incremental.datasourceType()));
        sb.append(String.format("  Incremental: %d statements, %dms%n",
            incremental.statementsGenerated(), incremental.executionTimeMs()));
        sb.append(String.format("  Re-export:   %d statements, %dms%n",
            reExport.statementsGenerated(), reExport.executionTimeMs()));
        sb.append(String.format("  Speedup:     %.1fx faster, %d fewer statements",
            speedupRatio(), statementsReduced()));
        return sb.toString();
    }

}
