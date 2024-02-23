package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatabaseProvider;
import cz.matfyz.tests.example.common.TestDatabase;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

public class Databases {

    public final SchemaCategory schema = Schema.newSchemaCategory();

    private static final DatabaseProvider databaseProvider = new DatabaseProvider("tests");

    // PostgreSQL

    private TestDatabase<PostgreSQLControlWrapper> postgreSQL;

    public TestDatabase<PostgreSQLControlWrapper> postgreSQL() {
        if (postgreSQL == null)
            postgreSQL = createNewPostgreSQL()
                .addMapping(PostgreSQL.order(schema))
                .addMapping(PostgreSQL.product(schema))
                .addMapping(PostgreSQL.item(schema));

        return postgreSQL;
    }

    public TestDatabase<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return databaseProvider.createPostgreSQL(schema, "setupPostgresqlBasic.sql");
    }

    // MongoDB

    private TestDatabase<MongoDBControlWrapper> mongoDB;

    public TestDatabase<MongoDBControlWrapper> mongoDB() {
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

    public TestDatabase<MongoDBControlWrapper> createNewMongoDB() {
        return databaseProvider.createMongoDB(schema, "setupMongodbBasic.js");
    }

    // Neo4j

    private TestDatabase<Neo4jControlWrapper> neo4j;

    public TestDatabase<Neo4jControlWrapper> neo4j() {
        if (neo4j == null)
            neo4j = createNewNeo4j()
                .addMapping(Neo4j.order(schema))
                .addMapping(Neo4j.item(schema));

        return neo4j;
    }

    public TestDatabase<Neo4jControlWrapper> createNewNeo4j() {
        return databaseProvider.createNeo4j(schema, "setupNeo4j.cypher");
    }

}
