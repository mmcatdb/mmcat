package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.mapping.MappingEntity;
import cz.matfyz.server.mapping.MappingService;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryService;
import cz.matfyz.server.utils.Configuration.SetupProperties;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.ExampleController.Example;
import cz.matfyz.server.example.common.DatasourceBuilder;
import cz.matfyz.server.example.common.MappingEntityBuilder;
import cz.matfyz.server.example.common.QueryBuilder;
import cz.matfyz.tests.example.queryevolution.MongoDB;
import cz.matfyz.tests.example.queryevolution.PostgreSQL;
import cz.matfyz.tests.example.queryevolution.Queries;
import cz.matfyz.tests.example.queryevolution.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionExampleSetup")
public class ExampleSetup {

    public SchemaCategoryEntity setup(int version) {
        SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = createDatasources();

        createQueries(schema.id());

        if (version > 1)
            schema = updateSchemaCategory(schema);

        createMappings(datasources, schema, version);

        // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Example.queryEvolution, Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity, 1);

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    private SchemaCategoryEntity updateSchemaCategory(SchemaCategoryEntity entity) {
        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(entity, 2);
        return schemaService.update(entity.id(), schemaUpdate);
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.queryEvolutionDatabase());

        return datasourceService.createIfNotExists(List.of(
            builder.createPostgreSQL("PostgreSQL"),
            builder.createMongoDB("MongoDB")
        ));
    }

    @Autowired
    private MappingService mappingService;

    private List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity, int version) {
        final var builder = new MappingEntityBuilder(datasources, schemaEntity);

        if (version == 3) {
            return builder
                .add(1, MongoDB::orders)
                .build(mappingService::create);
        }

        builder
            .add(0, PostgreSQL::customer)
            .add(0, PostgreSQL::knows)
            .add(0, PostgreSQL::product);

        if (version == 1) {
            builder.add(0, PostgreSQL::orders);
        }
        else if (version == 2) {
            builder
                .add(0, PostgreSQL::order)
                .add(0, PostgreSQL::ordered)
                .add(0, PostgreSQL::item);
        }
        else if (version == 4) {
            builder.add(1, MongoDB::order);
        }

        return builder.build(mappingService::create);
    }

    @Autowired
    private QueryService queryService;

    private List<Query> createQueries(Id categoryId) {
        return new QueryBuilder(categoryId)
            .add("Find friends", Queries.findFriends)
            .add("Most expensive order", Queries.mostExpensiveOrder)
            .build(queryService::create);
    }

}
