package cz.matfyz.tests.evolution;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs all 5 experiments (E1-E5) at multiple data scales.
 *
 * Writes results to CSV files in experiments/results/.
 *
 * Prerequisites:
 * - Docker containers running (docker compose up -d)
 * - Node.js with npx available
 * - npm deps installed (cd experiments/benchmarkDataSetup && npm install)
 *
 * Run: mvn test -pl tests -Dtest="cz.matfyz.tests.evolution.YelpScaledExperimentRunner"
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YelpScaledExperimentRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(YelpScaledExperimentRunner.class);

    @Test
    @Order(0)
    void initCsvHeaders() throws Exception {
        ExperimentHelper.writeCsvHeader("e1_propagation_performance.csv",
            "scale", "smo_type", "datasource", "statements", "total_time_ms");
        ExperimentHelper.writeCsvHeader("e2_optimizer_comparison.csv",
            "scale", "scenario", "baseline_ops", "optimized_ops",
            "cancelled", "reordered", "reduction_ratio", "description");
        ExperimentHelper.writeCsvHeader("e3_data_integrity.csv",
            "scale", "smo_type", "datasource", "rows_before", "rows_after",
            "null_values", "passed");
        ExperimentHelper.writeCsvHeader("e4_join_split_correctness.csv",
            "scale", "datasource", "samples_tested", "samples_passed", "all_correct");
        ExperimentHelper.writeCsvHeader("e5_incremental_vs_reexport.csv",
            "scale", "smo_type", "datasource", "incr_statements", "incr_time_ms",
            "reexport_statements", "reexport_time_ms", "speedup_ratio");

        LOGGER.info("CSV headers initialized");
    }

    @Test
    @Order(1)
    void scale1x() throws Exception {
        runExperiments(1.0);
    }

    @Test
    @Order(2)
    void scale5x() throws Exception {
        runExperiments(5.0);
    }

    @Test
    @Order(3)
    void scale10x() throws Exception {
        runExperiments(10.0);
    }

    private void runExperiments(double scale) throws Exception {
        LOGGER.info("===== Loading data at {}x scale =====", scale);
        ExperimentHelper.reloadYelpData(scale);

        final var suite = new YelpExperimentalEvaluationTests(scale);
        suite.runAll();

        LOGGER.info("===== {}x scale complete =====", scale);
    }
}
