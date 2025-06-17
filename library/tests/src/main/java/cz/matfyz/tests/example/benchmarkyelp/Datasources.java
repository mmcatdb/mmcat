package cz.matfyz.tests.example.benchmarkyelp;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_yelp");

    private TestDatasource<MongoDBControlWrapper> mongoDB;
    private TestDatasource<PostgreSQLControlWrapper> postgreSQL;

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null) {
            mongoDB = createNewMongoDB()
                .addMapping(MongoDB.business(schema))
                .addMapping(MongoDB.user(schema))
                .addMapping(MongoDB.review(schema));

            Schema.collectStatsToCache(mongoDB); // FIXME: this is a temporary implementation
        }

        return mongoDB;
    }

    public TestDatasource<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return datasourceProvider.createPostgreSQL(PostgreSQL.datasource.identifier, schema, null);
    }

        public TestDatasource<PostgreSQLControlWrapper> postgreSQL() {
        if (postgreSQL == null) {
            postgreSQL = createNewPostgreSQL()
                .addMapping(PostgreSQL.business(schema))
                .addMapping(PostgreSQL.user(schema))
                .addMapping(PostgreSQL.isFriend(schema))
                .addMapping(PostgreSQL.review(schema));
        }

        return postgreSQL;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(MongoDB.datasource.identifier, schema, null);
    }

}
