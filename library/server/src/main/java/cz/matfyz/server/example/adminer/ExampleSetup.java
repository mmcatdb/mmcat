package cz.matfyz.server.example.adminer;

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
import cz.matfyz.tests.example.adminer.MongoDB;
import cz.matfyz.tests.example.adminer.Neo4j;
import cz.matfyz.tests.example.adminer.PostgreSQL;
import cz.matfyz.tests.example.adminer.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("adminerExampleSetup")
public class ExampleSetup {

    public SchemaCategoryEntity setup() {
        final SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = createDatasources();
        createMappings(datasources, schema);

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Example.adminer, Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity);

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.adminerDatabase());

        return datasourceService.createIfNotExists(List.of(
            builder.createPostgreSQL("PostgreSQL - Adminer"),
            builder.createMongoDB("MongoDB - Adminer"),
            builder.createNeo4j("Neo4j - Adminer")
        ));
    }

    @Autowired
    private MappingService mappingService;

    private List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity) {
        return new MappingEntityBuilder(datasources, schemaEntity)
            .add(0, PostgreSQL::user)
            .add(0, PostgreSQL::comment)
            .add(0, PostgreSQL::businessHours)
            .add(1, MongoDB::business)
            .add(2, Neo4j::user)
            .add(2, Neo4j::friend)
            .build(mappingService::create);
    }

}
