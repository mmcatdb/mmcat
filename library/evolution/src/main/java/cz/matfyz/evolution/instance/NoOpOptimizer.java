package cz.matfyz.evolution.instance;

import java.util.List;

import cz.matfyz.evolution.category.SMO;

/**
 * No-op optimizer that returns operations unchanged.
 *
 * Used as baseline for comparison with GraphBasedOptimizer to measure
 * the effectiveness of optimization (cancelled operations, reordering benefits).
 *
 * Strategy pattern: Implements OperationOptimizer interface.
 */
public class NoOpOptimizer implements OperationOptimizer {

    @Override
    public OptimizationResult optimize(List<SMO> operations) {
        // Return operations unchanged - no optimization
        return new OptimizationResult(
            operations,
            0,  // cancelledCount
            0,  // reorderedCount
            operations.size()  // originalCount
        );
    }

}
