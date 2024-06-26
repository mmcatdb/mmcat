package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.wrapperpostgresql.PostgreSQLDDLWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLDMLWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MongoDBToPostgreSQLTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToPostgreSQLTests.class);

    private static final Datasources datasources = new Datasources();
    private static final SchemaCategory schema = datasources.schema;

    @BeforeAll
    static void setup() {
        datasources.mongoDB().setup();
        datasources.postgreSQL().setup();
    }

    @Test
    void basicTest() {
        new PullToDDLAndDMLTestBase(
            datasources.mongoDB().wrapper.getPullWrapper(),
            new PostgreSQLDDLWrapper(),
            new PostgreSQLDMLWrapper(),
            PostgreSQL.order(schema)
        )
            .run();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void jsonTest() throws Exception {
        final ComplexProperty path = PostgreSQL.order(schema).accessPath();
        LOGGER.trace(path.toString());

        final var jsonString = mapper.writer().writeValueAsString(path);
        LOGGER.trace(jsonString);

        final AccessPath parsedPath = mapper.readerFor(AccessPath.class).readValue(jsonString);
        LOGGER.trace(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }
}
