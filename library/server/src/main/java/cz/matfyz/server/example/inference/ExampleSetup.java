package cz.matfyz.server.example.inference;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.ExampleController.Example;
import cz.matfyz.server.example.common.DatasourceBuilder;
import cz.matfyz.server.inference.InferencePayload;
import cz.matfyz.server.job.Action;
import cz.matfyz.server.job.ActionService;
import cz.matfyz.server.job.JobService;
import cz.matfyz.server.utils.Configuration.SetupProperties;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("inferenceExampleSetup")
public class ExampleSetup {

    @Autowired
    private ActionService actionService;

    @Autowired
    private JobService jobService;

    public SchemaCategoryEntity setup() {
        final List<DatasourceEntity> datasources = createDatasources();
        final SchemaCategoryEntity schemaCategory = createEmptySchemaCategory();

        final InferencePayload inferencePayload = new InferencePayload(datasources.stream().map(DatasourceEntity::id).toList());
        final Action inferenceAction = actionService.create(schemaCategory.id(), "inference", List.of(inferencePayload));
        jobService.createRun(inferenceAction);

        return schemaCategory;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createEmptySchemaCategory() {
        return schemaService.create(Example.inference, "Inference Example Schema");
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.inferenceDatabase());

        return datasourceService.createIfNotExists(List.of(
            builder.createMongoDB("MongoDB - Inference")
        ));
    }

}
