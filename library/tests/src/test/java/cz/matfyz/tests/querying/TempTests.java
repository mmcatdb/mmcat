package cz.matfyz.tests.querying;

import cz.matfyz.abstractwrappers.datasource.Datasource;
import cz.matfyz.abstractwrappers.datasource.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.querying.queryresult.ResultMap;
import cz.matfyz.core.querying.queryresult.ResultNode;
import cz.matfyz.querying.algorithms.QueryResolver;
import cz.matfyz.querying.algorithms.QueryTreeBuilder;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
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
    private static final List<Kind> kinds = defineKinds(List.of(datasources.postgreSQL()));

    @Test
    void test() {
        final Query query = QueryParser.parse(queryString);
        final QueryNode queryTree = QueryTreeBuilder.run(query.context, datasources.schema, kinds, query.where);
        final var output = QueryResolver.run(query.context, queryTree);

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

    private static List<Kind> defineKinds(List<TestDatasource<?>> testDatasources) {
        return testDatasources.stream()
            .flatMap(testDatasource -> {
                final var builder = new Datasource.Builder();
                testDatasource.mappings.stream().forEach(builder::mapping);
                final var datasource = builder.build(testDatasource.type, testDatasource.wrapper, testDatasource.id);

                return datasource.kinds.stream();
            })
            .toList();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void resultToStringTest() {
        final var builder = new ResultList.TableBuilder();

        builder.addColumns(List.of("colum1", "column2", "column3"));
        builder.addRow(List.of("a1", "a2", "a3"));
        builder.addRow(List.of("b1", "b2", "b3"));
        builder.addRow(List.of("c1", "c2", "c3"));

        final var rootMap = new TreeMap<String, ResultNode>();
        rootMap.put("x", builder.build());
        final var root = new ResultMap(rootMap);

        final var rootArray = new ResultList(List.of(root));

        LOGGER.info("\n" + rootArray.toString());

        assertDoesNotThrow(() -> {
            LOGGER.info("\n" + mapper.writeValueAsString(rootArray));
        });
    }

}
