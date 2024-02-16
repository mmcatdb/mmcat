package cz.matfyz.tests.querying;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.tests.example.common.TestDatabase;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class QueryTestBase {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestBase.class);
    
    private final SchemaCategory schema;

    public QueryTestBase(SchemaCategory schema) {
        this.schema = schema;
    }

    private String queryString;

    public QueryTestBase query(String queryString) {
        this.queryString = queryString;

        return this;
    }

    private String expectedJson;

    public QueryTestBase expected(String expectedJson) {
        this.expectedJson = expectedJson;

        return this;
    }

    private List<TestDatabase<?>> databases = new ArrayList<>();

    public QueryTestBase addDatabase(TestDatabase<?> database) {
        databases.add(database);

        return this;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public void run() {
        final var kinds = defineKinds();
        final var queryToInstance = new QueryToInstance(schema, queryString, kinds);
        
        final ResultList result = queryToInstance.execute();
        final var jsonResults = result.toJsonArray();
        LOGGER.info("\n{}", jsonResults);
        
        final JsonNode jsonResult = parseJsonResult(jsonResults);
        final JsonNode expectedResult = parseExpectedResult(expectedJson);        
        assertEquals(expectedResult, jsonResult);
    }

    private List<Kind> defineKinds() {
        return databases.stream().flatMap(testDatabase -> {
            final var builder = new Database.Builder();
            testDatabase.mappings.forEach(builder::mapping);
            final var database = builder.build(testDatabase.type, testDatabase.wrapper, testDatabase.id);

            return database.kinds.stream();
        }).toList();
    }

    private JsonNode parseJsonResult(List<String> jsonResults) {
        try {
            final ArrayNode arrayResult = mapper.createArrayNode();
            for (final String jsonResult : jsonResults)
                arrayResult.add(mapper.readTree(jsonResult));

            return arrayResult;
        }
        catch (Exception e) {
            fail(e);
            return null;
        }
    }

    private JsonNode parseExpectedResult(String expectedJson) {
        try {
            return mapper.readTree(expectedJson);
        }
        catch (Exception e) {
            fail(e);
            return null;
        }
    }

}
