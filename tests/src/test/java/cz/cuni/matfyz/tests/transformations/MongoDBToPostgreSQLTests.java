package cz.cuni.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.tests.database.BasicDatabases;
import cz.cuni.matfyz.tests.mapping.PostgreSQL;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLDDLWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLDMLWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBToPostgreSQLTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToPostgreSQLTests.class);

    private static final BasicDatabases databases = new BasicDatabases();
    private static final SchemaCategory schema = databases.schema;

    @BeforeAll
    public static void setup() {
        databases.mongoDB().setup();
        databases.postgreSQL().setup();
    }

    @Test
    public void basicTest() {
        new PullToDDLAndDMLTestBase(
            databases.mongoDB().wrapper.getPullWrapper(),
            new PostgreSQLDDLWrapper(),
            new PostgreSQLDMLWrapper(),
            PostgreSQL.order(schema)
        )
            .run();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void jsonTest() throws Exception {
        final ComplexProperty path = PostgreSQL.order(schema).accessPath();
        LOGGER.trace(path.toString());

        final var jsonString = mapper.writer().writeValueAsString(path);
        LOGGER.trace(jsonString);

        final AccessPath parsedPath = mapper.readerFor(AccessPath.class).readValue(jsonString);
        LOGGER.trace(parsedPath.toString());
        
        assertEquals(path.toString(), parsedPath.toString());
    }
}
