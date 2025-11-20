package cz.matfyz.tests.querying;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.querying.core.QueryDescription.QueryPlanDescription;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.tests.example.common.TestDatasource;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.List;
import java.util.function.Predicate;

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

    private Predicate<QueryPlanDescription> restrictQueryTree;

    public QueryTestBase restrictQueryTree(Predicate<QueryPlanDescription> restrictionFunction) {
        this.restrictQueryTree = restrictionFunction;

        return this;
    }

    private CollectorCache cache;

    public QueryTestBase cache(CollectorCache cache) {
        this.cache = cache;

        return this;
    }

    private final List<TestDatasource<?>> datasources = new ArrayList<>();

    public QueryTestBase addDatasource(TestDatasource<?> datasource) {
        datasources.add(datasource);

        return this;
    }

    private boolean ordered = false;

    public QueryTestBase ordered() {
        ordered = true;
        return this;
    }

    public void run() {
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);
        final var queryToInstance = new QueryToInstance(provider, schema, queryString, kinds, cache);

        if (restrictQueryTree != null) {
            final var description = queryToInstance.describe();
            assertTrue(restrictQueryTree.test(description.optimized()), "Query tree restriction was not satisfied.");
        }

        if (expectedJson != null) {
            final ListResult result = queryToInstance.execute().result();
            final var jsonResults = result.toJsonArray();
            LOGGER.info("\n{}", jsonResults);

            final JsonNode jsonResult = parseJsonResult(jsonResults);
            final JsonNode expectedResult = parseExpectedResult(expectedJson);

            if (ordered) {
                assertEquals(expectedResult, jsonResult);
            } else {
                final var arrayResult = (ArrayNode)jsonResult;
                final var expectedArray = (ArrayNode)expectedResult;

                final var listResult = new ArrayList<JsonNode>(arrayResult.size());
                for (final var value : arrayResult) listResult.add(value);
                final var listExpected = new ArrayList<JsonNode>(expectedArray.size());
                for (final var value : expectedArray) listExpected.add(value);

                listResult.sort(new JsonComparator());
                listExpected.sort(new JsonComparator());
                assertEquals(listExpected, listResult);
            }
        }
    }

    private List<Mapping> defineKinds(DefaultControlWrapperProvider provider) {
        return datasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

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

    public void describe() {
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);
        final var queryToInstance = new QueryToInstance(provider, schema, queryString, kinds, cache);

        final var description = queryToInstance.describe();

        try {
            LOGGER.info("\n{}", mapper.writeValueAsString(description));
        }
        catch (Exception e) {
            fail(e);
        }
    }

    public QueryTestBase copy() {
        final var copy = new QueryTestBase(schema);
        copy.queryString = queryString;
        copy.expectedJson = expectedJson;
        copy.restrictQueryTree = restrictQueryTree;
        copy.cache = cache;
        copy.ordered = ordered;
        copy.datasources.addAll(datasources);
        return copy;
    }

    static class JsonComparator implements java.util.Comparator<JsonNode> {

        @Override public int compare(JsonNode arg0, JsonNode arg1) {
            if (arg0.isObject() || arg1.isObject()) {
                if (!arg0.isObject()) return -1;
                if (!arg1.isObject()) return 1;

                // get all the fields and recursively compare
                arg0.properties();
                final var keySet = new TreeSet<String>(arg0.properties().stream().map(e -> e.getKey()).toList());
                keySet.addAll(arg1.properties().stream().map(e -> e.getKey()).toList());

                for (final String key : keySet) {
                    if (!arg0.has(key)) return -1;
                    if (!arg1.has(key)) return 1;

                    final var result = compare(arg0.get(key), arg1.get(key));
                    if (result != 0) return result;
                }
            } else if (arg0.isArray() || arg1.isArray()) {
                if (!arg0.isArray()) return -1;
                if (!arg1.isArray()) return 1;

                // get all the elements and recursively compare
                if (arg0.size() != arg1.size()) return arg0.size() - arg1.size();
                for (int i = 0; i < arg0.size(); i++) {
                    final var result = compare(arg0.get(i), arg1.get(i));
                    if (result != 0) return result;
                }
            } else { // both are value nodes; for now we assume string
                return arg0.asText().compareTo(arg1.asText());
            }

            return 0;
        }

    }

}
