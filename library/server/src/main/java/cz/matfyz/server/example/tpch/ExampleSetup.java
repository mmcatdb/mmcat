package cz.matfyz.server.example.tpch;

import cz.matfyz.server.mapping.MappingEntity;
import cz.matfyz.server.mapping.MappingService;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryService;
import cz.matfyz.server.querying.QueryStats;
import cz.matfyz.server.querying.QueryStats.AggregatedDouble;
import cz.matfyz.server.querying.QueryStats.AggregatedLong;
import cz.matfyz.server.utils.Configuration.SetupProperties;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.tests.example.tpch.Neo4j;
import cz.matfyz.tests.example.tpch.PostgreSQL;
import cz.matfyz.tests.example.tpch.Schema;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.ExampleController.Example;
import cz.matfyz.server.example.common.DatasourceBuilder;
import cz.matfyz.server.example.common.MappingEntityBuilder;
import cz.matfyz.server.example.common.QueryBuilder;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("tpchExampleSetup")
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
        final SchemaCategoryEntity schemaEntity = schemaService.create(Example.tpch, Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity);

        createQueries(schemaEntity.id());

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.tpchDatabase());

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
            .add(0, PostgreSQL::orders)
            .add(0, PostgreSQL::lineItem)
            .add(2, Neo4j::supplier)
            .add(2, Neo4j::part)
            .add(2, Neo4j::partSupp)
            .add(2, Neo4j::suppliedBy)
            .add(2, Neo4j::isForPart)
            .build(mappingService::create);
    }

    @Autowired
    private QueryService queryService;

    private List<Query> createQueries(Id categoryId) {
        int seed = -1;
        return new QueryBuilder(categoryId)
            .add("Orders with low total price", mockQuery, mockQueryStats(seed++, 5, 1.2))
            .add("Supplier count per part", mockQuery, mockQueryStats(seed++, 25, 1.7))
            .add("Cheapest supplier for part", mockQuery, mockQueryStats(seed++, 10, 3.2))
            .build(queryService::create);
    }

    // FIXME replace with real queries
    /** @deprecated */
    final private String mockQuery = """
        SELECT {
            ?orders key ?orderkey .
        }
        WHERE {
            ?orders 70 ?orderkey .
        }
        """;

    /** @deprecated */
    public static QueryStats mockQueryStats(int seed, int executionCount, double factor) {
        final var random = new Random(seed);

        // Make sure that min < max.
        final double resultSizeMin = random.nextDouble(200 * factor) + 100 * factor;
        final double resultSizeMax = random.nextDouble(4000 * factor) + 2000 * factor;

        final double planningTimeMin = random.nextDouble(2) + 1;
        final double planningTimeMax = random.nextDouble(3) + 4;

        final double executionTimeMin = random.nextDouble(4 * factor) + 2 * factor;
        final double executionTimeMax = random.nextDouble(6 * factor) + 8 * factor;

        return new QueryStats(
            executionCount,
            mockLong(random, executionCount, (int) resultSizeMin, (int) resultSizeMax),
            mockDouble(random, executionCount, planningTimeMin, planningTimeMax),
            mockDouble(random, executionCount, executionTimeMin, executionTimeMax)
        );
    }

    /** @deprecated */
    private static AggregatedLong mockLong(Random random, int executionCount, long min, long max) {
        final double sum = (min + max) / 2 * random.nextDouble(0.8, 1.2) * executionCount;
        return new AggregatedLong(min, max, (long) sum);
    }

    /** @deprecated */
    private static AggregatedDouble mockDouble(Random random, int executionCount, double min, double max) {
        final double sum = (min + max) / 2 * random.nextDouble(0.8, 1.2) * executionCount;
        return new AggregatedDouble(min, max, sum);
    }

}
