package cz.matfyz.tests.querying;

import cz.matfyz.tests.example.querying.Datasources;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ComplexQueryTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ComplexQueryTests.class);

    private static final Datasources datasources = new Datasources();

    @BeforeAll
    static void setup() {
        datasources.postgreSQLs().stream().forEach(datasource -> datasource.setup());
    }

    @Test
    void oneJoin() {
        final var base = new QueryTestBase(datasources.schema);
        for (final var datasource : datasources.postgreSQLs())
            base.addDatasource(datasource);

        base
            .query("""
                SELECT {
                    ?e e_id ?e_id ;
                        e_value ?e_value ;
                        f ?f .

                    ?f f_id ?f_id ;
                        f_value ?f_value .
                }
                WHERE {
                    ?e 50 ?e_id ;
                        51 ?e_value ;
                        52 ?f .
                    ?f 60 ?f_id ;
                        61 ?f_value .
                }
            """)
            .expected("""
                [ {
                    "e_id": "e_id:0",
                    "e_value": "e_value:0",
                    "f": {
                        "f_id": "f_id:0",
                        "f_value": "f_value:0"
                    }
                }, {
                    "e_id": "e_id:1",
                    "e_value": "e_value:1",
                    "f": {
                        "f_id": "f_id:1",
                        "f_value": "f_value:1"
                    }
                } ]
            """)
            .run();
    }

    @Test
    void twoJoins() {
        final var base = new QueryTestBase(datasources.schema);
        for (final var datasource : datasources.postgreSQLs())
            base.addDatasource(datasource);

        base
            .query("""
                SELECT {
                    ?d d_id ?d_id ;
                        d_value ?d_value ;
                        e ?e .

                    ?e e_id ?e_id ;
                        e_value ?e_value ;
                        f ?f .

                    ?f f_id ?f_id ;
                        f_value ?f_value .
                }
                WHERE {
                    ?d 40 ?d_id ;
                        41 ?d_value ;
                        42 ?e .
                    ?e 50 ?e_id ;
                        51 ?e_value ;
                        52 ?f .
                    ?f 60 ?f_id ;
                        61 ?f_value .
                }
            """)
            .expected("""
                [ {
                    "d_id": "d_id:0",
                    "d_value": "d_value:0",
                    "e": {
                        "e_id": "e_id:0",
                        "e_value": "e_value:0",
                        "f": {
                            "f_id": "f_id:0",
                            "f_value": "f_value:0"
                        }
                    }
                }, {
                    "d_id": "d_id:1",
                    "d_value": "d_value:1",
                    "e": {
                        "e_id": "e_id:1",
                        "e_value": "e_value:1",
                        "f": {
                            "f_id": "f_id:1",
                            "f_value": "f_value:1"
                        }
                    }
                } ]
            """)
            .run();
    }

    @Test
    void unholyAmountOfJoins() {
        final var base = new QueryTestBase(datasources.schema);
        for (final var datasource : datasources.postgreSQLs())
            base.addDatasource(datasource);

        base
            .query("""
                SELECT {
                    ?a a_id ?a_id ;
                        a_value ?a_value ;
                        b ?b .

                    ?b b_id ?b_id ;
                        b_value ?b_value ;
                        c ?c .

                    ?c c_id ?c_id ;
                        c_value ?c_value ;
                        d ?d .

                    ?d d_id ?d_id ;
                        d_value ?d_value ;
                        e ?e .

                    ?e e_id ?e_id ;
                        e_value ?e_value ;
                        f ?f .

                    ?f f_id ?f_id ;
                        f_value ?f_value .
                }
                WHERE {
                    ?a 10 ?a_id ;
                        11 ?a_value ;
                        12 ?b .
                    ?b 20 ?b_id ;
                        21 ?b_value ;
                        22 ?c .
                    ?c 30 ?c_id ;
                        31 ?c_value ;
                        32 ?d .
                    ?d 40 ?d_id ;
                        41 ?d_value ;
                        42 ?e .
                    ?e 50 ?e_id ;
                        51 ?e_value ;
                        52 ?f .
                    ?f 60 ?f_id ;
                        61 ?f_value .
                }
            """)
            .expected("""
                [ {
                    "a_id": "a_id:0",
                    "a_value": "a_value:0",
                    "b": {
                        "b_id": "b_id:0",
                        "b_value": "b_value:0",
                        "c": {
                            "c_id": "c_id:0",
                            "c_value": "c_value:0",
                            "d": {
                                "d_id": "d_id:0",
                                "d_value": "d_value:0",
                                "e": {
                                    "e_id": "e_id:0",
                                    "e_value": "e_value:0",
                                    "f": {
                                        "f_id": "f_id:0",
                                        "f_value": "f_value:0"
                                    }
                                }
                            }
                        }
                    }
                }, {
                    "a_id": "a_id:1",
                    "a_value": "a_value:1",
                    "b": {
                        "b_id": "b_id:1",
                        "b_value": "b_value:1",
                        "c": {
                            "c_id": "c_id:1",
                            "c_value": "c_value:1",
                            "d": {
                                "d_id": "d_id:1",
                                "d_value": "d_value:1",
                                "e": {
                                    "e_id": "e_id:1",
                                    "e_value": "e_value:1",
                                    "f": {
                                        "f_id": "f_id:1",
                                        "f_value": "f_value:1"
                                    }
                                }
                            }
                        }
                    }
                } ]
            """)
            .run();
    }

}
