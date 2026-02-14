package cz.matfyz.evolution.propagation;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates propagation metrics across all datasources for thesis-ready reporting.
 * Collects per-datasource comparisons and produces a summary suitable for the thesis text.
 */
public class PropagationMetrics {

    private final List<PropagationComparison> comparisons = new ArrayList<>();
    private int totalSmoCount;

    public void setSmoCount(int count) {
        this.totalSmoCount = count;
    }

    public void addComparison(PropagationComparison comparison) {
        comparisons.add(comparison);
    }

    public List<PropagationComparison> comparisons() {
        return List.copyOf(comparisons);
    }

    public int datasourceCount() {
        return comparisons.size();
    }

    public long totalIncrementalStatements() {
        return comparisons.stream()
            .mapToLong(c -> c.incremental().statementsGenerated())
            .sum();
    }

    public long totalReExportStatements() {
        return comparisons.stream()
            .mapToLong(c -> c.reExport().statementsGenerated())
            .sum();
    }

    public long totalIncrementalTimeMs() {
        return comparisons.stream()
            .mapToLong(c -> c.incremental().executionTimeMs())
            .sum();
    }

    public long totalReExportTimeMs() {
        return comparisons.stream()
            .mapToLong(c -> c.reExport().executionTimeMs())
            .sum();
    }

    public double overallSpeedupRatio() {
        final long incrementalTime = totalIncrementalTimeMs();
        if (incrementalTime == 0)
            return totalReExportTimeMs() > 0 ? Double.POSITIVE_INFINITY : 1.0;
        return (double) totalReExportTimeMs() / incrementalTime;
    }

    /**
     * Produces a thesis-ready summary of all propagation metrics.
     */
    public String fullSummary() {
        final var sb = new StringBuilder();
        sb.append("=== Cross-Database Propagation Summary ===\n");
        sb.append(String.format("SMO operations applied: %d\n", totalSmoCount));
        sb.append(String.format("Datasources affected: %d\n\n", datasourceCount()));

        for (final var comparison : comparisons) {
            sb.append(comparison.summary()).append("\n\n");
        }

        sb.append("--- Totals ---\n");
        sb.append(String.format("Total incremental: %d statements, %dms\n",
            totalIncrementalStatements(), totalIncrementalTimeMs()));
        sb.append(String.format("Total re-export:   %d statements, %dms\n",
            totalReExportStatements(), totalReExportTimeMs()));
        sb.append(String.format("Overall speedup:   %.1fx faster, %d fewer statements\n",
            overallSpeedupRatio(),
            totalReExportStatements() - totalIncrementalStatements()));

        return sb.toString();
    }

}
