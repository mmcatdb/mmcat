package cz.matfyz.evolution.instance;

import java.util.List;

import cz.matfyz.evolution.category.SMO;

/**
 * Interface for optimizing a sequence of SMO operations.
 * 
 * Implementations can use different strategies:
 * - Rule-based: Simple pattern matching (CreateObjex + DeleteObjex → skip both)
 * - Graph-based: Build dependency DAG, topological sort, detect cancellations
 * - Future: More sophisticated algorithms if needed
 * 
 * The optimizer receives a batch of operations and returns an optimized sequence
 * that minimizes database operations while maintaining correctness.
 */
public interface OperationOptimizer {

    /**
     * Analyze and optimize a sequence of SMO operations.
     * 
     * @param operations The original sequence of operations
     * @return Optimized result containing the new sequence and statistics
     */
    OptimizationResult optimize(List<SMO> operations);

    /**
     * Result of optimization containing the optimized operations and statistics.
     */
    record OptimizationResult(
        /** The optimized sequence of operations (may be reordered, some may be removed). */
        List<SMO> operations,
        /** Number of operations that were cancelled (Create+Delete pairs). */
        int cancelledCount,
        /** Number of operations that were reordered for efficiency. */
        int reorderedCount,
        /** Original operation count before optimization. */
        int originalCount
    ) {
        public int optimizedCount() {
            return operations.size();
        }
        
        public double reductionRatio() {
            return originalCount > 0 ? 1.0 - (double) optimizedCount() / originalCount : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "OptimizationResult[%d→%d ops (%.1f%% reduction), cancelled=%d, reordered=%d]",
                originalCount, optimizedCount(), reductionRatio() * 100, cancelledCount, reorderedCount);
        }
    }

    /**
     * Comparison of optimization results between baseline (no optimization) and optimized execution.
     * Useful for thesis metrics: "With GraphBasedOptimizer, operations reduced from X to Y (Z% reduction)".
     */
    record OptimizationComparison(
        /** Result without optimization (baseline). */
        OptimizationResult baseline,
        /** Result with optimization. */
        OptimizationResult optimized
    ) {
        /** Number of operations saved by optimization. */
        public int operationsSaved() {
            return baseline.originalCount() - optimized.optimizedCount();
        }

        /** Reduction ratio compared to baseline. */
        public double reductionRatio() {
            return baseline.originalCount() > 0 
                ? 1.0 - (double) optimized.optimizedCount() / baseline.originalCount() 
                : 0.0;
        }

        /** Human-readable summary for logging/thesis. */
        public String summary() {
            return String.format(
                "Optimization comparison: WITHOUT optimizer: %d operations, WITH optimizer: %d operations " +
                "(saved %d ops, %.1f%% reduction, %d cancellations, %d reorderings)",
                baseline.originalCount(),
                optimized.optimizedCount(),
                operationsSaved(),
                reductionRatio() * 100,
                optimized.cancelledCount(),
                optimized.reorderedCount()
            );
        }

        @Override
        public String toString() {
            return summary();
        }
    }

    /**
     * Compare optimization: run both NoOpOptimizer (baseline) and this optimizer,
     * returning both results for comparison.
     * 
     * @param operations The operations to optimize
     * @return Comparison with baseline and optimized results
     */
    default OptimizationComparison compareWithBaseline(List<SMO> operations) {
        OptimizationResult baseline = new NoOpOptimizer().optimize(operations);
        OptimizationResult optimized = this.optimize(operations);
        return new OptimizationComparison(baseline, optimized);
    }
}
