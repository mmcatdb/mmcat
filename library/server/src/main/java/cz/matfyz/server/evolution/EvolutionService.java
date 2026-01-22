package cz.matfyz.server.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.category.SchemaEvolutionAlgorithm;
import cz.matfyz.evolution.instance.GraphBasedOptimizer;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.evolution.instance.NoOpOptimizer;
import cz.matfyz.evolution.instance.OperationOptimizer;
import cz.matfyz.evolution.instance.OperationOptimizer.OptimizationComparison;
import cz.matfyz.evolution.instance.OperationOptimizer.OptimizationResult;
import cz.matfyz.evolution.propagation.EvolutionPropagator;
import cz.matfyz.evolution.propagation.MappingEvolver;
import cz.matfyz.evolution.propagation.PropagationComparison;
import cz.matfyz.evolution.propagation.PropagationMetrics;
import cz.matfyz.evolution.propagation.PropagationResult;
import cz.matfyz.evolution.propagation.ReExportPropagator;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.WrapperService;
import cz.matfyz.server.instance.InstanceCategoryEntity;
import cz.matfyz.server.instance.InstanceCategoryRepository;
import cz.matfyz.server.job.Job;
import cz.matfyz.server.job.Run;
import cz.matfyz.server.mapping.MappingEntity;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryRepository;
import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

@Service
public class EvolutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionService.class);

    @Autowired
    private EvolutionRepository repository;

    @Autowired
    private SchemaCategoryRepository schemaRepository;

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private InstanceCategoryRepository instanceRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private WrapperService wrapperService;

    /**
     * Strategy pattern: optimizer to use for operation optimization.
     * Default: GraphBasedOptimizer (with cancellation detection and reordering).
     * Can be changed to NoOpOptimizer for baseline comparison.
     */
    private OperationOptimizer optimizer = new GraphBasedOptimizer();

    /**
     * Set the optimizer strategy.
     * Use GraphBasedOptimizer for optimization, NoOpOptimizer for baseline.
     */
    public void setOptimizer(OperationOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    /**
     * Use graph-based optimization (default).
     */
    public void useGraphBasedOptimizer() {
        this.optimizer = new GraphBasedOptimizer();
    }

    /**
     * Disable optimization (for baseline comparison).
     */
    public void useNoOpOptimizer() {
        this.optimizer = new NoOpOptimizer();
    }

    /**
     * Get the current optimizer (for testing/inspection).
     */
    public OperationOptimizer getOptimizer() {
        return optimizer;
    }

    /**
     * Compare optimization strategies for a list of operations.
     * Useful for benchmarking and thesis metrics.
     * 
     * @param operations List of SMO operations to analyze
     * @return Comparison showing baseline vs optimized results
     */
    public OptimizationComparison compareOptimization(List<SMO> operations) {
        return optimizer.compareWithBaseline(operations);
    }

    public void startJob(Run run, Job job, SchemaEvolutionPayload payload) {
        // FIXME filter correctly by versions.
        final List<Query> prevQueries = queryRepository.findAllInCategory(run.categoryId, payload.prevVersion());
        final List<Query> nextQueries = queryRepository.findAllInCategory(run.categoryId, payload.nextVersion());

        // Some high order magic right here because Java generics suck ass!
        @SuppressWarnings("unchecked")
        final List<Query> filteredPrevQueries = (List<Query>) (Object) ArrayUtils.filterSorted((List<Entity>) (Object) prevQueries, (List<Entity>) (Object) nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        // FIXME doesn't do anything now.
        // for (final var query : filteredPrevQueries) {
        //     final QueryEvolutionResult updateResult = evolver.run(query.content);
        //     final var newVersion = QueryEvolution.createNew(
        //         run.categoryId,
        //         // FIXME get system version
        //         payload.nextVersion(),
        //         query.id(),
        //         updateResult.nextContent,
        //         updateResult.errors
        //     );
        //     repository.create(newVersion);
        // }

        // Evolve instance category if session has one
        evolveInstance(run, payload);
    }

    private void evolveInstance(Run run, SchemaEvolutionPayload payload) {
        if (run.sessionId == null)
            return;

        final InstanceCategoryEntity instanceEntity = instanceRepository.find(run.sessionId);
        if (instanceEntity == null)
            return;

        // Load schema, metadata, and instance
        final SchemaCategoryEntity categoryEntity = schemaRepository.find(run.categoryId);
        final SchemaCategory schema = categoryEntity.toSchemaCategory();
        final MetadataCategory metadata = categoryEntity.toMetadataCategory(schema);
        final InstanceCategory instance = instanceEntity.toInstanceCategory(schema);

        // Get evolution operations between versions
        final List<SchemaEvolutionAlgorithm> updates = repository
            .findAllSchemaEvolutions(run.categoryId).stream()
            .filter(u -> u.version.compareTo(payload.prevVersion()) > 0 && u.version.compareTo(payload.nextVersion()) <= 0)
            .map(u -> u.toSchemaAlgorithm(payload.prevVersion()))
            .toList();

        // Collect all operations from all updates
        final List<SMO> allOperations = new ArrayList<>();
        for (final SchemaEvolutionAlgorithm update : updates) {
            allOperations.addAll(update.operations);
        }

        // Compare optimization: run both NoOp (baseline) and GraphBased optimizer
        // This gives us metrics for thesis: "Without optimizer: X ops, With optimizer: Y ops"
        final OptimizationComparison comparison = optimizer.compareWithBaseline(allOperations);
        LOGGER.info(comparison.summary());

        // Use the optimized result for execution
        final OptimizationResult optimizationResult = comparison.optimized();

        // Apply optimized operations: FIRST evolve schema, THEN evolve instance
        // The InstanceEvolver relies on the updated schema (e.g., getMorphism, getObjex)
        final var instanceEvolver = new InstanceEvolver(instance);
        for (final SMO operation : optimizationResult.operations()) {
            // 1. Evolve schema first - this updates the schema structure
            operation.up(schema, metadata);
            // 2. Then evolve instance - this migrates the data
            operation.accept(instanceEvolver);
        }

        // Log instance evolver metrics
        LOGGER.info("Instance evolution completed. Total cost: {}", instanceEvolver.getTotalCost());

        // Evolve mappings (update access paths to match evolved schema)
        evolveMappings(run.categoryId, optimizationResult.operations(), schema, metadata);

        // Propagate to databases (generate and execute migration commands)
        propagateToDatabase(run.categoryId, optimizationResult.operations(), instance, schema, metadata);

        // Save evolved instance
        final var newInstanceEntity = InstanceCategoryEntity.fromInstanceCategory(
            run.sessionId, run.categoryId, instance);
        instanceRepository.save(newInstanceEntity);
    }

    /**
     * Update mapping access paths to match the evolved schema.
     */
    private void evolveMappings(Id categoryId, List<SMO> operations, SchemaCategory schema, MetadataCategory metadata) {
        // Group mapping entities by datasource
        final List<MappingEntity> allMappingEntities = mappingRepository.findAllInCategory(categoryId);
        if (allMappingEntities.isEmpty())
            return;

        // Collect unique datasources
        final Map<Id, DatasourceEntity> datasources = new TreeMap<>();
        for (final MappingEntity entity : allMappingEntities) {
            if (!datasources.containsKey(entity.datasourceId)) {
                final DatasourceEntity ds = datasourceRepository.find(entity.datasourceId);
                if (ds != null)
                    datasources.put(entity.datasourceId, ds);
            }
        }

        // For each datasource, evolve its mappings
        for (final var dsEntry : datasources.entrySet()) {
            final DatasourceEntity datasource = dsEntry.getValue();
            final var datasourceMappingEntities = allMappingEntities.stream()
                .filter(e -> e.datasourceId.equals(dsEntry.getKey()))
                .toList();

            // Convert to Mapping objects
            final var mappings = datasourceMappingEntities.stream()
                .map(e -> e.toMapping(datasource.toDatasource(), schema))
                .toList();

            // Apply MappingEvolver
            final var mappingEvolver = new MappingEvolver(mappings, schema, metadata);
            for (final SMO operation : operations) {
                operation.accept(mappingEvolver);
            }

            // Update mapping entities with evolved access paths
            final var evolvedMappings = mappingEvolver.getEvolvedMappings();
            for (int i = 0; i < datasourceMappingEntities.size() && i < evolvedMappings.size(); i++) {
                final MappingEntity entity = datasourceMappingEntities.get(i);
                final Mapping evolved = evolvedMappings.get(i);
                entity.accessPath = evolved.accessPath();
                entity.kindName = evolved.kindName();
                mappingRepository.save(entity);
            }

            LOGGER.info("Mappings evolved for datasource: {} ({} mappings)",
                datasource.label, evolvedMappings.size());
        }
    }

    /**
     * Propagate evolution operations to actual databases.
     * Runs both incremental and re-export approaches for comparison.
     */
    private void propagateToDatabase(
        Id categoryId, List<SMO> operations,
        InstanceCategory evolvedInstance, SchemaCategory schema, MetadataCategory metadata
    ) {
        final List<MappingEntity> allMappingEntities = mappingRepository.findAllInCategory(categoryId);
        if (allMappingEntities.isEmpty())
            return;

        // Collect unique datasources
        final Map<Id, DatasourceEntity> datasources = new TreeMap<>();
        for (final MappingEntity entity : allMappingEntities) {
            if (!datasources.containsKey(entity.datasourceId)) {
                final DatasourceEntity ds = datasourceRepository.find(entity.datasourceId);
                if (ds != null)
                    datasources.put(entity.datasourceId, ds);
            }
        }

        final var metrics = new PropagationMetrics();
        metrics.setSmoCount(operations.size());

        for (final var dsEntry : datasources.entrySet()) {
            final DatasourceEntity datasource = dsEntry.getValue();

            try {
                final AbstractControlWrapper control = wrapperService.getControlWrapper(datasource);
                final var dsObj = datasource.toDatasource();
                final var mappings = allMappingEntities.stream()
                    .filter(e -> e.datasourceId.equals(dsEntry.getKey()))
                    .map(e -> e.toMapping(dsObj, schema))
                    .toList();

                // Approach A: Incremental propagation (per-SMO)
                final PropagationResult incrementalResult = runIncrementalPropagation(
                    operations, mappings, control, metadata, datasource);

                // Approach B: Re-export propagation (baseline)
                final PropagationResult reExportResult = new ReExportPropagator().propagate(
                    evolvedInstance, mappings, control,
                    datasource.label, datasource.type.toString());

                // Collect metrics for thesis comparison
                final var comparison = new PropagationComparison(
                    incrementalResult, reExportResult, datasource.label);
                metrics.addComparison(comparison);

                // Execute the incremental approach (the real one)
                if (control.isWritable() && incrementalResult.success()) {
                    control.execute(incrementalResult.statements());
                    LOGGER.info("Executed {} incremental statements for {}",
                        incrementalResult.statementsGenerated(), datasource.label);
                }
            } catch (UnsupportedOperationException e) {
                LOGGER.debug("Evolution wrapper not supported for datasource: {} — skipping propagation",
                    datasource.label);
            } catch (Exception e) {
                LOGGER.error("Propagation failed for datasource {}: {}", datasource.label, e.getMessage());
            }
        }

        // Log thesis-ready summary
        LOGGER.info(metrics.fullSummary());
    }

    private PropagationResult runIncrementalPropagation(
        List<SMO> operations, List<Mapping> mappings,
        AbstractControlWrapper control, MetadataCategory metadata,
        DatasourceEntity datasource
    ) {
        final long startTime = System.currentTimeMillis();
        try {
            final var propagator = new EvolutionPropagator(
                control.getEvolutionWrapper(), mappings, metadata);

            final var allStatements = new ArrayList<AbstractStatement>();
            for (final SMO operation : operations) {
                allStatements.addAll(operation.accept(propagator));
            }

            final long elapsed = System.currentTimeMillis() - startTime;
            return PropagationResult.success(
                datasource.label, datasource.type.toString(), allStatements, elapsed);
        } catch (Exception e) {
            final long elapsed = System.currentTimeMillis() - startTime;
            return PropagationResult.failure(
                datasource.label, datasource.type.toString(), List.of(), elapsed, e.getMessage());
        }
    }

    private QueryEvolver createQueryEvolver(Id categoryId, Version prevVersion, Version nextVersion) {
        final SchemaCategoryEntity categoryEntity = schemaRepository.find(categoryId);
        final List<SchemaEvolutionAlgorithm> updates = repository
            .findAllSchemaEvolutions(categoryId).stream()
            // TODO Check if the version comparison is correct (with respect to the previous algorithm)
            .filter(u -> u.version.compareTo(prevVersion) > 0 && u.version.compareTo(nextVersion) <= 0)
            // .filter(u -> u.prevVersion.compareTo(prevVersion) >= 0 && u.nextVersion.compareTo(nextVersion) <= 0)
            .map(u -> u.toSchemaAlgorithm(prevVersion)).toList();
            // .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevSchema = categoryEntity.toSchemaCategory();
        final MetadataCategory prevMetadata = categoryEntity.toMetadataCategory(prevSchema);
        final SchemaCategory nextSchema = categoryEntity.toSchemaCategory();
        final MetadataCategory nextMetadata = categoryEntity.toMetadataCategory(nextSchema);
        SchemaEvolutionAlgorithm.setToVersion(prevSchema, prevMetadata, updates, categoryEntity.version(), prevVersion);
        SchemaEvolutionAlgorithm.setToVersion(nextSchema, nextMetadata, updates, categoryEntity.version(), nextVersion);

        return new QueryEvolver(prevSchema, nextSchema, updates);
    }

}
