package cz.matfyz.tests.querying;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import cz.matfyz.tests.example.benchmarkyelp.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.schema.SchemaCategory;
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
import cz.matfyz.querying.resolver.ProjectionResolver;
import cz.matfyz.querying.resolver.SelectionResolver;
import cz.matfyz.querying.resolver.QueryPlanDescriptor;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class QueryEstimator<TWrapper extends AbstractControlWrapper> {

    public QueryEstimator(
        Datasources datasources,
        TestDatasource<TWrapper> testDatasource,
        String queryString,
        boolean optimize
    ) {
        this.datasources = datasources;
        this.testDatasource = testDatasource;
        this.queryString = queryString;
        this.optimize = optimize;
    }

    private final Datasources datasources;
    private final TestDatasource<TWrapper> testDatasource;
    private final String queryString;
    private final boolean optimize;

    public void run() {
        // ! from QueryTestBase ctor and builder
        final var schema = datasources.schema;

        // ! from QueryTestBase.defineKinds
        final var provider = new DefaultControlWrapperProvider();
        final var datasource = testDatasource.datasource();
        provider.setControlWrapper(datasource, testDatasource.wrapper);
        final List<Mapping> kinds = testDatasource.mappings;

        // ! from QueryToInstance.innerExecute() (onward from here...)

        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);
        normalized.context.setProvider(provider);

        // from QueryTreeBuilder.run()
        final var extracted = SchemaExtractor.run(normalized.context, schema, kinds, normalized.selection);
        final List<Set<PatternForKind>> plans = PlanDrafter.run(extracted);

        for (final var plan : plans) {
            QueryNode currentNode = PlanJoiner.run(normalized.context, plan, normalized.selection.variables());

            for (final var filter : normalized.selection.filters())
                currentNode = new FilterNode(currentNode, filter);

            QueryPlan planned = new QueryPlan(currentNode, normalized.context);

            if (optimize)
                planned = QueryOptimizer.run(planned);

            int costOverNet = QueryCostEstimator.run(planned);

            final var serialized = QueryPlanDescriptor.run(planned);

            System.out.println("Plan:");
            System.out.println(serialized.toString());
            System.out.println("Cost over network: " + costOverNet);
            System.out.println("------");
        }
    }
}
