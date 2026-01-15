package cz.matfyz.server;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests;

@RestController
public class DevController {

    @GetMapping("/")
    public String index() {
        return "Server is running.";
    }

    @GetMapping("/ping")
    public String ping() {
        return new Date().toString();
    }

    @PostMapping("/runTestSeparateDatasources")
    public String runTestSeparateDatasources() {
        String result = "";

        for (final var datasource : List.of(
            CalDotComTests.datasources.postgreSQL(),
            CalDotComTests.datasources.mongoDB(),
            CalDotComTests.datasources.neo4j()
        )) {
            final var resultsAndFile = CalDotComTests.systemTest(List.of(datasource), datasource.datasource().identifier);
            final var results = resultsAndFile.results();
            final var filename = resultsAndFile.filename();

            long agg = 0;
            for (final var row : results) {
                agg += row.innerSelectionTimeInMs() + row.underlyingDBMSSelectionTimeInMs();
            }
            agg /= results.size();


            result += datasource.datasource().identifier + ": Ran tests with average " + agg + " ms / query. Detailed results are in " + filename + ".\n";
        }

        return result;
    }

    @PostMapping("/runTestAllDatasources")
    public String runTestAllDatasources() {
        final var resultsAndFile = CalDotComTests.systemTest(List.of(
            CalDotComTests.datasources.postgreSQL(),
            CalDotComTests.datasources.mongoDB(),
            CalDotComTests.datasources.neo4j()
        ), "all");
        final var results = resultsAndFile.results();
        final var filename = resultsAndFile.filename();

        long agg = 0;
        for (final var row : results) {
            agg += row.innerSelectionTimeInMs() + row.underlyingDBMSSelectionTimeInMs();
        }
        agg /= results.size();

        return "Ran tests with average " + agg + " ms / query. Detailed results are in " + filename + ".";
    }

}
