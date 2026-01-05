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

    @Test
    void test() {
        final var datasrcs = cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests.datasources;

        cz.matfyz.tests.example.benchmark.caldotcom.CalDotComTests.systemTest(List.of(
            datasrcs.postgreSQL(),
            datasrcs.mongoDB(),
            datasrcs.neo4j()
        ), "all");
    }

    // A helper test to see errors
    @Test
    void singleQueryTest() {
        final var queryFiller = new FilterQueryFiller(new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final String query = """
SELECT {
    ?et title ?etTitle ;
        hostGroups ?hgId ;
        hosts ?hId .
}
WHERE {
    ?et 101 ?etId ;
        102 ?etTitle ;
        -132/131 ?hgId ;
        -144/141 ?hId .

    FILTER(?etId = "&101")
}
        """;

        final var filled = queryFiller.fillQuery(query).generateQuery();
        System.out.println(filled);

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
    void errorMassiveDuplicationInPlanDrafter() {
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

    @Test
    void errorNeo4jEvaluationGetsStuckOrSlow() {
        final String query = """
SELECT {
    ?user username ?handle ;
          oooStarts ?oooStart ;
          scheduleNames ?schedName .
}
WHERE {
    # Match User by ID
    ?user 41 ?userId ;
          42 ?handle .

    # Path: User <- OutOfOffice -> Start
    ?user -124/122 ?oooStart .

    # Path: User <- Schedule -> Name
    ?user -93/92 ?schedName .

    FILTER(?userId = "179")
}
        """;


        /*
MATCH (`VAR_CDCUser`:`CDCUser`)
MATCH (`VARFROM_CDC_OUT_OF_OFFICE`)-[`VAR_CDC_OUT_OF_OFFICE`:`CDC_OUT_OF_OFFICE`]->(`VARTO_CDC_OUT_OF_OFFICE`)
MATCH (`VAR_CDCSchedule`:`CDCSchedule`)
MATCH (`VARFROM_CDC_USER_SCHEDULE`)-[`VAR_CDC_USER_SCHEDULE`:`CDC_USER_SCHEDULE`]->(`VARTO_CDC_USER_SCHEDULE`)
MATCH (`VAR_CDCUser`)-[`VAR_CDC_OUT_OF_OFFICE`]->()
MATCH (`VAR_CDCUser`)-[`VAR_CDC_USER_SCHEDULE`]->()
MATCH (`VAR_CDCUser`)-[`VAR_CDC_OUT_OF_OFFICE`]->()
MATCH (`VAR_CDCUser`)-[`VAR_CDC_USER_SCHEDULE`]->()
MATCH ()-[`VAR_CDC_OUT_OF_OFFICE`]-()-[`VAR_CDC_USER_SCHEDULE`]-()
WHERE
  `VARFROM_CDC_USER_SCHEDULE`.`id` = '181'
WITH
  `VAR_CDCSchedule`.`name` AS `schedName`,
  `VAR_CDCSchedule`.`id` AS `#var2`,
  `VAR_CDCUser`.`username` AS `handle`,
  `VAR_CDCUser`.`id` AS `userId`,
  `VAR_CDC_OUT_OF_OFFICE`.`start` AS `oooStart`,
  `VAR_CDC_OUT_OF_OFFICE`.`id` AS `#var3`
RETURN
  `oooStart`,
  `#var2`,
  `handle`,
  `schedName`,
  `#var3`,
  {`userId`: `userId`} AS `user`,
  `userId`


MATCH (sched:`CDCSchedule`)<-[usrSched:`CDC_USER_SCHEDULE`]-(usr:`CDCUser`)-[ooo:`CDC_OUT_OF_OFFICE`]->()
WHERE
  usr.`id` = '181'
WITH
  sched.`name` AS `schedName`,
  sched.`id` AS `#var2`,
  usr.`username` AS `handle`,
  usr.`id` AS `userId`,
  ooo.`start` AS `oooStart`,
  ooo.`id` AS `#var3`
RETURN
  `oooStart`,
  `#var2`,
  `handle`,
  `schedName`,
  `#var3`,
  {`userId`: `userId`} AS `user`,
  `userId`
        */

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            // .addDatasource(datasources.mongoDB())
            .addDatasource(datasources.neo4j())
            .cache(cache)
            .query(query)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

}
