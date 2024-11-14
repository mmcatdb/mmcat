package cz.matfyz.server.example.inference;

import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.service.ActionService;
import cz.matfyz.server.service.JobService;
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
    private ActionService actionService;

    @Autowired
    private JobService jobService;

    public SchemaCategoryWrapper setup() {
        final DatasourceWrapper datasource = datasourceSetup.createDatasource();

        final SchemaCategoryWrapper schemaCategory = createEmptySchemaCategory();

        final RSDToCategoryPayload inferencePayload = new RSDToCategoryPayload(List.of(datasource.id()));

        final Action inferenceAction = actionService.create(schemaCategory.id(), "inference", List.of(inferencePayload));

        jobService.createRun(inferenceAction);

        return schemaCategory;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createEmptySchemaCategory() {
        return schemaService.create("Inference Example Schema");
    }

}
