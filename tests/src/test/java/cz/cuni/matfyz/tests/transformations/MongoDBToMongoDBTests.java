package cz.cuni.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.tests.database.BasicDatabases;
import cz.cuni.matfyz.tests.database.TestDatabase;
import cz.cuni.matfyz.tests.mapping.MongoDB;
import cz.cuni.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDDLWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDMLWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBToMongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToMongoDBTests.class);

    private static final BasicDatabases databases = new BasicDatabases();
    private static final SchemaCategory schema = databases.schema;
    private static final TestDatabase<MongoDBControlWrapper> database = databases.mongoDB();

    @BeforeAll
    public static void setup() {
        database.setup();
    }

    @Test
    public void basicTest() {
        new PullToDDLAndDMLTestBase(
            database.wrapper.getPullWrapper(),
            new MongoDBDDLWrapper(),
            new MongoDBDMLWrapper(),
            MongoDB.order(schema)
        )
            .run();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void jsonTest() throws Exception {
        final ComplexProperty path = MongoDB.order(schema).accessPath();
        LOGGER.trace(path.toString());

        final var jsonString = mapper.writer().writeValueAsString(path);
        LOGGER.trace(jsonString);

        final AccessPath parsedPath = mapper.readerFor(AccessPath.class).readValue(jsonString);
        LOGGER.trace(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }

    @Test
    public void complex_arrayTest() {
        new PullToDDLAndDMLTestBase(
            database.wrapper.getPullWrapper(),
            new MongoDBDDLWrapper(),
            new MongoDBDMLWrapper(),
            MongoDB.item(schema)
        )
            .run();
    }
}
