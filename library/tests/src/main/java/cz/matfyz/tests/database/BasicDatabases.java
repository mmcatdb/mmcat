package cz.matfyz.tests.database;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Config;
import cz.matfyz.tests.mapping.MongoDB;
import cz.matfyz.tests.mapping.Neo4j;
import cz.matfyz.tests.mapping.PostgreSQL;
import cz.matfyz.tests.schema.BasicSchema;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;

public class BasicDatabases {
    
    public final SchemaCategory schema = BasicSchema.newSchemaCategory();

    /**
     * PostgreSQL
     */

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
        return TestDatabase.createPostgreSQL(getPostgreSQLProvider(), schema);
    }

    private static PostgreSQLProvider postgreSQLProvider;

    private static PostgreSQLProvider getPostgreSQLProvider() {
        if (postgreSQLProvider == null) {
            postgreSQLProvider = new PostgreSQLProvider(new PostgreSQLSettings(
                Config.getBool("tests.isInDocker") ? "mmcat-postgresql" : "localhost",
                Config.get("tests.postgresqlPort"),
                Config.get("tests.database"),
                Config.get("tests.username"),
                Config.get("tests.password")
            ));
        }

        return postgreSQLProvider;
    }

    /**
     * MongoDB
     */

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
        return TestDatabase.createMongoDB(getMongoDBProvider(), schema);
    }

    private static MongoDBProvider mongoDBProvider;

    private static MongoDBProvider getMongoDBProvider() {
        if (mongoDBProvider == null) {
            mongoDBProvider = new MongoDBProvider(new MongoDBSettings(
                Config.getBool("tests.isInDocker") ? "mmcat-mongodb" : "localhost",
                Config.get("tests.mongodbPort"),
                "admin",
                Config.get("tests.database"),
                Config.get("tests.username"),
                Config.get("tests.password")
            ));
        }

        return mongoDBProvider;
    }

    /**
     * Neo4j
     */

    private TestDatabase<Neo4jControlWrapper> neo4j;

    public TestDatabase<Neo4jControlWrapper> neo4j() {
        if (neo4j == null)
            neo4j = createNewNeo4j()
                .addMapping(Neo4j.order(schema))
                .addMapping(Neo4j.item(schema));

        return neo4j;
    }

    public TestDatabase<Neo4jControlWrapper> createNewNeo4j() {
        return TestDatabase.createNeo4j(getNeo4jProvider(), schema);
    }

    private static Neo4jProvider neo4jProvider;

    private static Neo4jProvider getNeo4jProvider() {
        if (neo4jProvider == null) {
            neo4jProvider = new Neo4jProvider(new Neo4jSettings(
                Config.getBool("tests.isInDocker") ? "mmcat-neo4j" : "localhost",
                Config.get("tests.neo4jPort"),
                "neo4j",
                "neo4j",
                Config.get("tests.password")
            ));
        }

        return neo4jProvider;
    }

}
