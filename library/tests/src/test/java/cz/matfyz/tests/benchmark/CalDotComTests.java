package cz.matfyz.tests.benchmark;

import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.tests.example.benchmark.caldotcom.Datasources;
import cz.matfyz.tests.querying.FilterQueryFiller;
import cz.matfyz.tests.querying.FilterValueGenerator;
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

    @Test
    void test() {
        final var queryFiller = new FilterQueryFiller(new FilterValueGenerator(datasources.schema, List.of(
            datasources.postgreSQL() /*,
            datasources.mongoDB(),
            datasources.neo4j()
            */
        )));

        // final String query = """
        //     SELECT {
        //         ?team id ?id ;
        //             name ?name .
        //     }
        //     WHERE {
        //         ?team 1 ?id ;
        //             2 ?name .

        //         FILTER(?id = ??)
        //     }
        //     """;

        // final String query = """
        //     SELECT {
        //         ?role id ?id ;
        //             name ?name ;
        //             team ?team ;
        //             userIds ?userId .

        //         ?team id ?teamId ;
        //             name ?teamName .
        //     }
        //     WHERE {
        //         ?role 11 ?id ;
        //             12 ?name ;
        //             14 ?team ;
        //             -56/54/41 ?userId .

        //         ?team 1 ?teamId ;
        //             2 ?teamName .

        //         FILTER(?teamId = ??)
        //     }
        //     """;

        final String query = """
                SELECT {
                    ?team id ?id ;
                        name ?name .
                }
                WHERE {
                    ?team 1 ?id ;
                        2 ?name .
                }
            """;
            /*
            [ {
                "id": "51",
                "name": "Admin",
                "team": {
                    "id": "2",
                    "name": "MMCat Devs",
                },
                "userIds": [ "125", "126" ],
            } ]
            */

        final var filled = queryFiller.fillQuery(query);
        System.out.println(filled);

        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            // .addDatasource(datasources.mongoDB())
            // .addDatasource(datasources.neo4j())
            // .cache(cache)
            .query(filled)
            .expected("""
                [ {
                    "number": "o_100"
                } ]
            """)
            .run();
    }

    /*

    @Test
    void cache() {
        final var queries = List.of(
            """
                SELECT {
                ?business
                    bid ?bid ;
                    name ?name .
                }
                WHERE {
                    ?business 1 ?bid .
                    ?business 2 ?name .
                }
            """,
            """
                SELECT {
                ?user
                    uid ?uid ;
                    name ?name .
                }
                WHERE {
                    ?user 6 ?uid .
                    ?user 7 ?name .
                }
            """,
            """
                SELECT {
                ?review
                    rid ?rid ;
                    uid ?uid ;
                    bid ?bid ;
                    stars ?stars .
                }
                WHERE {
                    ?review 11 ?rid .
                    ?review 12/6 ?uid .
                    ?review 13/1 ?bid .
                    ?review 14 ?stars .
                }
            """,
            """
                SELECT {
                ?review
                    rid ?rid ;
                    uid ?uid ;
                    bid ?bid ;
                    stars ?stars .
                }
                WHERE {
                    ?review 11 ?rid .
                    ?review 12/6 ?uid .
                    ?review 13/1 ?bid .
                    ?review 14 ?stars .

                    FILTER(?bid = "123")
                }
            """,
            """
                SELECT {
                ?review
                    rid ?rid ;
                    uid ?uid ;
                    bid ?bid ;
                    stars ?stars .
                }
                WHERE {
                    ?review 11 ?rid .
                    ?review 12/6 ?uid .
                    ?review 13/1 ?bid .
                    ?review 14 ?stars .

                    FILTER(?uid = "12345")
                }
            """,
            """
                SELECT {
                ?user
                    uid ?user_id ;
                    name ?name .
                }
                WHERE {
                    ?user 6 ?user_id .
                    ?user 7 ?name .
                    ?user -12/13/1 ?business_id .

                    FILTER(?business_id = "MTSW4McQd7CbVtyjqoe9mw")
                    # GROUP BY ?user
                    # HAVING(?business_id = "MTSW4McQd7CbVtyjqoe9mw")
                }
            """
        );

        for (final var query : queries) {

            LOGGER.info("Querying : " + query);

            final var usedDatasources = List.of(datasources.mongoDB());
            final var provider = new DefaultControlWrapperProvider();
            final var kinds = usedDatasources.stream()
                .flatMap(testDatasource -> {
                    provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                    return testDatasource.mappings.stream();
                }).toList();

            final var queryToInstance = new QueryToInstance(provider, datasources.schema, query, kinds, cache);

            // final ListResult result = queryToInstance.execute();
            queryToInstance.execute();

            // log how long it took and display the plan (hopefully seeing if dependent join was taken)
        }
    }

    */
}
