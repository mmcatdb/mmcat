package cz.matfyz.tests.benchmark;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.querying.optimizer.QueryDebugPrinter;
import cz.matfyz.tests.example.benchmark.yelp.Datasources;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.QueryEstimator;
import cz.matfyz.tests.querying.QueryTestBase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YelpTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(YelpTests.class);

    private static final Datasources datasources = new Datasources();
    private static final CollectorCache cache = new CollectorCache();

    @Test
    void yelpIsLoaded() {
        final var kindNames = datasources.mongoDB().wrapper.getPullWrapper().getKindNames();

        assertTrue(kindNames.contains("business"));
        assertTrue(kindNames.contains("yelp_user"));
        assertTrue(kindNames.contains("review"));

        // MongoDBPullWrapper.executeQuery("db.count?")
    }

    @Test
    void costEstimationBasic() {
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL(),
            datasources.mongoDB()
        );

        final var query = """
            SELECT {
                ?review
                    rid ?review_id ;
                    bid ?business_id ;
                    useful ?useful .

            }
            WHERE {
                ?review 11 ?review_id .
                ?review 13 ?business_id .
                ?review 16 ?useful .

                FILTER(?useful >= "100")
            }
        """;

        final var plans1 = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            cache,
            false
        ).run();

        final var plans2 = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            cache,
            true
        ).run();

        LOGGER.info("Basic cost estimation - best plans: \n" +
            "unoptimized:\n" + QueryDebugPrinter.run(plans1.get(0).root) + "\n" + 
            "optimized:\n" + QueryDebugPrinter.run(plans2.get(0).root)
        );

        final var error = plans1.get(0).root.predictedCostData.network() >= plans2.get(0).root.predictedCostData.network()
            ? null : "Filtering increases cost estimation";
        assertNull(error);
    }

    @Test
    void costEstimationJoin() {
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL(),
            datasources.mongoDB()
        );

        // All users which reviewed a given business
        // (theoretically the filter should be the commented GROUPBY HAVING, but that is not implemented yet, and this seems to work anyways)
        final var query = """
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
        """;

        final var plans = new QueryEstimator(
            datasources,
            testDatasources,
            query,
            cache,
            true
        ).run();

        LOGGER.info("Join cost estimation - gathered Plans: \n" +
            String.join("\n", plans.stream().map(p -> QueryDebugPrinter.run(p.root)).toList())
        );

        final var error = plans.get(0).root instanceof DatasourceNode
            ? null : "PostgreSQL DatasourceNode expected as the best plan root";
        assertNull(error);

        // new QueryTestBase(datasources.schema)
        //     .addDatasource(datasources.postgreSQL())
        //     .query(query)
        //     .expected("""
        //         [ { "test": "" } ]
        //     """)
        //     .run();
    }

    @Test
    void cache() {
        // All users which reviewed a given business
        // (theoretically the filter should be the commented GROUPBY HAVING, but that is not implemented yet, and this seems to work anyways)
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

            /*final ListResult result = */queryToInstance.execute();

            // log how long it took and display the plan (hopefully seeing if dependent join was taken)
        }
    }

    @Test
    void join() {
        // All users which reviewed a given business
        // (theoretically the filter should be the commented GROUPBY HAVING, but that is not implemented yet, and this seems to work anyways)
        final var query = """
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
        """;

        new QueryTestBase(datasources.schema)
            // .addDatasource(datasources.postgreSQL())
            .addDatasource(datasources.mongoDB())
            .cache(cache)
            .query(query).expected("""
                [
                    {"name":"K","uid":"6_SpY41LIHZuIaiDs5FMKA"},{"name":"Rob","uid":"tCXElwhzekJEH6QJe3xs7Q"},{"name":"Kassi","uid":"WqfKtI-aGMmvbA9pPUxNQQ"},{"name":"Katherine","uid":"3-1va0IQfK-9tUMzfHWfTA"},{"name":"Linda","uid":"EouCKoDfzaVG0klEgdDvCQ"},{"name":"Lizzie","uid":"KQSRUu4Aapl0hG6eu2v8iw"},{"name":"Eric","uid":"X_DkwPTzdO_VWzUcbUXREg"},{"name":"Sandy","uid":"qUfRCH5NUyRDsJfM6jA5PQ"},{"name":"Sam","uid":"gNJNxucGoZ31nlH74EQpPg"},{"name":"Courtney","uid":"NMOxipsnXc6olWdHYzXiYA"},{"name":"Stephen","uid":"ZO3Hh2lSFWmiPjDj0Wad5w"},{"name":"Vanessa","uid":"PoVeK6WYt2r1e3y3y4FwqQ"},{"name":"Richard","uid":"_ChgV15rAkH1-FWB8Qd8PQ"},{"name":"Hsini","uid":"uk6po-UUCTk_NvKKgvsOwg"},{"name":"Danan","uid":"ouODopBKF3AqfCkuQEnrDg"},{"name":"Kristin","uid":"ojxS1v-8nUvEEx4DlsPQrA"},{"name":"Kate","uid":"0q2W3-ieBUJWD5TTLKi3Ug"},{"name":"D","uid":"z-yvbUGwFn8PAijEHdU_RA"},{"name":"Fawn","uid":"pzikC7tVCIcGSX9XkyKmDw"},{"name":"Jana","uid":"g3HFkZgloxLQCvoJ5Zs5gg"},{"name":"Lawrence","uid":"YqqSMPzBrZIng-Y0YJTvfw"},{"name":"Thuy","uid":"OmL2bjLvvRxg1brM5Pehgw"},{"name":"Peter","uid":"FMGO5L64t-jkE1P4YWIPkg"},{"name":"Elodie","uid":"yMlk3HBqiRi0-HIU2jtmIw"},{"name":"Donna","uid":"h-NFrVwhhCvxdle_3pBwiQ"},{"name":"Melissa","uid":"MaueOwM1-iPoOaA5F6a5xA"},{"name":"Jeff","uid":"0_FWb5d-EkJVaP0GAr0gcQ"},{"name":"Lauren","uid":"4H0chGckIoOzGv2DcK2KSw"},{"name":"Danan","uid":"ouODopBKF3AqfCkuQEnrDg"},{"name":"kathy","uid":"Q5I2xTcaQ22bmE_mp2q_Rw"},{"name":"Helen","uid":"Pt8wZkFQNQfm2IlWJ9Ipng"},{"name":"Melissa","uid":"MaueOwM1-iPoOaA5F6a5xA"},{"name":"lynn","uid":"nmW4jna8vbE50F9SgjmgPQ"},{"name":"Andrea m","uid":"x22llLkqJas2J9VTJVNk3A"},{"name":"Charlene","uid":"nDFRVVcNLLUt-F_s0yBIPA"},{"name":"Julia","uid":"2Gp0gQNpIVmShIt3-gOebw"},{"name":"DOPETASTIC","uid":"oDuMcQ73TF60-TuWDu_GUQ"},{"name":"Donald","uid":"1vB6Olfkba7Sb-Y6TfiB0Q"},{"name":"Kyoto","uid":"ahY-mV1HFDIhK_MI5tmPVw"},{"name":"Jeff","uid":"0_FWb5d-EkJVaP0GAr0gcQ"},{"name":"Gwendolyn","uid":"Te3BwftEFN8C9WxImhfM_g"},{"name":"Patty","uid":"1DjkPbctTZ4SV_MS3TaeTQ"},{"name":"头脑","uid":"OyjJWNmlky-Ase9ov1Pq5Q"},{"name":"Lilli","uid":"X7cade4By6ENDk54g7XRtg"},{"name":"Susan","uid":"cUk4n3GMShdIr5pLyt9PFQ"},{"name":"Virginia","uid":"ntJ0mR5D4JqJOzsYZV-cxw"},{"name":"Nana","uid":"qVVPhYDSHsEfXQzklxfRKw"},{"name":"Zoya","uid":"GTATEXLSbDhqm-k5-E48Zw"},{"name":"Eugene","uid":"oKNSXiAy4_rOaxNejTVF7w"},{"name":"Hank","uid":"nnwBdqGHIAJQ5QX9lHOtrQ"},{"name":"Hoang","uid":"Wk_xohYcysEWnkhOp3sBSA"},{"name":"Dung","uid":"u73tXwSsYPF04WiHP6pTng"},{"name":"K","uid":"gMasL8Bv1S75vh0AqltSxQ"},{"name":"Laura","uid":"rOnsnJGYDvv36WF20qu0AQ"},{"name":"Katherine","uid":"pljgkZSB60BmtbkM6PvsGA"},{"name":"Amanda","uid":"19cvBxYcO52xV5x6EKTa_A"},{"name":"Melissa","uid":"MaueOwM1-iPoOaA5F6a5xA"},{"name":"Susan","uid":"mq--YNqsMPLpuK8fqaO8IQ"},{"name":"Cole","uid":"pocYAxpIEGSCQxd37gNQ0w"},{"name":"Weena","uid":"RwseYM2pXCTLwPw0g5nzNQ"},{"name":"Eva","uid":"6HMepXAIwsetCZKNvN2fdA"},{"name":"Crumb","uid":"ST97w3WYND_9uxcsexQuNA"},{"name":"Thuy","uid":"OmL2bjLvvRxg1brM5Pehgw"},{"name":"Lisa","uid":"-ZskOdnpNCWzTjeoBQM1LA"},{"name":"Lily","uid":"fyugYI0E5B9rLxgA0Cn7ZQ"},{"name":"Catherine","uid":"BSqC_tlyCCg_ngR-YqPyQA"},{"name":"Andrea","uid":"gBpxjo01cpY_5stvzru46Q"},{"name":"Mabel","uid":"JsAC6cQ2FSad_1ElPj1wxw"},{"name":"Angeline","uid":"ntqURA0yzrFadofEZNwIyw"},{"name":"Hillary","uid":"cBFsaSWURU_rOw1zvxUsdw"},{"name":"Linda","uid":"EouCKoDfzaVG0klEgdDvCQ"},{"name":"April","uid":"UHyquwvf_mI98eNsbIZbng"},{"name":"Jenny","uid":"Qoji0BPWUFgPfwGK9du8AA"},{"name":"Annie","uid":"IsMv1_7hd438DmGZmfhwZQ"},{"name":"KhanhMy","uid":"itcyGJsassJ0iutr0gBMjg"},{"name":"Vivienne","uid":"v3jR1g-mGJVTTq7gurRN3A"},{"name":"Gene","uid":"AxUyVJ_7ymHqhirZyBuXeA"},{"name":"Akosua","uid":"gLhYsnsgr9vKoDo78hXTeQ"},{"name":"Eileen","uid":"-6GY04bTPM2Zo4z0GN4a1A"},{"name":"Harlem","uid":"GRrtXgGH00p2KBwqmRMaTg"},{"name":"Ashley","uid":"WqeE5e5ROfaVEgkb9dAkiQ"},{"name":"Ashley","uid":"WqeE5e5ROfaVEgkb9dAkiQ"},{"name":"Ronnie","uid":"A7plO8trcZ3VsyDnw3LLcA"},{"name":"Mary","uid":"azhYuAZnnVpKRvOEc-Vc-w"},{"name":"Elisa","uid":"6kJFLAHV-tNsBEZaRTqEWQ"},{"name":"Patrice","uid":"UuVWbpQu76pJOFc1SQNk6A"},{"name":"Jaime","uid":"5CfRj0dIV1EPlarQ8oeh4w"}]
            """)
            .run();
    }
}
