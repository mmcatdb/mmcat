package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryUpdateResult;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.Configuration.ServerProperties;
import cz.matfyz.server.Configuration.SparkProperties;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutorService.class);

    @Autowired
    private JobRepository repository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private SchemaCategoryService schemaService;

    @Autowired
    private InstanceCategoryService instanceService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private ServerProperties server;

    @Autowired
    private SparkProperties spark;

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private DatasourceService datasourceService;

    // The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
    // There is a space for an optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require a synchronization on the instance level in the transformation algorithms.

    // This method will be executed 2 seconds after the previous execution finished.
    @Scheduled(fixedDelay = 2000)
    public void executeAllJobs() {
        final var readyIds = repository.findAllReadyIds();
        for (final var jobId : readyIds)
            executeJob(jobId);
    }

    private void executeJob(Id jobId) {
        final var jobWithRun = repository.find(jobId);
        final var run = jobWithRun.run();
        final var job = jobWithRun.job();

        if (job.state != Job.State.Ready) {
            LOGGER.info("Job { id: {}, name: '{}' } is not ready.", job.id, job.label);
            return;
        }

        job.state = Job.State.Running;
        repository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.label);

        try {
            processJobByType(run, job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
            job.state = Job.State.Finished;
            repository.save(job);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);

            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id, job.label), finalException);
            job.state = Job.State.Failed;
            job.data = finalException.toSerializedException();
            repository.save(job);
        }
    }

    private void processJobByType(Run run, Job job) {
        if (job.payload instanceof CategoryToModelPayload categoryToModelPayload)
            categoryToModelAlgorithm(run, job, categoryToModelPayload);
        else if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload)
            modelToCategoryAlgorithm(run, job, modelToCategoryPayload);
        else if (job.payload instanceof UpdateSchemaPayload updateSchemaPayload)
            updateSchemaAlgorithm(run, updateSchemaPayload);
        else if (job.payload instanceof RSDToCategoryPayload rsdToCategoryPayload)
            rsdToCategoryAlgorithm(run, rsdToCategoryPayload);

        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);
    }


    private void modelToCategoryAlgorithm(Run run, Job job, ModelToCategoryPayload payload) {
        if (run.sessionId == null)
            throw SessionException.notFound(run.id);

        final DatasourceWrapper datasource = logicalModelService.find(payload.logicalModelId()).datasource();
        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        final List<MappingWrapper> mappingWrappers = mappingService.findAll(payload.logicalModelId());

        final SchemaCategory schema = schemaService.find(run.categoryId).toSchemaCategory();
        @Nullable InstanceCategory instance = instanceService.loadCategory(run.sessionId, schema);
        //System.out.println("jobexecutor: " + instance.objects());
        //System.out.println("print if non empty");
        System.out.println("instance before");
        System.out.println(instance);

        if (mappingWrappers.isEmpty())
            System.out.println("mapping wrappers is empty");

        for (final MappingWrapper mappingWrapper : mappingWrappers) {
            final Mapping mapping = mappingWrapper.toMapping(schema);
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();
        }

        System.out.println("instance after");
        System.out.println(instance);

        if (instance != null)
            instanceService.saveCategory(run.sessionId, run.categoryId, instance);
    }

    private void categoryToModelAlgorithm(Run run, Job job, CategoryToModelPayload payload) {
        if (run.sessionId == null)
            throw SessionException.notFound(job.id);

        final SchemaCategory schema = schemaService.find(run.categoryId).toSchemaCategory();
        @Nullable InstanceCategory instance = instanceService.loadCategory(run.sessionId, schema);

        final DatasourceWrapper datasource = logicalModelService.find(payload.logicalModelId()).datasource();
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> wrapper.toMapping(schema))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasource);

        final var output = new StringBuilder();
        for (final Mapping mapping : mappings) {
            final var result = new InstanceToDatabase()
                .input(
                    mapping,
                    mappings,
                    instance,
                    control.getDDLWrapper(),
                    control.getDMLWrapper(),
                    control.getICWrapper()
                )
                .run();

            output.append(result.statementsAsString() + "\n");

            // TODO - find a better way how to execute the changes (currently its too likely to fail)

            // TODO - verzovat databáze - tj. vytvořit vždy novou databázi (v rámci stejného engine)
            //  - např. uživatel zvolí "my_db", tak vytvářet "my_db_1", "my_db_2" a podobně
            //  - resp. při opětovném spuštění to smazat a vytvořit znovu ...

            if (server.executeModels() && control.isWritable()) {
                LOGGER.info("Start executing models ...");
                control.execute(result.statements());
                LOGGER.info("... models executed.");
            }
            /*else { LOGGER.info("Models didn't get executed. Yikes");}*/
            /* for now I choose not to execute the statements, but just see if they even get created
            LOGGER.info("Start executing models ...");
            control.execute(result.statements());
            LOGGER.info("... models executed."); */
        }

        job.data = output.toString();
    }

    private void updateSchemaAlgorithm(Run run, UpdateSchemaPayload payload) {
        final List<QueryWithVersion> prevQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.prevVersion());
        final List<QueryWithVersion> nextQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.nextVersion());
        final List<QueryWithVersion> filteredPrevQueries = ArrayUtils.filterSorted(prevQueries, nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        for (final var query : filteredPrevQueries) {
            final QueryUpdateResult updateResult = evolver.run(query.version().content);
            final var newVersion = QueryVersion.createNew(
                query.query().id,
                payload.nextVersion(),
                updateResult.nextContent,
                updateResult.errors
            );
            queryRepository.save(newVersion);
        }
    }

    private QueryEvolver createQueryEvolver(Id categoryId, Version prevVersion, Version nextVersion) {
        final SchemaCategoryWrapper wrapper = schemaService.find(categoryId);
        final List<SchemaCategoryUpdate> updates = schemaService
            .findAllUpdates(categoryId).stream()
            .filter(u -> u.prevVersion.compareTo(prevVersion) >= 0 && u.nextVersion.compareTo(nextVersion) <= 0)
            .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevCategory = wrapper.toSchemaCategory();
        final SchemaCategory nextCategory = wrapper.toSchemaCategory();
        SchemaCategoryUpdate.setToVersion(prevCategory, updates, wrapper.version, prevVersion);
        SchemaCategoryUpdate.setToVersion(nextCategory, updates, wrapper.version, nextVersion);

        return new QueryEvolver(prevCategory, nextCategory, updates);
    }

    private void rsdToCategoryAlgorithm(Run run, RSDToCategoryPayload payload) {
        // extracting the empty SK wrapper
        final SchemaCategoryWrapper originalSchemaWrapper = schemaService.find(run.categoryId);
        final DatasourceWrapper datasourceWrapper = datasourceService.find(payload.datasourceId());

        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());
        final AbstractInferenceWrapper inferenceWrapper = wrapperService.getControlWrapper(datasourceWrapper).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, payload.kindName(), originalSchemaWrapper.label)
            .run();

        //System.out.println(categoryMappingPair.schemaCat().allObjects());
        //System.out.println(categoryMappingPair.schemaCat().allMorphisms());
        final SchemaCategoryWrapper schemaWrapper = createWrapperFromCategory(categoryMappingPair.schemaCat());

        // what about this label?
        LogicalModelInit logicalModelInit = new LogicalModelInit(datasourceWrapper.id, run.categoryId, "Initial logical model");
        LogicalModelWithDatasource logicalModelWithDatasource = logicalModelService.createNew(logicalModelInit);

        schemaService.overwriteInfo(schemaWrapper, run.categoryId);
        mappingService.createNew(categoryMappingPair.mapping(), logicalModelWithDatasource.logicalModel().id);
    }

    /**
     * Layout algo using JUNG library
     */
    private Map<Key, Position> layoutObjects(Collection<SchemaObject> objects, Collection<SchemaMorphism> morphisms) {
        DirectedSparseGraph<SchemaObject, SchemaMorphism> graph = new DirectedSparseGraph<>();
        for (SchemaObject o : objects) {
            graph.addVertex(o);
        }

        for (SchemaMorphism m : morphisms) {
            if (m.dom() != null && m.cod() != null) 
                graph.addEdge(m, m.dom(), m.cod());
        }

        // determine the layout size based on the num of nodes
        int numNodes = objects.size();
        int layoutSize = Math.max(600, (int) (Math.log(numNodes + 1.0) * 200));


        FRLayout<SchemaObject, SchemaMorphism> layout = new FRLayout<>(graph);
        //layout.setSize(new Dimension(600, 600));
        layout.setSize(new Dimension(layoutSize, layoutSize));

        // Adjust attraction and repulsion multipliers based on the graph size
        // Only applies for FRLayout
        // play with these parameters to tweek how the graph will look like        
        if (numNodes > 50) {
            layout.setAttractionMultiplier(0.75);
            layout.setRepulsionMultiplier(2.0);
        } else if (numNodes > 20) {
            layout.setAttractionMultiplier(0.85);
            layout.setRepulsionMultiplier(1.5);
        } else {
            layout.setAttractionMultiplier(1.0);
            layout.setRepulsionMultiplier(1.0);
        }
        

        for (int i = 0; i < 1000; i++) { // initialize positions
            layout.step();
        }

        Map<Key, Position> positions = new HashMap<>();
        for (SchemaObject node : graph.getVertices()) {
            double x = layout.getX(node);
            double y = layout.getY(node);
            positions.put(node.key(), new Position(x, y));
        }
        ensureNoOverlap(positions, layoutSize);

        return positions;
    }

    private void ensureNoOverlap(Map<Key, Position> positions, int layoutSize) {
        double nodeRadius = 10.0; // Assume a radius for each node
        double margin = 50.0; // Margin to keep nodes away from the edges
        int maxIterations = 100; // Maximum number of iterations
        int iteration = 0;
    
        boolean overlapExists;
        do {
            overlapExists = false;
            Map<Key, Position> newPositions = new HashMap<>(positions);
    
            for (Key key1 : positions.keySet()) {
                Position pos1 = positions.get(key1);
                for (Key key2 : positions.keySet()) {
                    if (key1.equals(key2)) continue;
                    Position pos2 = positions.get(key2);
    
                    double dx = pos1.x() - pos2.x();
                    double dy = pos1.y() - pos2.y();
                    double distance = Math.sqrt(dx * dx + dy * dy);
    
                    if (distance < 2 * nodeRadius) { // Nodes are overlapping
                        overlapExists = true;
                        double overlap = 2 * nodeRadius - distance;
                        double angle = Math.atan2(dy, dx);
    
                        // Move nodes away from each other
                        double newX1 = pos1.x() + Math.cos(angle) * overlap / 2;
                        double newY1 = pos1.y() + Math.sin(angle) * overlap / 2;
                        double newX2 = pos2.x() - Math.cos(angle) * overlap / 2;
                        double newY2 = pos2.y() - Math.sin(angle) * overlap / 2;
    
                        // Ensure nodes are within layout bounds
                        newX1 = Math.max(margin, Math.min(newX1, layoutSize - margin));
                        newY1 = Math.max(margin, Math.min(newY1, layoutSize - margin));
                        newX2 = Math.max(margin, Math.min(newX2, layoutSize - margin));
                        newY2 = Math.max(margin, Math.min(newY2, layoutSize - margin));
    
                        newPositions.put(key1, new Position(newX1, newY1));
                        newPositions.put(key2, new Position(newX2, newY2));
                    }
                }
            }
            positions.putAll(newPositions);
            iteration++;
        } while (overlapExists && iteration < maxIterations);
    }
    


    private SchemaCategoryWrapper createWrapperFromCategory(SchemaCategory category) {
        MetadataContext context = new MetadataContext();

        context.setId(new Id(null)); // is null ok?
        context.setVersion(Version.generateInitial());

        Map<Key, Position> positions = layoutObjects(category.allObjects(), category.allMorphisms());
        positions.forEach(context::setPosition);

        return SchemaCategoryWrapper.fromSchemaCategory(category, context);
    }

}
