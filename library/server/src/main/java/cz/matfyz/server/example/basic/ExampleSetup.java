package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
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
    @Qualifier("basicDatabaseSetup")
    private DatabaseSetup databaseSetup;

    @Autowired
    @Qualifier("basicMappingSetup")
    private MappingSetup mappingSetup;

    public void setup() {
        final SchemaCategoryWrapper schema = createSchemaCategory();
        final List<DatabaseEntity> databases = databaseSetup.createDatabases();
        final List<LogicalModel> logicalModels = createLogicalModels(databases, schema.id);
        final List<MappingInfo> mappings = mappingSetup.createMappings(logicalModels, schema);

        // TODO jobs
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryInit schemaInit = new SchemaCategoryInit(Schema.schemaLabel);
        final SchemaCategoryInfo schemaInfo = schemaService.createNewInfo(schemaInit);
        final SchemaCategoryWrapper wrapper = schemaService.find(schemaInfo.id);

        final SchemaUpdateInit schemaUpdate = SchemaSetup.createNewUpdate(wrapper, "0:0");
        return schemaService.update(schemaInfo.id, schemaUpdate);
    }

    @Autowired
    private LogicalModelService logicalModelService;

    private List<LogicalModel> createLogicalModels(List<DatabaseEntity> databases, Id schemaId) {
        return databases.stream().map(database -> logicalModelService.createNew(new LogicalModelInit(database.id, schemaId, database.label)).logicalModel()).toList();
    }

}
