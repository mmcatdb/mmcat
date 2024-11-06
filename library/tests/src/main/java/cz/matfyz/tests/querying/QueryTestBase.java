package cz.matfyz.tests.querying;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.tests.example.common.TestDatasource;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private List<TestDatasource<?>> datasources = new ArrayList<>();

    public QueryTestBase addDatasource(TestDatasource<?> datasource) {
        datasources.add(datasource);

        return this;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public void run() {
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);
        final var queryToInstance = new QueryToInstance(provider, schema, queryString, kinds);

        final ResultList result = queryToInstance.execute();
        final var jsonResults = result.toJsonArray();
        LOGGER.info("\n{}", jsonResults);

        final JsonNode jsonResult = parseJsonResult(jsonResults);
        final JsonNode expectedResult = parseExpectedResult(expectedJson);
        assertEquals(expectedResult, jsonResult);
    }

    private List<Kind> defineKinds(DefaultControlWrapperProvider provider) {
        return datasources.stream()
            .flatMap(testDatasource -> {
                final var datasource = new Datasource(testDatasource.type, testDatasource.id);
                provider.setControlWrapper(datasource, testDatasource.wrapper);
                return testDatasource.mappings.stream().map(mapping -> new Kind(mapping, datasource));
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
