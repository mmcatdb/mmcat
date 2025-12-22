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
    void testFunctional() {
        cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests.testFunctional();
    }

    // An option to run the test from the test menu instead of the server API
    @Test
    void test() {
        cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests.systemTest();
    }

    // A helper test to see errors
    @Test
    void queryValidityTest() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final String query = """
            SELECT {
                ?team name ?teamName ;
                    memberUsernames ?username ;
                    memberRoles ?roleId .
            }
            WHERE {
                ?team 1 ?teamId ;
                    2 ?teamName ;
                    -55/54/42 ?username ;   # Team <- Membership -> User -> Username
                    -55/56/11 ?roleId .   # Team <- Membership -> Role -> RoleId

                FILTER(?teamId = "&1")
            }
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();
        System.out.println(filled);

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            .addDatasource(datasources.mongoDB())
            // .addDatasource(datasources.neo4j())
            .cache(cache)
            .query(filled)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

    // A helper test to see errors
    @Test
    void errorInfiniteCycleInPlanDrafter() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(
            datasources.postgreSQL()
        )));

        final String query = """
            SELECT {
                ?booking title ?bTitle ;
                        attendeeEmails ?attEmails ;
                        hostAvailabilityStarts ?hostAvailStarts .
            }
            WHERE {
                ?booking 232 ?bTitle ;
                        -243/242 ?attEmails ;
                        234/-93/-116/112 ?hostAvailStarts .

                FILTER(?bTitle = "&232")
            }
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            // .addDatasource(datasources.mongoDB())
            .addDatasource(datasources.neo4j())
            .cache(cache)
            .query(filled)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

    // A helper test to see errors
    @Test
    void errorValueJoinUnsupported() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(
            datasources.postgreSQL()
        )));

        final String query = """
            SELECT {
                ?booking title ?bTitle ;
                        attendeeEmails ?attEmails ;
                        hostAvailabilityStarts ?hostAvailStarts .
            }
            WHERE {
                ?booking 232 ?bTitle ;
                        -243/242 ?attEmails ;
                        234/-93/-116/112 ?hostAvailStarts .

                FILTER(?bTitle = "&232")
            }
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            .addDatasource(datasources.mongoDB())
            // .addDatasource(datasources.neo4j())
            .cache(cache)
            .query(filled)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

    // A helper test to see errors
    @Test
    void errorNullDereferenceInResultStructureResolution() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final String query = """
            SELECT {
                ?feature name ?fName ;
                        enabledForUsers ?username ;
                        enabledForTeams ?teamName .
            }
            WHERE {
                # Match Feature by ID
                ?feature 161 ?fId ;
                        162 ?fName .

                # Path: Feature <- UserFeatures -> User -> Username
                ?feature -172/171/42 ?username .

                # Path: Feature <- TeamFeatures -> Team -> Name
                ?feature -182/181/2 ?teamName .

                FILTER(?fId = "&161")
            }
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();
        System.out.println(filled);

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            .addDatasource(datasources.mongoDB())
            // .addDatasource(datasources.neo4j())
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
