package cz.matfyz.tests.database;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.mapping.TestMapping;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase<TWrapper extends AbstractControlWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDatabase.class);

    private static int lastId = 0;
    
    public final String id;
    public final TWrapper wrapper;
    public final List<Mapping> mappings = new ArrayList<>();
    public final SchemaCategory schema;
    private final String setupFileName;

    private TestDatabase(TWrapper wrapper, SchemaCategory schema, String setupFileName) {
        this.id = "" + lastId++;
        this.wrapper = wrapper;
        this.schema = schema;
        this.setupFileName = setupFileName;
    }

    public static TestDatabase<PostgreSQLControlWrapper> createPostgreSQL(PostgreSQLProvider provider, SchemaCategory schema) {
        return new TestDatabase<>(new PostgreSQLControlWrapper(provider), schema, "setupPostgresql.sql");
    }

    public static TestDatabase<MongoDBControlWrapper> createMongoDB(MongoDBProvider provider, SchemaCategory schema) {
        return new TestDatabase<>(new MongoDBControlWrapper(provider), schema, "setupMongodb.js");
    }

    public static TestDatabase<Neo4jControlWrapper> createNeo4j(Neo4jProvider provider, SchemaCategory schema) {
        return new TestDatabase<>(new Neo4jControlWrapper(provider), schema, "setupNeo4j.cypher");
    }

    public TestDatabase<TWrapper> addMapping(TestMapping testMapping) {
        mappings.add(testMapping.mapping());

        return this;
    }

    public void setup() {
        wrapper.execute(getFilePath());
    }

    private Path getFilePath() {
        try {
            final var url = ClassLoader.getSystemResource(setupFileName);
            return Paths.get(url.toURI()).toAbsolutePath();
        }
        catch (URISyntaxException e) {
            LOGGER.error("Database setup error: ", e);
            throw new RuntimeException(e);
        }
    }
    
}
