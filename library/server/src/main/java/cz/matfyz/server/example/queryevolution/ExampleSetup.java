package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.database.Database;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.tests.schema.BasicSchema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleSetup {

    // @Autowired
    // DatabaseSetup databaseSetup;

    // @Autowired
    // MappingSetup mappingSetup;

    public void setup() {
        // final SchemaCategoryWrapper schema = createSchemaCategory();
        // final List<Database> databases = databaseSetup.createDatabases();
        // final List<LogicalModel> logicalModels = createLogicalModels(databases, schema);
        // final List<MappingInfo> mappings = mappingSetup.createMappings(logicalModels, schema);

        // // TODO jobs
    }

    // @Autowired
    // SchemaCategoryService schemaService;

    // private SchemaCategoryWrapper createSchemaCategory() {
    //     final SchemaCategoryInit schemaInit = new SchemaCategoryInit(BasicSchema.schemaLabel);
    //     final SchemaCategoryInfo schemaInfo = schemaService.createNewInfo(schemaInit);

    //     final SchemaUpdateInit schemaUpdate = SchemaSetup.createNewUpdate();
    //     return schemaService.update(schemaInfo.id, schemaUpdate);
    // }

    // @Autowired
    // LogicalModelService logicalModelService;

    // private List<LogicalModel> createLogicalModels(List<Database> databases, SchemaCategoryWrapper schema) {
    //     return databases.stream().map(database -> logicalModelService.createNew(new LogicalModelInit(database.id, schema.id, database.label))).toList();
    // }

}
