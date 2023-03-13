package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.wrappermongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBSettings;
import cz.cuni.matfyz.wrapperneo4j.Neo4jSessionProvider;
import cz.cuni.matfyz.wrapperneo4j.Neo4jSettings;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLSettings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
abstract class DatabaseSetup {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetup.class);

    private DatabaseSetup() {}

    private static class PostgreSQL {
        static final String HOST = Config.get("postgresql.host");
        static final String PORT = Config.get("postgresql.port");
        static final String DATABASE = Config.get("postgresql.database");
        static final String USERNAME = Config.get("postgresql.username");
        static final String PASSWORD = Config.get("postgresql.password");
    }

    static PostgreSQLConnectionProvider createPostgreSQLConnectionProvider() {
        return new PostgreSQLConnectionProvider(new PostgreSQLSettings(
            PostgreSQL.HOST,
            PostgreSQL.PORT,
            PostgreSQL.DATABASE,
            PostgreSQL.USERNAME,
            PostgreSQL.PASSWORD
        ));
    }

    static void executePostgreSQLScript(String pathToFile) throws Exception {
        String beforePasswordString = new StringBuilder()
            .append("psql postgresql://")
            .append(PostgreSQL.USERNAME)
            .append(":")
            .toString();

        String afterPasswordString = new StringBuilder()
            .append("@")
            .append(PostgreSQL.HOST)
            .append(":")
            .append(PostgreSQL.PORT)
            .append("/")
            .append(PostgreSQL.DATABASE)
            .append(" -f ")
            .append(pathToFile)
            .toString();
        
        LOGGER.info("Executing: " + beforePasswordString + "********" + afterPasswordString);

        String commandString = beforePasswordString + PostgreSQL.PASSWORD + afterPasswordString;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String info = bufferReader.lines().collect(Collectors.joining());
        LOGGER.info(info);
    }

    private static class MongoDB {
        static final String HOST = Config.get("mongodb.host");
        static final String PORT = Config.get("mongodb.port");
        static final String DATABASE = Config.get("mongodb.database");
        static final String AUTHENTICATIONDATABASE = Config.get("mongodb.authenticationDatabase");
        static final String USERNAME = Config.get("mongodb.username");
        static final String PASSWORD = Config.get("mongodb.password");
    }

    static MongoDBDatabaseProvider createMongoDBDatabaseProvider() {
        return new MongoDBDatabaseProvider(new MongoDBSettings(
            MongoDB.HOST,
            MongoDB.PORT,
            MongoDB.DATABASE,
            MongoDB.AUTHENTICATIONDATABASE,
            MongoDB.USERNAME,
            MongoDB.PASSWORD
        ));
    }

    static void executeMongoDBScript(String pathToFile) throws Exception {
        String beforePasswordString = new StringBuilder()
            .append("mongo --username ")
            .append(MongoDB.USERNAME)
            .append(" --password ").toString();

        String afterPasswordString = new StringBuilder()
            .append(" --authenticationDatabase ")
            .append(MongoDB.AUTHENTICATIONDATABASE)
            .append(" ")
            .append(MongoDB.HOST)
            .append(":")
            .append(MongoDB.PORT)
            .append("/")
            .append(MongoDB.DATABASE)
            .append(" ")
            .append(pathToFile)
            .toString();

        LOGGER.info("Executing: " + beforePasswordString + "********" + afterPasswordString);

        String commandString = beforePasswordString + MongoDB.PASSWORD + afterPasswordString;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));
    }

    private static class Neo4j {
        static final String HOST = Config.get("neo4j.host");
        static final String PORT = Config.get("neo4j.port");
        static final String DATABASE = Config.get("neo4j.database");
        static final String USERNAME = Config.get("neo4j.username");
        static final String PASSWORD = Config.get("neo4j.password");
    }

    static Neo4jSessionProvider createNeo4jSessionProvider() {
        return new Neo4jSessionProvider(new Neo4jSettings(
            Neo4j.HOST,
            Neo4j.PORT,
            Neo4j.DATABASE,
            Neo4j.USERNAME,
            Neo4j.PASSWORD
        ));
    }

    static void executeNeo4jScript(String pathToFile) throws Exception {
        String beforePasswordString = new StringBuilder()
            .append("cypher-shell -f ")
            .append(pathToFile)
            .append(" -a bolt://")
            .append(Neo4j.HOST)
            .append(":")
            .append(Neo4j.PORT)
            .append(" -u ")
            .append(Neo4j.USERNAME)
            .append(" -p ")
            .toString();

        LOGGER.info("Executing: " + beforePasswordString + "********");

        String commandString = beforePasswordString + Neo4j.PASSWORD;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));
    }

}
