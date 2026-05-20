package cz.matfyz.server.example.basic;

import cz.matfyz.server.mapping.MappingEntity;
import cz.matfyz.server.mapping.MappingService;
import cz.matfyz.server.utils.Configuration.SetupProperties;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.ExampleController.Example;
import cz.matfyz.server.example.common.DatasourceBuilder;
import cz.matfyz.server.example.common.MappingEntityBuilder;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.Neo4j;
import cz.matfyz.tests.example.basic.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicExampleSetup")
public class ExampleSetup {

    public SchemaCategoryEntity setup() {
        final SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = createDatasources();
        createMappings(datasources, schema);

        // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Example.basic, Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity);

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.basicDatabase());

        return datasourceService.createIfNotExists(List.of(
            builder.createPostgreSQL("PostgreSQL"),
            builder.createMongoDB("MongoDB"),
            builder.createNeo4j("Neo4j")
        ));
    }

    @Autowired
    private MappingService mappingService;

    private List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity) {
        return new MappingEntityBuilder(datasources, schemaEntity)
            .add(0, PostgreSQL::order)
            .add(0, PostgreSQL::product)
            .add(0, PostgreSQL::item)
            .add(1, MongoDB::address)
            .add(1, MongoDB::tagSet)
            .add(1, MongoDB::contact)
            .add(1, MongoDB::customer)
            .add(1, MongoDB::note)
            .add(2, Neo4j::item)
            .build(mappingService::create);
    }

}
