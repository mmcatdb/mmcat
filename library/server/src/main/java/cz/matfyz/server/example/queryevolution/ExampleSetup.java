package cz.matfyz.server.example.queryevolution;

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

    public void setup(int version) {
        SchemaCategoryWrapper schema = createSchemaCategory();
        final List<DatasourceWrapper> datasources = datasourceSetup.createDatasources();

        querySetup.createQueries(schema.id);

        if (version > 1)
            schema = updateSchemaCategory(schema);

        final List<LogicalModel> logicalModels = createLogicalModels(datasources, schema.id);
        final List<MappingInfo> mappings = mappingSetup.createMappings(logicalModels, schema, version);

        // // TODO jobs
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryInit schemaInit = new SchemaCategoryInit(Schema.schemaLabel);
        final SchemaCategoryInfo schemaInfo = schemaService.createNewInfo(schemaInit);
        final SchemaCategoryWrapper wrapper = schemaService.find(schemaInfo.id);

        final SchemaUpdateInit schemaUpdate = SchemaSetup.createNewUpdate(wrapper, "0:0", 1);
        return schemaService.update(schemaInfo.id, schemaUpdate);
    }

    private SchemaCategoryWrapper updateSchemaCategory(SchemaCategoryWrapper wrapper) {
        final var updates = schemaService.findAllUpdates(wrapper.id);
        final var operations = updates.get(updates.size() - 1).operations;
        final String lastUpdateVersion = operations.get(operations.size() - 1).version();

        final SchemaUpdateInit schemaUpdate = SchemaSetup.createNewUpdate(wrapper, lastUpdateVersion, 2);
        return schemaService.update(wrapper.id, schemaUpdate);
    }

    @Autowired
    private LogicalModelService logicalModelService;

    private List<LogicalModel> createLogicalModels(List<DatasourceWrapper> datasources, Id schemaId) {
        return datasources.stream().map(datasource -> logicalModelService.createNew(new LogicalModelInit(datasource.id, schemaId, datasource.label)).logicalModel()).toList();
    }

}
