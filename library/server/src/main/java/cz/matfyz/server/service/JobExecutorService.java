package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.core.utils.io.UrlInputStreamProvider;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryUpdateResult;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.CategoryMappingPair;
import cz.matfyz.integration.processes.JsonLdToCategory;
import cz.matfyz.server.builder.MappingBuilder;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.configuration.ServerProperties;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.JsonLdToCategoryPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
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
    private DatabaseService databaseService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SchemaCategoryService schemaService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private ServerProperties server;

    @Autowired
    private QueryRepository queryRepository;

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

        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.label);
        
        try {
            processJobByType(run, job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
            job.state = Job.State.Finished;
            job.data = null;
            repository.save(job);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);
            
            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id, job.label), finalException);
            job.state = Job.State.Failed;
            job.data = finalException.toSerializedException();
            repository.save(job);
        }

        // tryStartNextJob(true);
    }

    private void processJobByType(Run run, Job job) {
        if (job.payload instanceof CategoryToModelPayload categoryToModelPayload)
            categoryToModelAlgorithm(run, categoryToModelPayload);
        else if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload)
            modelToCategoryAlgorithm(run, modelToCategoryPayload);
        else if (job.payload instanceof JsonLdToCategoryPayload jsonLdToCategoryPayload)
            jsonLdToCategoryAlgorithm(run, jsonLdToCategoryPayload);
        else if (job.payload instanceof UpdateSchemaPayload updateSchemaPayload)
            updateSchemaAlgorithm(run, updateSchemaPayload);
        else if (job.payload instanceof RSDToCategoryPayload rsdToCategoryPayload)
        	RSDToCategoryAlgorithm(run, rsdToCategoryPayload);

        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);
    }
    /**
     * Layout algo using grid layout
     * @param objects
     * @return
     */
    private Map<Key, Position> layoutObjects(Collection<SchemaObject> objects) {
    	int gridSize = (int) Math.ceil(Math.sqrt(objects.size()));
    	double distance = 100; //distance between nodes on the grid
    	
    	Map<Key, Position> positions = new HashMap<>();
    	int i = 0;
    	for (SchemaObject object: objects) {
    		int row = i / gridSize;
    		int col = i % gridSize;
    		double x = col * distance;
    		double y = row * distance;
    		positions.put(object.key(), new Position(x, y));
    		i++;
    	}
    	return positions;
    }
    
    private SchemaCategoryWrapper createWrapperFromCategory(SchemaCategory category) {
        //Akorát k tomu potřebujete vytvořit souřadnice objektů v grafu, nicméně na to by mělo jít najít nějaký layoutovací algoritmus 
        MetadataContext context = new MetadataContext();
    	
    	//Create a special id, under which you later load this category ?? 
    	Id id = new Id("schm_from_rsd"); //now hard coded special id 
    	context.setId(id);
    	
    	Version version = Version.generateInitial(); // can I use this?
    	context.setVersion(version);
    	
    	//maybe get rid of this second loop? and do all in one
    	Map<Key, Position> positions = layoutObjects(category.allObjects());
    	
    	for (Map.Entry<Key, Position> entry: positions.entrySet()) {
    		Key key = entry.getKey();
    		Position position = entry.getValue();
    		context.setPosition(key, position);
    	}  	
	    	
    	SchemaCategoryWrapper wrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context); 
    	
    	return wrapper;
    }
    
    private void RSDToCategoryAlgorithm(Run run, RSDToCategoryPayload payload) {
    	final DataSource dataSource = dataSourceService.find(payload.dataSourceId());
    	String url = dataSource.url;
    	
    	//Assuming the url is in the MongoDB format
		String uri = url.substring(0, url.lastIndexOf("/"));
		String dbNameAndCollection = url.substring(url.lastIndexOf("/") + 1);
		String[] parts = dbNameAndCollection.split("\\.");
		 
		String databaseName = parts[0];
		String collectionName = parts[1]; 

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll().input("appName", uri, databaseName, collectionName).run();
        
        SchemaCategoryWrapper wrapper = createWrapperFromCategory(categoryMappingPair.schemaCat());
        schemaService.createNewInfo(wrapper);
        
        mappingService.createNew(categoryMappingPair.mapping());
    }

    private void modelToCategoryAlgorithm(Run run, ModelToCategoryPayload payload) {
        final DatabaseEntity database = logicalModelService.find(payload.logicalModelId()).database();
        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();
        final List<MappingWrapper> mappingWrappers = mappingService.findAll(payload.logicalModelId());

        // InstanceCategory instance = store.getCategory(run.categoryId);
        InstanceCategory instance = null;

        for (final MappingWrapper mappingWrapper : mappingWrappers) {
            final Mapping mapping = createMapping(mappingWrapper, run.categoryId);
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();
        }

        // store.setInstance(run.categoryId, instance);
    }

    private void categoryToModelAlgorithm(Run run, CategoryToModelPayload payload) {
        // final InstanceCategory instance = store.getCategory(run.categoryId);
        InstanceCategory instance = null;

        final DatabaseEntity database = logicalModelService.find(payload.logicalModelId()).database();
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> createMapping(wrapper, run.categoryId))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(database);
        
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

            if (server.executeModels()) {
                LOGGER.info("Start executing models ...");
                control.execute(result.statements());
                LOGGER.info("... models executed.");
            }
        }
        
        // modelService.createNew(store, job, run, job.label, output.toString());
    }
    
    private void jsonLdToCategoryAlgorithm(Run run, JsonLdToCategoryPayload payload) {
        // final InstanceCategory instance = store.getCategory(run.categoryId);
        final InstanceCategory instance = null;

        final DataSource dataSource = dataSourceService.find(payload.dataSourceId());
        final var inputStreamProvider = new UrlInputStreamProvider(dataSource.url);

        final SchemaCategoryWrapper schemaWrapper = schemaService.find(run.categoryId);
        final SchemaCategory schema = schemaWrapper.toSchemaCategory();

        final var newInstance = new JsonLdToCategory().input(schema, instance, inputStreamProvider).run();
        // store.setInstance(run.categoryId, newInstance);
    }

    private Mapping createMapping(MappingWrapper mappingWrapper, Id categoryId) {
        final var categoryWrapper = schemaService.find(categoryId);

        return new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
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
            .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevCategory = wrapper.toSchemaCategory();
        final SchemaCategory nextCategory = wrapper.toSchemaCategory();
        SchemaCategoryUpdate.setToVersion(prevCategory, updates, wrapper.version, prevVersion);
        SchemaCategoryUpdate.setToVersion(nextCategory, updates, wrapper.version, nextVersion);

        return new QueryEvolver(prevCategory, nextCategory);
    }

}
