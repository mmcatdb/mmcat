package cz.matfyz.tests.querying;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.algorithms.QueryPlanner;
import cz.matfyz.querying.algorithms.QueryProjector;
import cz.matfyz.querying.algorithms.QueryResolver;
import cz.matfyz.querying.algorithms.SchemaExtractor;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class QueryCustomTreeTest<TWrapper extends AbstractControlWrapper> {

    @FunctionalInterface
    public static interface QueryTreeBuilderFunction {
        QueryNode build(SchemaCategory schema, Datasource datasource, Set<PatternForKind> plan);
    }

    public QueryCustomTreeTest(
        Datasources datasources,
        TestDatasource<TWrapper> testDatasource,
        String queryString,
        QueryTreeBuilderFunction queryTreeBuilder,
        String expectedResult
    ) {
        this.datasources = datasources;
        this.testDatasource = testDatasource;
        this.queryString = queryString;
        this.queryTreeBuilder = queryTreeBuilder;
        this.expectedResult = expectedResult;
    }

    private final Datasources datasources;
    private final TestDatasource<TWrapper> testDatasource;
    private final QueryTreeBuilderFunction queryTreeBuilder;
    private final String queryString;
    private final String expectedResult;

    private static final ObjectMapper mapper = new ObjectMapper();

    private ResultList doQuery() {
        // ! from QueryTestBase ctor and builder
        final var schema = datasources.schema;

        // ! from QueryTestBase.defineKinds
        final var provider = new DefaultControlWrapperProvider();
        final var datasource = testDatasource.datasource();
        provider.setControlWrapper(datasource, testDatasource.wrapper);
        final List<Mapping> kinds = testDatasource.mappings;

        // ! from QueryToInstance.innerExecute() (onward from here...)

        final Query query = QueryParser.parse(queryString);
        query.context.setProvider(provider);

        // from QueryTreeBuilder.run()
        final var extracted = SchemaExtractor.run(query.context, schema, kinds, query.where);
        final List<Set<PatternForKind>> plans = QueryPlanner.run(extracted);

        final var plan = plans.get(0);

        final var queryTree = queryTreeBuilder.build(schema, datasource, plan);

        // ! the rest is left unchanged
        final QueryResult selection = QueryResolver.run(query.context, queryTree);
        final QueryResult projection = QueryProjector.run(query.context, query.select, selection);

        return projection.data;
    }

    public void run() {
        final ResultList result = doQuery();
        final var jsonResults = result.toJsonArray();

        final JsonNode jsonResult = parseJsonResult(jsonResults);
        final JsonNode expectedResultJson = parseExpectedResult(expectedResult);
        assertEquals(expectedResultJson, jsonResult);
    }

    private static JsonNode parseJsonResult(List<String> jsonResults) {
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

    private static JsonNode parseExpectedResult(String expectedJson) {
        try {
            return mapper.readTree(expectedJson);
        }
        catch (Exception e) {
            fail(e);
            return null;
        }
    }
}
