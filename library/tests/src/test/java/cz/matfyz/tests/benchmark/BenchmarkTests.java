package cz.matfyz.tests.benchmark;

import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.optimizer.QueryDebugPrinter;
import cz.matfyz.tests.example.benchmarkyelp.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.QueryEstimator;
import cz.matfyz.tests.querying.QueryTestBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CompactNumberFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BenchmarkTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkTests.class);

    private static final Datasources datasources = new Datasources();

    @BeforeAll
    static void setup() {
        // datasources.mongoDB().setup();
        // setupMongoDB();
    }

    static void setupMongoDB() {
        // I just need to run it in bash rather than mongosh
        Path path = null;
        try {
            final var url = ClassLoader.getSystemResource("setupBenchmarkYelp.sh");
            path = Paths.get(url.toURI()).toAbsolutePath();

            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(new String[] { path.toString() });
            process.waitFor();

            final BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));
        }
        catch (URISyntaxException e) {
            LOGGER.error("Datasource setup error: ", e);
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            throw new ExecuteException(e, path);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    @Test
    void yelpIsLoaded() {
        final var kindNames = datasources.mongoDB().wrapper.getPullWrapper().getKindNames();

        assertEquals(3, kindNames.size());
        assertTrue(kindNames.contains("business"));
        assertTrue(kindNames.contains("user"));
        assertTrue(kindNames.contains("review"));

        // MongoDBPullWrapper.executeQuery("db.count?")
    }

    @Test
    void costEstimationBasic() {
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL(),
            datasources.mongoDB()
        );

        final var query = """
            SELECT {
                ?business
                    bid ?business_id ;
                    name ?name ;
                    reviews ?reviews .

            }
            WHERE {
                ?business 1 ?business_id .
                ?business 2 ?name .
                ?business 6 ?reviews .

                FILTER(?reviews >= "100")
            }
        """;

        final var plans1 = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            false
        ).run();

        final var plans2 = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            true
        ).run();

        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        final var cost1h = QueryDebugPrinter.run(plans1.get(0).root);
        final var cost2h = QueryDebugPrinter.run(plans2.get(0).root);

        final var error = plans1.get(0).root.costData.network() >= plans2.get(0).root.costData.network() ? null : "Filtering increases cost estimation";

        assertNull(error);
    }

    @Test
    void costEstimationJoin() {
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL()
            // datasources.mongoDB()
        );

        // All users which reviewed a given business
        final var query = """
            SELECT {
                ?user
                    uid ?user_id ;
                    name ?name .
            }
            WHERE {
                ?user  9 ?user_id .
                ?user 10 ?name .
                ?user -18/19 ?bid .

                FILTER(?bid = "MTSW4McQd7CbVtyjqoe9mw")
            }
        """;

        final var plans = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            true
        ).run();

        final var cost1h = QueryDebugPrinter.run(plans.get(0).root);
        final var cost2h = QueryDebugPrinter.run(plans.get(1).root);
        final var error = plans.get(0).root instanceof DatasourceNode ? null : "PostgreSQL DatasourceNode expected as the best plan root";
        assertNull(error);

        // new QueryTestBase(datasources.schema)
        //     .addDatasource(datasources.postgreSQL())
        //     .query(query)
        //     .expected("""
        //         [ {
        //             "number": "o_100"
        //         } ]
        //     """)
        //     .run();
    }
}
