package cz.matfyz.server.example.inference;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.repository.ActionRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.service.SchemaCategoryService;

import java.util.List;

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

        final SchemaCategoryWrapper schemaCategory = createEmptySchemaCategory();

        RSDToCategoryPayload inferencePayload = new RSDToCategoryPayload(List.of(datasource.id()));

        Action inferenceAction = createAndSaveAction(inferencePayload, schemaCategory.id(), "inference");

        createAndSaveJob(inferencePayload, schemaCategory.id(), inferenceAction.id(), "inference job");

        return schemaCategory;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createEmptySchemaCategory() {
        return schemaService.create("Inference Example Schema");
    }

    private Action createAndSaveAction(ActionPayload payload, Id categoryId, String label) {
        final Action action = Action.createNew(categoryId, label, payload);
        actionRepository.save(action);

        return action;
    }

    // if more jobs per run, change the logic
    private void createAndSaveJob(ActionPayload payload, Id categoryId, Id actionId, String label) {
        Run run = Run.createUser(categoryId, actionId, null);
        jobRepository.save(run);

        Job job = Job.createNew(run.id(), label, payload, false);
        jobRepository.save(job);
    }

}
