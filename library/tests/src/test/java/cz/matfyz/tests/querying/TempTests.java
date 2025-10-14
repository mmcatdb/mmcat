package cz.matfyz.tests.querying;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.ResultNode;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.QueryNormalizer;
import cz.matfyz.querying.optimizer.QueryOptimizer;
import cz.matfyz.querying.parser.ParsedQuery;
import cz.matfyz.querying.parser.QueryParser;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.planner.QueryPlanner;
import cz.matfyz.querying.resolver.SelectionResolver;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;

import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TempTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);

    private static final Datasources datasources = new Datasources();
    private static final DefaultControlWrapperProvider provider = new DefaultControlWrapperProvider();
    private static final List<Mapping> kinds = defineKinds(provider, List.of(datasources.postgreSQL()));

    @Test
    void test() {
        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);
        final var context = new QueryContext(datasources.schema, provider, normalized.selection.variables());

        final QueryPlan planned = QueryPlanner.run(context, kinds, normalized.selection);
        final QueryPlan optimized = QueryOptimizer.run(planned);

        final var output = SelectionResolver.run(optimized);

        LOGGER.info("OK");
        LOGGER.info("\n" + output.data);
    }

    private static final String queryString = """
        SELECT {
            ?order number ?number .
        }
        WHERE {
            ?number -1 ?order .
        }
    """;

    private static List<Mapping> defineKinds(DefaultControlWrapperProvider provider, List<TestDatasource<?>> testDatasources) {
        return testDatasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            })
            .toList();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void resultToStringTest() {
        final var builder = new ListResult.TableBuilder();

        builder.addColumns(List.of("colum1", "column2", "column3"));
        builder.addRow(List.of("a1", "a2", "a3"));
        builder.addRow(List.of("b1", "b2", "b3"));
        builder.addRow(List.of("c1", "c2", "c3"));

        final var rootMap = new TreeMap<String, ResultNode>();
        rootMap.put("x", builder.build());
        final var root = new MapResult(rootMap);

        final var rootArray = new ListResult(List.of(root));

        LOGGER.info("\n" + rootArray.toString());

        assertDoesNotThrow(() -> {
            LOGGER.info("\n" + mapper.writeValueAsString(rootArray));
        });
    }

}
