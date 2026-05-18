package cz.matfyz.server;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.matfyz.querying.optimizer.QueryOptimizer;
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
        final var REPETITIONS = 10;

        for (int i = 0; i < 4; i++) {
            String optLevel = "?";
            if (i == 0) {
                optLevel = "base";
                QueryOptimizer.predicatePushdown =  false;
                QueryOptimizer.dependentJoins =     false;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 1) {
                optLevel = "predpushdown";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     false;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 2) {
                optLevel = "depjoins";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     true;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 3) {
                optLevel = "fastdrafting";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     true;
                QueryOptimizer.fastPlanDrafting =   true;
            }

            for (final var datasource : List.of(
                CalDotComTests.datasources.postgreSQL(),
                CalDotComTests.datasources.mongoDB(),
                CalDotComTests.datasources.neo4j()
            )) {
                CalDotComTests.systemTest(List.of(datasource), REPETITIONS, datasource.datasource().identifier + '-' + optLevel);
            }
        }

        return "Fininshed.";
    }

    @PostMapping("/runTestAllDatasources")
    public String runTestAllDatasources() {
        final var REPETITIONS = 10;

        for (int i = 1; i < 4; i++) {
            String optLevel = "?";
            if (i == 0) {
                optLevel = "base";
                QueryOptimizer.predicatePushdown =  false;
                QueryOptimizer.dependentJoins =     false;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 1) {
                optLevel = "predpushdown";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     false;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 2) {
                optLevel = "depjoins";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     true;
                QueryOptimizer.fastPlanDrafting =   false;
            } else if (i == 3) {
                optLevel = "fastdrafting";
                QueryOptimizer.predicatePushdown =  true;
                QueryOptimizer.dependentJoins =     true;
                QueryOptimizer.fastPlanDrafting =   true;
            }

            CalDotComTests.systemTest(List.of(
                CalDotComTests.datasources.mixPostgreSQL(),
                CalDotComTests.datasources.mixMongoDB(),
                CalDotComTests.datasources.mixNeo4j()
            ), REPETITIONS, "mix-" + optLevel);
        }

        return "Fininshed.";
    }

}
