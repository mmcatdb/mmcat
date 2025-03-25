package cz.matfyz.tests.benchmark;

import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.JoinNode.SerializedJoinNode;
import cz.matfyz.tests.example.benchmarkyelp.Datasources;
import cz.matfyz.tests.example.benchmarkyelp.MongoDB;
import cz.matfyz.wrappermongodb.MongoDBPullWrapper;

import java.util.List;

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
}
