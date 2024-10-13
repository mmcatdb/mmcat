package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.queryevolution.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("queryEvolutionExampleSetup")
public class ExampleSetup {

    @Autowired
    @Qualifier("queryEvolutionDatasourceSetup")
    private DatasourceSetup datasourceSetup;

    @Autowired
    @Qualifier("queryEvolutionMappingSetup")
    private MappingSetup mappingSetup;

    @Autowired
    @Qualifier("queryEvolutionQuerySetup")
    private QuerySetup querySetup;

    public SchemaCategoryWrapper setup(int version) {
        SchemaCategoryWrapper schema = createSchemaCategory();
        final List<DatasourceWrapper> datasources = datasourceSetup.createDatasources();

        querySetup.createQueries(schema.id());

        if (version > 1)
            schema = updateSchemaCategory(schema);

        final List<LogicalModel> logicalModels = createLogicalModels(schema.id(), datasources);
        final List<MappingWrapper> mappings = mappingSetup.createMappings(logicalModels, schema, version);

        // // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryWrapper schemaWrapper = schemaService.create(Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaWrapper, 1);

        return schemaService.update(schemaWrapper.id(), schemaUpdate);
    }

    private SchemaCategoryWrapper updateSchemaCategory(SchemaCategoryWrapper wrapper) {
        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(wrapper, 2);
        return schemaService.update(wrapper.id(), schemaUpdate);
    }

    @Autowired
    private LogicalModelService logicalModelService;

    private List<LogicalModel> createLogicalModels(Id categoryId, List<DatasourceWrapper> datasources) {
        return datasources.stream().map(datasource -> logicalModelService.create(categoryId, datasource.id(), datasource.label).logicalModel()).toList();
    }

}
