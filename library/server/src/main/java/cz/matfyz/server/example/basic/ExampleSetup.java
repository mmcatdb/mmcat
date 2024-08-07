package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.SchemaCategoryService;
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
        final List<LogicalModel> logicalModels = createLogicalModels(datasources, schema.id());
        final List<MappingInfo> mappings = mappingSetup.createMappings(logicalModels, schema);

        // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryInit schemaInit = new SchemaCategoryInit(Schema.schemaLabel);
        final SchemaCategoryWrapper schemaWrapper = schemaService.create(schemaInit);

        final SchemaUpdateInit schemaUpdate = SchemaSetup.createNewUpdate(schemaWrapper, "0");

        return schemaService.update(schemaWrapper.id(), schemaUpdate);
    }

    @Autowired
    private LogicalModelService logicalModelService;

    private List<LogicalModel> createLogicalModels(List<DatasourceWrapper> datasources, Id schemaId) {
        return datasources.stream().map(datasource -> logicalModelService.createNew(new LogicalModelInit(datasource.id(), schemaId, datasource.label)).logicalModel()).toList();
    }

}
