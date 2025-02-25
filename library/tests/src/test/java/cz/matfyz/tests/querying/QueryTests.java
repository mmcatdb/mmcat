package cz.matfyz.tests.querying;

import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueryTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);

    private static final Datasources datasources = new Datasources();
    private static final ExpressionScope scope = new ExpressionScope();

    @BeforeAll
    static void setup() {
        datasources.postgreSQL().setup();
        datasources.mongoDB().setup();
    }

    @Test
    void basicPostgreSQL() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
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
            """)
            .run();
    }

    @Test
    void basicMongoDB() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
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
            """)
            .run();
    }

    @Test
    void basicNeo4J() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.neo4j())
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
            """)
            .run();
    }

    @Test
    void nestedMongoDB() {
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
                    ?order 8 ?address .
                    ?address 9 ?street ;
                        10 ?city .
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
    void nestedMongoDBWithArray() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.mongoDB())
            .query("""
                SELECT {
                    ?order items ?item .
                    ?item id ?id ;
                        label ?label .
                }
                WHERE {
                    ?order -12 ?item .
                    ?item 13/15 ?id ;
                        13/16 ?label .
                }
            """)
            .expected("""
                [ {
                    "items": [
                        { "id": "123", "label": "Clean Code" }
                    ]
                }, {
                    "items": [
                        { "id": "765", "label": "The Lord of the Rings" }
                    ]
                }, {
                    "items": [
                        { "id": "457", "label": "The Art of War" },
                        { "id": "734", "label": "Animal Farm" }
                    ]
                } ]
            """)
            .run();
    }

    // TODO add double nested object array.

    /**
     * The arrays are flatten (if it's possible) exept for the top-level array. I.e., if there are multiple orders in the database, we are still going to get an array of orders. However, the properties of the orders are going to be flatten.
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
                    ?order -12/13/15 ?id .
                }
            """)
            .expected("""
                [ {
                    "id": [ "123" ]
                }, {
                    "id": [ "765" ]
                }, {
                    "id": [ "457", "734" ]
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
                    ?order -12 ?item .
                    ?item 13/15 ?id ;
                        13/16 ?label .
                }
            """)
            .expected("""
                [ {
                    "id": [ "123" ],
                    "label": [ "Clean Code" ]
                }, {
                    "id": [ "765" ],
                    "label": [ "The Lord of the Rings" ]
                }, {
                    "id": [ "457", "734" ],
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
                    ?order -2 ?tag .
                }
            """)
            .expected("""
                [ {
                    "tags": [ "123", "456", "789" ]
                }, {
                    "tags": [ "123", "String456", "String789" ]
                } ]
            """)
            .run();
    }

    @Test
    void filterPostgreSQL() {
        new QueryCustomTreeTest<>(
            datasources,
            datasources.postgreSQL(),
            """
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100")
                }
            """,
            (schema, datasource, plan) -> {
                final var onlyPattern = plan.stream().findFirst().get();

                final var filters = new ArrayList<Computation>();
                filters.add(scope.computation.create(Operator.Equal, onlyPattern.root.children().stream().findFirst().get().variable, new Constant("o_100")));

                return new DatasourceNode(
                    datasource,
                    plan,
                    schema,
                    List.of(),
                    filters,
                    onlyPattern.root.variable
                );
            },
            """
                [ {
                    "number": "o_100"
                } ]
            """).run();
    }

    @Test
    void filterMongoDB() {
        new QueryCustomTreeTest<>(
            datasources,
            datasources.mongoDB(),
            """
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100")
                }
            """,
            (schema, datasource, plan) -> {
                final var onlyPattern = plan.stream().findFirst().get();

                final var filters = new ArrayList<Computation>();
                filters.add(scope.computation.create(Operator.Equal, onlyPattern.root.children().stream().findFirst().get().variable, new Constant("o_100")));

                return new DatasourceNode(
                    datasource,
                    plan,
                    schema,
                    List.of(),
                    filters,
                    onlyPattern.root.variable
                );
            },
            """
                [ {
                    "number": "o_100"
                } ]
            """).run();
    }

    @Test
    void filterNeo4J() {
        new QueryCustomTreeTest<>(
            datasources,
            datasources.neo4j(),
            """
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = "o_100")
                }
            """,
            (schema, datasource, plan) -> {
                final var onlyPattern = plan.stream().findFirst().get();

                final var filters = new ArrayList<Computation>();
                filters.add(scope.computation.create(Operator.Equal, onlyPattern.root.children().stream().findFirst().get().variable, new Constant("o_100")));

                return new DatasourceNode(
                    datasource,
                    plan,
                    schema,
                    List.of(),
                    filters,
                    onlyPattern.root.variable
                );
            },
            """
                [ {
                    "number": "o_100"
                } ]
            """).run();
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
                    ?product 15 ?id ;
                        16 ?label ;
                        17 ?price .
                }
            """)
            .expected("""
                [ {
                    "id": "123",
                    "label": "Clean Code",
                    "price": "125"
                }, {
                    "id": "765",
                    "label": "The Lord of the Rings",
                    "price": "199"
                }, {
                    "id": "457",
                    "label": "The Art of War",
                    "price": "299"
                }, {
                    "id": "734",
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
                    ?order -12 ?item .
                    ?item 14 ?quantity ;
                        13/15 ?id ;
                        13/16 ?label ;
                        13/17 ?price .
                }
            """)
            .expected("""
                [ {
                    "items": [
                        { "quantity": "1", "id": "123", "label": "Clean Code", "price": "125" }
                    ]
                }, {
                    "items": [
                        { "quantity": "2", "id": "765", "label": "The Lord of the Rings", "price": "199" }
                    ]
                }, {
                    "items": [
                        { "quantity": "7", "id": "457", "label": "The Art of War", "price": "299" },
                        { "quantity": "3", "id": "734", "label": "Animal Farm", "price": "350" }
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
                    ?order -12 ?item .
                    ?item 14 ?quantity ;
                        13/15 ?id ;
                }
            """)
            .expected("""
                [
                    { "quantity": "1", "id": "123" },
                    { "quantity": "2", "id": "765" },
                    { "quantity": "7", "id": "457" },
                    { "quantity": "3", "id": "734" }
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
                    ?item 12/1 ?number ;
                        13/15 ?id .
                }
            """)
            .expected("""
                [ {
                    "number": "o_100",
                    "id": "123"
                }, {
                    "number": "o_100",
                    "id": "765"
                }, {
                    "number": "o_200",
                    "id": "457"
                }, {
                    "number": "o_200",
                    "id": "734"
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
                    ?item 14 ?quantity ;
                        13/16 ?label .
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
                    ?item 14 ?quantity ;
                        12/8/9 ?street .
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
                    "quantity": "7",
                    "street": "Malostranské nám. 2/25"
                }, {
                    "quantity": "3",
                    "street": "Malostranské nám. 2/25"
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
                    ?item 12 ?order .
                    ?order 1 ?number .
                    ?item 13 ?product .
                    ?product 15 ?id .
                }
            """)
            .expected("""
                [ {
                    "number": "o_100",
                    "id": "123"
                }, {
                    "number": "o_100",
                    "id": "765"
                }, {
                    "number": "o_200",
                    "id": "457"
                }, {
                    "number": "o_200",
                    "id": "734"
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
