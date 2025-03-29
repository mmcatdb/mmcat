package cz.matfyz.tests.example.benchmarkyelp;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_yelp");

    private TestDatasource<MongoDBControlWrapper> mongoDB;

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null)
            mongoDB = createNewMongoDB()
                .addMapping(MongoDB.business(schema))
                .addMapping(MongoDB.user(schema))
                .addMapping(MongoDB.review(schema));

        return mongoDB;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(MongoDB.datasource.identifier, schema, null);
    }

}
