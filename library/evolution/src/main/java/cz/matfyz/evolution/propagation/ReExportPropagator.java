package cz.matfyz.evolution.propagation;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.transformations.InstanceToDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Baseline propagation approach: DROP existing tables/collections, then re-create
 * and re-insert all data using the existing InstanceToDatabase pipeline.
 *
 * This is the "naive" approach for thesis comparison with the incremental EvolutionPropagator.
 * It is simpler but generates more statements and takes longer for large datasets.
 */
public class ReExportPropagator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReExportPropagator.class);

    /**
     * Propagate by re-exporting the evolved instance to the database.
     *
     * @param evolvedInstance The in-memory instance after evolution
     * @param mappings The mappings for this datasource
     * @param controlWrapper The database control wrapper
     * @param datasourceLabel Human-readable label for this datasource
     * @param datasourceType The type of datasource (e.g., "postgresql", "mongodb")
     * @return PropagationResult with generated statements and timing
     */
    public PropagationResult propagate(
        InstanceCategory evolvedInstance,
        Collection<Mapping> mappings,
        AbstractControlWrapper controlWrapper,
        String datasourceLabel,
        String datasourceType
    ) {
        final long startTime = System.currentTimeMillis();

        try {
            final var allStatements = new ArrayList<AbstractStatement>();

            // Step 1: Generate DROP statements for all existing kinds
            final var evolutionWrapper = controlWrapper.getEvolutionWrapper();
            for (final Mapping mapping : mappings) {
                allStatements.addAll(evolutionWrapper.evolveDeleteObjex(mapping.kindName()));
            }

            // Step 2: Use existing InstanceToDatabase to generate CREATE + INSERT
            final var exportResult = new InstanceToDatabase()
                .input(
                    mappings,
                    evolvedInstance,
                    controlWrapper.getDDLWrapper(),
                    controlWrapper.getDMLWrapper(),
                    controlWrapper.getICWrapper()
                )
                .run();

            allStatements.addAll(exportResult.statements());

            final long elapsed = System.currentTimeMillis() - startTime;
            LOGGER.info("Re-export propagation for {}: {} statements in {}ms",
                datasourceLabel, allStatements.size(), elapsed);

            return PropagationResult.success(datasourceLabel, datasourceType, allStatements, elapsed);
        } catch (Exception e) {
            final long elapsed = System.currentTimeMillis() - startTime;
            LOGGER.error("Re-export propagation failed for {}: {}", datasourceLabel, e.getMessage());
            return PropagationResult.failure(datasourceLabel, datasourceType, List.of(), elapsed, e.getMessage());
        }
    }

}
