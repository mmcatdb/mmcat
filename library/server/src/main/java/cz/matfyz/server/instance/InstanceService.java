package cz.matfyz.server.instance;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.WrapperService;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.file.File;
import cz.matfyz.server.file.FileService;
import cz.matfyz.server.job.Job;
import cz.matfyz.server.job.Run;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.utils.Configuration.ServerProperties;
import cz.matfyz.transformations.DatabaseToInstance;
import cz.matfyz.transformations.InstanceToDatabase;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceService.class);

    @Autowired
    private InstanceCategoryRepository repository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SchemaCategoryRepository schemaRepository;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private ServerProperties server;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private FileService fileService;

    public void startModelToCategoryJob(Run run, Job job, ModelToCategoryPayload payload) {
        if (run.sessionId == null)
            throw SessionException.runNotInSession(run.id());

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryEntity instanceEntity = repository.find(run.sessionId);

        InstanceCategory instance = instanceEntity != null
            ? instanceEntity.toInstanceCategory(schema)
            : new InstanceBuilder(schema).build();

        final DatasourceEntity datasourceEntity = datasourceRepository.find(payload.datasourceId());
        final Datasource datasource = datasourceEntity.toDatasource();
        final List<Mapping> mappings = mappingRepository.findAllInCategory(run.categoryId, payload.datasourceId()).stream()
            .filter(entity -> payload.mappingIds().isEmpty() || payload.mappingIds().contains(entity.id()))
            .map(entity -> entity.toMapping(datasource, schema))
            .toList();

        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasourceEntity).getPullWrapper();

        for (final Mapping mapping : mappings)
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();

        if (instance != null) {
            final var newEntity = InstanceCategoryEntity.fromInstanceCategory(run.sessionId, run.categoryId, instance);
            repository.save(newEntity);
        }
    }

    public void startCategoryToModelJob(Run run, Job job, CategoryToModelPayload payload) {
        if (run.sessionId == null)
            throw SessionException.runNotInSession(run.id());

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryEntity instanceEntity = repository.find(run.sessionId);

        final InstanceCategory instance = instanceEntity != null
            ? instanceEntity.toInstanceCategory(schema)
            : new InstanceBuilder(schema).build();

        final DatasourceEntity datasourceEntity = datasourceRepository.find(payload.datasourceId());
        final Datasource datasource = datasourceEntity.toDatasource();
        final List<Mapping> mappings = mappingRepository.findAllInCategory(run.categoryId, payload.datasourceId()).stream()
            .filter(entity -> payload.mappingIds().isEmpty() || payload.mappingIds().contains(entity.id()))
            .map(entity -> entity.toMapping(datasource, schema))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceEntity);

        final var result = new InstanceToDatabase()
            .input(
                mappings,
                instance,
                control.getDDLWrapper(),
                control.getDMLWrapper(),
                control.getICWrapper()
            )
            .run();

        // TODO - find a better way how to execute the changes (currently its too likely to fail)

        // TODO - verzovat databáze - tj. vytvořit vždy novou databázi (v rámci stejného engine)
        //  - např. uživatel zvolí "my_db", tak vytvářet "my_db_1", "my_db_2" a podobně
        //  - resp. při opětovném spuštění to smazat a vytvořit znovu ...

        boolean isExecuted = false;

        if (server.executeModels() && control.isWritable()) {
            LOGGER.info("Start executing models ...");
            control.execute(result.statements());
            LOGGER.info("... models executed.");
            isExecuted = true;
        }

        final var resultString = result.statementsAsString();

        final File file = fileService.create(job.id(), datasourceEntity.id(), isExecuted, datasource.type, resultString);

        // Instead of the result we are saving only the id of the file, where the result is saved
        job.data = new TransformationJobData(file.id());
    }

}
