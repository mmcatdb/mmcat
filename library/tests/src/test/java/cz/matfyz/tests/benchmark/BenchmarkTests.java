package cz.matfyz.tests.benchmark;

import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.tests.example.benchmarkyelp.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.QueryEstimator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[] { path.toString() });
            process.waitFor();

            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
        var kindNames = datasources.mongoDB().wrapper.getPullWrapper().getKindNames("10", "0").data();

        assertEquals(3, kindNames.size());
        assertTrue(kindNames.contains("business"));
        assertTrue(kindNames.contains("user"));
        assertTrue(kindNames.contains("review"));

        // MongoDBPullWrapper.executeQuery("db.count?")
    }

    @Test
    void costEstimationBasic() {
        // TODO: add multiple datasources
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL()
            // datasources.mongoDB()
        );

        final var query = """
            SELECT {
                ?business
                    bid ?business_id ;
                    name ?name ;
                    orders ?review_count .

            }
            WHERE {
                ?business 1 ?business_id .
                ?business 2 ?name .
                ?business 6 ?review_count .

                FILTER(?review_count >= "100")
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

        final var error = plans1.get(0).cost() >= plans2.get(0).cost() ? null : "Filtering increases cost estimation";
        assertNull(error);
    }
}
