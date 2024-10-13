package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.basic.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("basicExampleSetup")
public class ExampleSetup {

    @Autowired
    @Qualifier("basicDatasourceSetup")
    private DatasourceSetup datasourceSetup;

    @Autowired
    @Qualifier("basicMappingSetup")
    private MappingSetup mappingSetup;

    public SchemaCategoryWrapper setup() {
        final SchemaCategoryWrapper schema = createSchemaCategory();
        final List<DatasourceWrapper> datasources = datasourceSetup.createDatasources();
        final List<LogicalModel> logicalModels = createLogicalModels(schema.id(), datasources);
        final List<MappingWrapper> mappings = mappingSetup.createMappings(logicalModels, schema);

        // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryWrapper schemaWrapper = schemaService.create(Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaWrapper);

        return schemaService.update(schemaWrapper.id(), schemaUpdate);
    }

    @Autowired
    private LogicalModelService logicalModelService;

    private List<LogicalModel> createLogicalModels(Id categoryId, List<DatasourceWrapper> datasources) {
        return datasources.stream().map(datasource -> logicalModelService.create(categoryId, datasource.id(), datasource.label).logicalModel()).toList();
    }

}
