package cz.matfyz.tests.benchmark;

import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.tests.example.benchmarkyelp.Datasources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        setupMongoDB();
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
}
