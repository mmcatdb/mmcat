package cz.matfyz.tests.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.QueryNormalizer;
import cz.matfyz.querying.optimizer.QueryDebugPrinter;
import cz.matfyz.querying.parser.ParsedQuery;
import cz.matfyz.querying.parser.QueryParser;
import cz.matfyz.querying.planner.PatternExtractor;
import cz.matfyz.querying.planner.PlanDrafter;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.planner.ResultStructureResolver;
import cz.matfyz.querying.resolver.ProjectionResolver;
import cz.matfyz.querying.resolver.SelectionResolver;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class QueryCustomTreeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestBase.class);

    @FunctionalInterface
    public static interface QueryTreeBuilderFunction {
        QueryNode build(List<Set<PatternForKind>> draftPlans, ExpressionScope scope);
    }

    private final SchemaCategory schema;

    public QueryCustomTreeTest(SchemaCategory schema) {
        this.schema = schema;
    }

    private String queryString;

    public QueryCustomTreeTest query(String queryString) {
        this.queryString = queryString;

        return this;
    }

    private String expectedJson;

    public QueryCustomTreeTest expected(String expectedJson) {
        this.expectedJson = expectedJson;

        return this;
    }

    private QueryTreeBuilderFunction queryTreeBuilder;

    public QueryCustomTreeTest queryTreeBuilder(QueryTreeBuilderFunction builder) {
        queryTreeBuilder = builder;

        return this;
    }

    private final List<TestDatasource<?>> datasources = new ArrayList<>();

    public QueryCustomTreeTest addDatasource(TestDatasource<?> datasource) {
        datasources.add(datasource);

        return this;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private ListResult doQuery() {
        // ! from QueryTestBase.defineKinds
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);

        // ! from QueryToInstance.innerExecute() (onward from here...)

        final var startNanos = System.nanoTime();

        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);
        final var context = new QueryContext(schema, provider, normalized.selection.variables());

        // from QueryTreeBuilder.run()
        final var extracted = PatternExtractor.run(context, kinds, normalized.selection);
        final List<Set<PatternForKind>> plans = PlanDrafter.run(extracted);

        final var queryTree = queryTreeBuilder.build(plans, normalized.selection.scope());
        final QueryPlan planned = new QueryPlan(queryTree, context, normalized.selection.scope());
        ResultStructureResolver.run(planned);

        // final QueryPlan optimized = QueryOptimizer.run(planned, cache); // Due to custom tree so far not neccessary
        final QueryPlan optimized = planned;

        final var preEvalMillis = (int)((System.nanoTime() - startNanos) / 1_000_000);

        // ! the rest is left unchanged
        final QueryResult selected = SelectionResolver.run(optimized);
        final QueryResult projected = ProjectionResolver.run(context, normalized.projection, selected);

        // optimized
        LOGGER.info("Parsing & creating plans took {} ms", preEvalMillis);
        LOGGER.info("Evaluated query took {} ms", optimized.root.evaluationMillis);
        LOGGER.info("Detailed execution time info:\n{}", QueryDebugPrinter.measuredCost(optimized.root));

        return projected.data;
    }

    public void run() {
        final ListResult result = doQuery();
        final var jsonResults = result.toJsonArray();

        final JsonNode jsonResult = parseJsonResult(jsonResults);
        final JsonNode expectedResultJson = parseExpectedResult(expectedJson);
        assertEquals(expectedResultJson, jsonResult);
    }

    private List<Mapping> defineKinds(DefaultControlWrapperProvider provider) {
        return datasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();
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

    public QueryCustomTreeTest copy() {
        final var copy = new QueryCustomTreeTest(schema);
        copy.queryString = queryString;
        copy.expectedJson = expectedJson;
        copy.queryTreeBuilder = queryTreeBuilder;
        copy.datasources.addAll(datasources);
        return copy;
    }
}
