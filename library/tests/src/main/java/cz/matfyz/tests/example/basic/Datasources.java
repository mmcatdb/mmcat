package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests");

    // PostgreSQL

    private TestDatasource<PostgreSQLControlWrapper> postgreSQL;

    public TestDatasource<PostgreSQLControlWrapper> postgreSQL() {
        if (postgreSQL == null)
            postgreSQL = createNewPostgreSQL()
                .addMapping(PostgreSQL.order(schema))
                .addMapping(PostgreSQL.product(schema))
                .addMapping(PostgreSQL.item(schema));

        return postgreSQL;
    }

    public TestDatasource<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return datasourceProvider.createPostgreSQL(schema, "setupPostgresqlBasic.sql");
    }

    // MongoDB

    private TestDatasource<MongoDBControlWrapper> mongoDB;

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null)
            mongoDB = createNewMongoDB()
                .addMapping(MongoDB.order(schema))
                .addMapping(MongoDB.address(schema))
                .addMapping(MongoDB.tag(schema))
                .addMapping(MongoDB.item(schema))
                .addMapping(MongoDB.contact(schema))
                .addMapping(MongoDB.customer(schema))
                .addMapping(MongoDB.note(schema));

        return mongoDB;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(schema, "setupMongodbBasic.js");
    }

    // Neo4j

    private TestDatasource<Neo4jControlWrapper> neo4j;

    public TestDatasource<Neo4jControlWrapper> neo4j() {
        if (neo4j == null)
            neo4j = createNewNeo4j()
                .addMapping(Neo4j.order(schema))
                .addMapping(Neo4j.item(schema));

        return neo4j;
    }

    public TestDatasource<Neo4jControlWrapper> createNewNeo4j() {
        return datasourceProvider.createNeo4j(schema, "setupNeo4j.cypher");
    }

}
