package cz.matfyz.tests.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.matfyz.tests.example.benchmarkyelp.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.QueryNormalizer;
import cz.matfyz.querying.optimizer.QueryCostEstimator;
import cz.matfyz.querying.optimizer.QueryOptimizer;
import cz.matfyz.querying.parser.ParsedQuery;
import cz.matfyz.querying.parser.QueryParser;
import cz.matfyz.querying.planner.PlanDrafter;
import cz.matfyz.querying.planner.PlanJoiner;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.planner.SchemaExtractor;
import cz.matfyz.querying.core.patterntree.PatternForKind;

public class QueryEstimator {

    public static record EvaluatedPlan(QueryPlan plan, long cost) {}

    public QueryEstimator(
        Datasources datasources,
        List<TestDatasource<?>> testDatasources,
        String queryString,
        boolean optimize
    ) {
        this.datasources = datasources;
        this.testDatasources = testDatasources;
        this.queryString = queryString;
        this.optimize = optimize;
    }

    private final Datasources datasources;
    private final List<TestDatasource<?>> testDatasources;
    private final String queryString;
    private final boolean optimize;

    public List<EvaluatedPlan> run() {
        // ! from QueryTestBase ctor and builder
        final var schema = datasources.schema;

        // ! from QueryTestBase.defineKinds

        final var provider = new DefaultControlWrapperProvider();
        final List<Mapping> kinds = defineKinds(provider);

        // ! from QueryToInstance.innerExecute() (onward from here...)

        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);
        normalized.context.setProvider(provider);

        // from QueryTreeBuilder.run()
        final var extracted = SchemaExtractor.run(normalized.context, schema, kinds, normalized.selection);
        final List<Set<PatternForKind>> plans = PlanDrafter.run(extracted);

        final var output = new ArrayList<EvaluatedPlan>();

        for (final var plan : plans) {
            QueryNode currentNode = PlanJoiner.run(normalized.context, plan, normalized.selection.variables());

            for (final var filter : normalized.selection.filters())
                currentNode = new FilterNode(currentNode, filter);

            QueryPlan planned = new QueryPlan(currentNode, normalized.context);

            if (optimize)
                planned = QueryOptimizer.run(planned);

            long costOverNet = QueryCostEstimator.run(planned);

            output.add(new EvaluatedPlan(planned, costOverNet));
        }

        output.sort((x, y) -> y.cost() - x.cost() >= 0 ? 1 : -1); // descending
        return output;
    }

    private List<Mapping> defineKinds(DefaultControlWrapperProvider provider) {
        return testDatasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();
    }
}
