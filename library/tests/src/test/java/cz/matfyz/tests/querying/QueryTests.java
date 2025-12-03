package cz.matfyz.tests.querying;

import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueryTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);

    private static final Datasources datasources = new Datasources();

    @BeforeAll
    static void setup() {
        datasources.postgreSQL().setup();
        datasources.mongoDB().setup();
        datasources.neo4j().setup();
    }

    static final QueryTestBase commonBasic = new QueryTestBase(datasources.schema)
        .query("""
            SELECT {
                ?order number ?number .
            }
            WHERE {
                ?order 1 ?number .
            }
        """)
        .expected("""
            [ {
                "number": "o_100"
            }, {
                "number": "o_200"
            } ]
        """);

    static final QueryCustomTreeTest commonFilter = new QueryCustomTreeTest(datasources.schema)
        .query("""
            SELECT {
                ?order number ?number .
            }
            WHERE {
                ?order 1 ?number .

                FILTER(?number = "o_100")
            }
        """)
        .queryTreeBuilder((plans, scope) -> {
            final var plan = plans.get(0);
            final var pattern = plan.stream().findFirst().get();

            return new DatasourceNode(
                pattern.kind.datasource(),
                plan,
                List.of(),
                List.of(
                    scope.computation.create(Operator.Equal, pattern.root.children().stream().findFirst().get().variable, new Constant("o_100"))
                ),
                pattern.root.variable
            );
        })
        .expected("""
            [ {
                "number": "o_100"
            } ]
        """);

    static final QueryTestBase commonJoin = new QueryTestBase(datasources.schema)
        .query("""
            SELECT {
                ?item
                    product ?label ;
                    quantity ?quantity .
            }
            WHERE {
                ?item 52/55 ?label .
                ?item 53 ?quantity .
            }
        """)
        .expected("""
            [ {
                "product": "Animal Farm",
                "quantity":"3"
            }, {
                "product": "Clean Code",
                "quantity":"1"
            }, {
                "product": "The Art of War",
                "quantity":"7"
            }, {
                "product": "The Lord of the Rings",
                "quantity":"2"
            } ]
        """);

    @Test
    void postgreSQLBasic() {
        commonBasic
            .copy()
            .addDatasource(datasources.postgreSQL())
            .run();
    }

    @Test
    void postgreSQLFilter() {
        commonFilter
            .copy()
            .addDatasource(datasources.postgreSQL())
            .run();
    }

    @Test
    void postgreSQLJoin() {
        commonJoin
            .copy()
            .addDatasource(datasources.postgreSQL())
            .run();
    }

    @Test
    void postgreSQLNested() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?orderItem
                        quantity ?quantity ;
                        product ?product .

                    ?product
                        id ?id ;
                        label ?label .

                }
                WHERE {
                    ?orderItem 53 ?quantity .
                    ?orderItem 52 ?product .
                    ?product 54 ?id .
                    ?product 55 ?label .
                }
            """)
            .expected("""
                [ {
                    "TBA": "TBA"
                } ]
            """)
            .run();
    }

    @Test
    void postgreSQLNestedWithArray() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?product
                        id ?id ;
                        label ?label ;
                        orders ?orders .

                    ?orders id ?orderId .

                }
                WHERE {
                    ?product 54 ?id .
                    ?product 55 ?label .
                    ?product -52 ?orders .
                    ?orders 51/1 ?orderId .
                }
            """)
            .expected("""
                [ {
                    "id": "p_123",
                    "label": "Clean Code",
                    "orders": [ { "id": "o_100" } ]
                },
                {
                    "id": "p_765",
                    "label": "The Lord of the Rings",
                    "orders": [ { "id": "o_100" } ]
                },
                {
                    "id": "p_457",
                    "label": "The Art of War",
                    "orders": [ { "id": "o_200" } ]
                },
                {
                    "id": "p_734",
                    "label": "Animal Farm",
                    "orders": [ { "id": "o_200" } ]
                } ]
            """)
            .run();
    }


    @Test
    void mongoDBBasic() {
        commonBasic
            .copy()
            .addDatasource(datasources.mongoDB())
            .run();
    }

    @Test
    void mongoDBFilter() {
        commonFilter
            .copy()
            .addDatasource(datasources.mongoDB())
            .run();
    }

    @Test
    void mongoDBNested() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order street ?street ;
                        city ?city .
                    ?order address ?address .
                    ?address street ?street ;
                        city ?city .
                }
                WHERE {
                    ?order 41 ?address .
                    ?address 42 ?street ;
                        43 ?city .
                }
            """)
            .expected("""
                [ {
                    "street": "Ke Karlovu 2027/3",
                    "city": "Praha 2",
                    "address": {
                        "street": "Ke Karlovu 2027/3",
                        "city": "Praha 2"
                    }
                }, {
                    "street": "Malostranské nám. 2/25",
                    "city": "Praha 1",
                    "address": {
                        "street": "Malostranské nám. 2/25",
                        "city": "Praha 1"
                    }
                } ]
            """)
            .run();
    }

    /**
     * Contrary to the previous test, the nesting path contains an array. This enforces us to use the $map operator in MongoDB.
     */
    @Test
    void mongoDBNestedWithArray() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order items ?item .
                    ?item id ?id ;
                        label ?label .
                }
                WHERE {
                    ?order -51 ?item .
                    ?item 52/54 ?id ;
                        52/55 ?label .
                }
            """)
            .expected("""
                [ {
                    "items": [
                        { "id": "p_123", "label": "Clean Code" }
                    ]
                }, {
                    "items": [
                        { "id": "p_765", "label": "The Lord of the Rings" }
                    ]
                }, {
                    "items": [
                        { "id": "p_457", "label": "The Art of War" },
                        { "id": "p_734", "label": "Animal Farm" }
                    ]
                } ]
            """)
            .run();
    }

    // TODO add double nested object array.


    @Test
    void neo4jBasic() {
        commonBasic
            .copy()
            .addDatasource(datasources.neo4j())
            .run();
    }

    @Test
    void neo4jFilter() {
        commonFilter
            .copy()
            .addDatasource(datasources.neo4j())
            .run();
    }

    @Test
    void neo4jJoin() {
        commonJoin
            .copy()
            .addDatasource(datasources.neo4j())
            .run();
    }

    @Test
    void neo4jNodeAndRelationshipShareObjex() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.neo4j())
            .query("""
                SELECT {
                    ?contact
                        type ?type ;
                        value ?value ;
                        order ?orderNumber .
                }
                WHERE {
                    ?contact 63 ?type .
                    ?contact 62 ?value .
                    ?contact 61/1 ?orderNumber .
                }
            """)
            .expected("""
                [ {
                    "order":"o_100",
                    "type":"phone",
                    "value":"123456789"
                }, {
                    "order":"o_100",
                    "type":"email",
                    "value":"alice@mmcatdb.com"
                }, {
                    "order":"o_200",
                    "type":"email",
                    "value":"bob@mmcactdb.com"
                }, {
                    "order":"o_200",
                    "type":"github",
                    "value":"https://github.com/mmcactdb"
                } ]
            """)
            .run();
    }

    /**
     * The arrays are flattened (if it's possible) exept for the top-level array. I.e., if there are multiple orders in the database, we are still going to get an array of orders. However, the properties of the orders are going to be flattened.
     * In this case, the flattening happens in the database itself.
     */
    @Test
    void flattenArrayInSelection() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order id ?id .
                }
                WHERE {
                    ?order -51/52/54 ?id .
                }
            """)
            .expected("""
                [ {
                    "id": [ "p_123" ]
                }, {
                    "id": [ "p_765" ]
                }, {
                    "id": [ "p_457", "p_734" ]
                } ]
            """)
            .run();
    }

    /**
     * The database returns the data in the full form. The flattening then happens in the projection.
     */
    @Test
    void flattenArrayInProjection() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order id ?id ;
                        label ?label .
                }
                WHERE {
                    ?order -51 ?item .
                    ?item 52/54 ?id ;
                        52/55 ?label .
                }
            """)
            .expected("""
                [ {
                    "id": [ "p_123" ],
                    "label": [ "Clean Code" ]
                }, {
                    "id": [ "p_765" ],
                    "label": [ "The Lord of the Rings" ]
                }, {
                    "id": [ "p_457", "p_734" ],
                    "label": [ "The Art of War", "Animal Farm" ]
                } ]
            """)
            .run();
    }

    @Test
    void alias() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?o has_number ?n .
                }
                WHERE {
                    ?o 1 ?n .
                }
            """)
            .expected("""
                [ {
                    "has_number": "o_100"
                }, {
                    "has_number": "o_200"
                } ]
            """)
            .run();
    }

    /**
     * This test fails because the planning algorithm isn't able to find patterns in the mapping graph. Instead it just tries first available path from the root and if none is available, it simply fails.
     */
    @Test
    void dualSignature() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?number -1 ?order .
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void multipleElements() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order tags ?tag .
                }
                WHERE {
                    ?order -2/4 ?tag .
                }
            """)
            .expected("""
                [ {
                    "tags": [ "t_123", "t_456", "t_789" ]
                }, {
                    "tags": [ "t_123", "t_555", "t_888" ]
                } ]
            """)
            .run();
    }

    @Test
    void filter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100")
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                } ]
            """)
            .run();
    }

    @Test
    void selfJoin() {
        // TODO: This needs an actual implementation in all underlying database systems to test it properly
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?customer friend ?customer2 .

                    ?customer2 name ?name ;
                        orders ?number .
                }
                WHERE {
                    ?customer -23/24 ?customer2 .

                    ?customer2 22 ?name ;
                        -21/1 ?number .
                }
            """)
            .expected("""
                [ {
                    "expected": "TBA"
                } ]
            """)
            .run();
    }

    @Test
    void stringEscaping() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number != "x\\\\x\\nx'x\\'\\"x")
                    FILTER(?number != 'x\\\\x\\nx"x\\'\\"x')
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void multipleFilters() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number != "o_200")
                    FILTER(?number != "o_300")
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                } ]
            """)
            .run();
    }

    @Test
    void tautologyFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = ?number)
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void contradictionFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number != ?number)
                }
            """)
            .expected("""
                []
            """)
            .run();
    }

    @Test
    void constantTautologyFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER("1" = "1")
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void constantContradictionFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER("1" = "2")
                }
            """)
            .expected("""
                []
            """)
            .run();
    }

    @Test
    void computationFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER("true" != (?number = "o_100"))
                }
            """)
            .expected("""
                [ {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void orFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100" || ?number = "o_200")
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void andFilter() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100" && ?number != "o_300")
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                } ]
            """)
            .run();
    }

    @Test
    void multipleProperties() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?product id ?id ;
                        label ?label ;
                        price ?price .
                }
                WHERE {
                    ?product 54 ?id ;
                        55 ?label ;
                        56 ?price .
                }
            """)
            .expected("""
                [ {
                    "id": "p_123",
                    "label": "Clean Code",
                    "price": "125"
                }, {
                    "id": "p_765",
                    "label": "The Lord of the Rings",
                    "price": "199"
                }, {
                    "id": "p_457",
                    "label": "The Art of War",
                    "price": "299"
                }, {
                    "id": "p_734",
                    "label": "Animal Farm",
                    "price": "350"
                } ]
            """)
            .run();
    }

    @Test
    void multipleCompositeProperties() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order items ?item .
                    ?item quantity ?quantity ;
                        id ?id ;
                        label ?label ;
                        price ?price .
                }
                WHERE {
                    ?order -51 ?item .
                    ?item 53 ?quantity ;
                        52/54 ?id ;
                        52/55 ?label ;
                        52/56 ?price .
                }
            """)
            .expected("""
                [ {
                    "items": [
                        { "quantity": "1", "id": "p_123", "label": "Clean Code", "price": "125" }
                    ]
                }, {
                    "items": [
                        { "quantity": "2", "id": "p_765", "label": "The Lord of the Rings", "price": "199" }
                    ]
                }, {
                    "items": [
                        { "quantity": "7", "id": "p_457", "label": "The Art of War", "price": "299" },
                        { "quantity": "3", "id": "p_734", "label": "Animal Farm", "price": "350" }
                    ]
                } ]
            """)
            .run();
    }

    @Test
    void differentProjectionRoot() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?item quantity ?quantity ;
                        id ?id ;
                }
                WHERE {
                    ?order -51 ?item .
                    ?item 53 ?quantity ;
                        52/54 ?id ;
                }
            """)
            .expected("""
                [
                    { "quantity": "1", "id": "p_123" },
                    { "quantity": "2", "id": "p_765" },
                    { "quantity": "7", "id": "p_457" },
                    { "quantity": "3", "id": "p_734" }
                ]
            """)
            .run();
    }

    /**
     * The mapping "order_item" already contains all necessary information so the other mappings shouldn't be used.
     * This needs to be checked manually.
     */
    @Test
    void notNeededJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?item number ?number ;
                        id ?id .
                }
                WHERE {
                    ?item 51/1 ?number ;
                        52/54 ?id .
                }
            """)
            .expected("""
                [ {
                    "number": "o_100",
                    "id": "p_123"
                }, {
                    "number": "o_100",
                    "id": "p_765"
                }, {
                    "number": "o_200",
                    "id": "p_457"
                }, {
                    "number": "o_200",
                    "id": "p_123"
                }, {
                    "number": "o_200",
                    "id": "p_734"
                } ]
            """)
            .run();
    }

    @Test
    void oneDatabaseJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?item quantity ?quantity ;
                        label ?label .
                }
                WHERE {
                    ?item 53 ?quantity ;
                        52/55 ?label .
                }
            """)
            .expected("""
                [ {
                    "quantity": "1",
                    "label": "Clean Code"
                }, {
                    "quantity": "2",
                    "label": "The Lord of the Rings"
                }, {
                    "quantity": "9",
                    "label": "Clean Code"
                }, {
                    "quantity": "7",
                    "label": "The Art of War"
                }, {
                    "quantity": "3",
                    "label": "Animal Farm"
                } ]
            """)
            .run();
    }

    @Test
    void multipleDatabasesJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .addDatasource(
                datasources.createNewMongoDB()
                    .addMapping(MongoDB.address(datasources.schema))
            )
            .query("""
                SELECT {
                    ?item quantity ?quantity ;
                        street ?street .
                }
                WHERE {
                    ?item 53 ?quantity ;
                        51/41/42 ?street .
                }
            """)
            .expected("""
                [ {
                    "quantity": "1",
                    "street": "Ke Karlovu 2027/3"
                }, {
                    "quantity": "2",
                    "street": "Ke Karlovu 2027/3"
                }, {
                    "quantity": "9",
                    "street": "Malostranské nám. 2/25"
                }, {
                    "quantity": "7",
                    "street": "Malostranské nám. 2/25"
                }, {
                    "quantity": "3",
                    "street": "Malostranské nám. 2/25"
                } ]
            """)
            .run();
    }

    @Test
    void mergeJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .addDatasource(
                datasources.createNewMongoDB()
                    .addMapping(MongoDB.tagSet(datasources.schema))
                    .addMapping(MongoDB.customer(datasources.schema))
            )
            .query("""
                SELECT {
                    ?order
                        name ?customerName ;
                        tags ?tag .

                }
                WHERE {
                    ?order
                        21/22 ?customerName ;
                        -2/4 ?tag .
                }
            """)
            .expected("""
                [ {
                    "name": "Alice",
                    "tags": [ "t_123", "t_456", "t_789" ]
                }, {
                    "name": "Bob",
                    "tags": [ "t_123", "t_555", "t_888" ]
                } ]
            """)
            .run();
    }

    /**
     * The variables ?order and ?product are not needed. However, the query result should be the same as if the user used composite morphisms instead.
     */
    @Test
    void unnecessaryVariables() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?item number ?number ;
                        id ?id .
                }
                WHERE {
                    ?item 51 ?order .
                    ?order 1 ?number .
                    ?item 52 ?product .
                    ?product 54 ?id .
                }
            """)
            .expected("""
                [ {
                    "number": "o_100",
                    "id": "p_123"
                }, {
                    "number": "o_100",
                    "id": "p_765"
                }, {
                    "number": "o_200",
                    "id": "p_123"
                }, {
                    "number": "o_200",
                    "id": "p_457"
                }, {
                    "number": "o_200",
                    "id": "p_734"
                } ]
            """)
            .run();
    }

    // TODO - something breaks when one kind is in multiple mappings. The planning algorithm can't deal with it. Don't know why. Test it.

    @Test
    void filterConcatenation() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = CONCAT("o", "_", "100"))
                    # FILTER(CONCAT(?number, "-xyz") = CONCAT(CONCAT("o", "_", "100"), "-xyz"))
                }
            """)
            .expected("""
                [ {
                    "number": "o_100"
                } ]
            """)
            .run();
    }
}
