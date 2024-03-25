package cz.matfyz.tests.querying;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.querying.queryresult.ResultMap;
import cz.matfyz.core.querying.queryresult.ResultNode;
import cz.matfyz.querying.algorithms.QueryResolver;
import cz.matfyz.querying.algorithms.QueryTreeBuilder;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
import cz.matfyz.tests.example.basic.Databases;
import cz.matfyz.tests.example.common.TestDatabase;

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

    private static final Databases databases = new Databases();
    private static final List<Kind> kinds = defineKinds(List.of(databases.postgreSQL()));

    @Test
    void test() {
        final Query query = QueryParser.parse(queryString);
        final QueryNode queryTree = QueryTreeBuilder.run(query.context, databases.schema, kinds, query.where);
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

    private static List<Kind> defineKinds(List<TestDatabase<?>> testDatabases) {
        return testDatabases.stream()
            .flatMap(testDatabase -> {
                final var builder = new Database.Builder();
                testDatabase.mappings.stream().forEach(builder::mapping);
                final var database = builder.build(testDatabase.type, testDatabase.wrapper, testDatabase.id);

                return database.kinds.stream();
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
