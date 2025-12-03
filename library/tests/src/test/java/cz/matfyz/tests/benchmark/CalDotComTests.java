package cz.matfyz.tests.benchmark;

import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.tests.example.benchmark.caldotcom.Datasources;
import cz.matfyz.tests.example.benchmark.caldotcom.ValueGenerator;
import cz.matfyz.tests.querying.FilterQueryFiller;
import cz.matfyz.tests.querying.QueryTestBase;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CalDotComTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CalDotComTests.class);

    private static final Datasources datasources = new Datasources();
    private static final CollectorCache cache = new CollectorCache();

    // An option to run the test from the test menu instead of the server API
    @Test
    void test() {
        cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests.systemTest();
    }

    // A helper test to see errors
    @Test
    void queryValidityTest() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(
            datasources.postgreSQL()
        )));

        // // Joining Impossible
        // final String query = """
        //     SELECT {
        //         ?role id ?id ;
        //             name ?name ;
        //             teamId ?teamId ;
        //             temName ?teamName
        //             userIds ?userId .
        //     }
        //     WHERE {
        //         ?role 11 ?id ;
        //             12 ?name ;
        //             14 ?team ;
        //             -56/54/41 ?userId .

        //         ?team 1 ?teamId ;
        //             2 ?teamName .

        //         FILTER(?teamId = "#1")
        //     }
        //     """;

        final String query = """
            SELECT {
                ?user userId ?uId ;
                    orgFeatureNames ?featureNames ;
                    orgFeatureIds ?featureIds .
            }
            WHERE {
                ?user 41 ?uId ;
                    -61/62/-181/182/162 ?featureNames ;
                    -61/62/-181/182/161 ?featureIds .

                FILTER(?uId = "#41")
            }
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();
        System.out.println(filled);

        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .cache(cache)
            .query(filled)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

}
