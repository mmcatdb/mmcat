package cz.matfyz.tests.example.tpch;

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
                .addMapping(PostgreSQL.orders(schema))
                .addMapping(PostgreSQL.lineItem(schema));

        return postgreSQL;
    }

    public TestDatasource<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return datasourceProvider.createPostgreSQL(PostgreSQL.datasource.identifier, schema, "setupPostgresqlTpch.sql");
    }

    // MongoDB

    private TestDatasource<MongoDBControlWrapper> mongoDB;

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null)
            mongoDB = createNewMongoDB();

        return mongoDB;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(MongoDB.datasource.identifier, schema, null);
    }

    // Neo4j

    private TestDatasource<Neo4jControlWrapper> neo4j;

    public TestDatasource<Neo4jControlWrapper> neo4j() {
        if (neo4j == null)
            neo4j = createNewNeo4j()
                .addMapping(Neo4j.supplier(schema))
                .addMapping(Neo4j.part(schema))
                .addMapping(Neo4j.partSupp(schema))
                .addMapping(Neo4j.suppliedBy(schema))
                .addMapping(Neo4j.isForPart(schema));

        return neo4j;
    }

    public TestDatasource<Neo4jControlWrapper> createNewNeo4j() {
        return datasourceProvider.createNeo4j(Neo4j.datasource.identifier, schema, null);
    }

}
