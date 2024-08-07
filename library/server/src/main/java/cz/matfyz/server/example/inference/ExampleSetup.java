package cz.matfyz.server.example.inference;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.ActionRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.service.SchemaCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("inferenceExampleSetup")
public class ExampleSetup {

    @Autowired
    @Qualifier("inferenceDatasourceSetup")
    private DatasourceSetup datasourceSetup;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private JobRepository jobRepository;

    public SchemaCategoryWrapper setup() {
        final DatasourceWrapper datasource = datasourceSetup.createDatasource();

        // create empty SK
        final SchemaCategoryWrapper schemaCategory = createEmptySchemaCategory();

        RSDToCategoryPayload inferencePayload = new RSDToCategoryPayload(datasource.id, "business");

        Action inferenceAction = createAndSaveAction(inferencePayload, schemaCategory.id, "inference");

        // create (and run) the inference job
        createAndSaveJob(inferencePayload, schemaCategory.id, inferenceAction.id, "inference job");

        // TODO: for the other parts of data generation (mct and ctm) create actions, and let the user run them themselves
        /*
        List<DatasourceWrapper> datasources = datasourceSetup.createDatasourceForMapping();

        ModelToCategoryPayload mtcPayload = new ModelToCategoryPayload(null); // insert logical model Id
        Action mtcAction = createAndSaveAction(mtcPayload, schemaCategory.id, "create instance category");

        CategoryToModelPayload ctmPayload = new CategoryToModelPayload(null); // insert logical model Id
        Action ctmAction = createAndSaveAction(ctmPayload, schemaCategory.id, "generate mm data");
        */
        // this has been added after, because the compiler was complaining in IndexController, that all cases need to return SchemaCategoryWrapper
        return schemaCategory;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createEmptySchemaCategory() {
        final SchemaCategoryInit schemaInit = new SchemaCategoryInit("Inference Example Schema");
        final SchemaCategoryInfo schemaInfo = schemaService.createNewInfo(schemaInit);
        return schemaService.find(schemaInfo.id);
    }

    private Action createAndSaveAction(ActionPayload payload, Id schemaId, String label) {
        Action action = Action.createNew(schemaId, label, payload);
        actionRepository.save(action);
        return action;
    }

    // if more jobs per run, change the logic
    private void createAndSaveJob(ActionPayload payload, Id schemaId, Id actionId, String label) {
        Run run = Run.createUser(schemaId, actionId, null);
        jobRepository.save(run);

        Job job = Job.createNew(run.id, label, payload, false);
        jobRepository.save(job);
    }

}
