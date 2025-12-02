package cz.matfyz.server;

import java.util.Date;

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

    @PostMapping("/runTests")
    public String runTests() {
        final var resultsAndFile = CalDotComTests.systemTest();
        final var results = resultsAndFile.results();
        final var filename = resultsAndFile.filename();

        long agg = 0;
        for (final var row : results) {
            agg += row.executionMs();
        }
        agg /= results.size();

        return "Ran tests with average " + agg + " ms / query. Detailed results are in " + filename + ".";
    }

}
