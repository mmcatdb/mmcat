package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBDDLWrapper;
import cz.matfyz.wrappermongodb.MongoDBDMLWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MongoDBToMongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToMongoDBTests.class);

    private static final Datasources datasources = new Datasources();
    private static final SchemaCategory schema = datasources.schema;
    private static final TestDatasource<MongoDBControlWrapper> datasource = datasources.mongoDB();

    @BeforeAll
    public static void setup() {
        datasource.setup();
    }

    @Test
    void basicTest() {
        new PullToDDLAndDMLTestBase(
            datasource.wrapper.getPullWrapper(),
            new MongoDBDDLWrapper(),
            new MongoDBDMLWrapper(),
            MongoDB.order(schema)
        )
            .run();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void jsonTest() throws Exception {
        final ComplexProperty path = MongoDB.order(schema).accessPath();
        LOGGER.trace(path.toString());

        final var jsonString = mapper.writer().writeValueAsString(path);
        LOGGER.trace(jsonString);

        final AccessPath parsedPath = mapper.readerFor(AccessPath.class).readValue(jsonString);
        LOGGER.trace(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }

    @Test
    void complex_arrayTest() {
        new PullToDDLAndDMLTestBase(
            datasource.wrapper.getPullWrapper(),
            new MongoDBDDLWrapper(),
            new MongoDBDMLWrapper(),
            MongoDB.item(schema)
        )
            .run();
    }
}
