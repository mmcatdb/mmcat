package cz.cuni.matfyz.tests.querying;

import cz.cuni.matfyz.tests.database.BasicDatabases;
import cz.cuni.matfyz.tests.mapping.MongoDB;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);

    private static final BasicDatabases databases = new BasicDatabases();

    @BeforeAll
    public static void setup() {
        databases.postgreSQL().setup();
        databases.mongoDB().setup();
    }

    @Test
    public void basicPostgreSQL() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .
                }
            """)
            .expected("""
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]
            """)
            .run();
    }

    @Test
    public void basicMongoDB() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.mongoDB())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .
                }
            """)
            .expected("""
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]
            """)
            .run();
    }

    @Test
    public void alias() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .query("""
                SELECT {
                    ?o has_number ?n .
                }
                WHERE {
                    ?o 1 ?n .
                }
            """)
            .expected("""
                [{
                    "has_number": "o_100"
                }, {
                    "has_number": "o_200"
                }]
            """)
            .run();
    }

    /**
     * This test fails because the planning algorithm isn't able to find patterns in the mapping graph. Instead it just tries first available path from the root and if none is available, it simply fails.
     */
    @Test
    public void dualSignature() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?number -1 ?order .
                }
            """)
            .expected("""
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]
            """)
            .run();
    }

    @Test
    public void multipleElements() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.mongoDB())
            .query("""
                SELECT {
                    ?order tags ?tag .
                }
                WHERE {
                    ?order -2 ?tag .
                }
            """)
            .expected("""
                [{
                    "tags": [ "123", "456", "789" ]
                }, {
                    "tags": [ "123", "String456", "String789" ]
                }]
            """)
            .run();
    }

    @Test
    public void filter() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number = \"o_100\")
                }
            """)
            .expected("""
                [{
                    "number": "o_100"
                }]
            """)
            .run();
    }

    @Test
    public void multipleFilters() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .query("""
                SELECT {
                    ?order number ?number .
                }
                WHERE {
                    ?order 1 ?number .

                    FILTER(?number != \"o_200\")
                    FILTER(?number != \"o_300\")
                }
            """)
            .expected("""
                [{
                    "number": "o_100"
                }]
            """)
            .run();
    }

    @Test
    public void multipleProperties() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
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
                [{
                    "price": "125",
                    "id": "123",
                    "label": "Clean Code"
                }, {
                    "price": "299",
                    "id": "457",
                    "label": "The Art of War"
                }, {
                    "price": "350",
                    "id": "734",
                    "label": "Animal Farm"
                }, {
                    "price": "199",
                    "id": "765",
                    "label": "The Lord of the Rings"
                }]
            """)
            .run();
    }

    /**
     * The mapping "order_item" already contains all necessary information so the other mappings shouldn't be used.
     * This needs to be checked manually.
     */
    @Test
    public void notNeededJoin() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
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
                [{
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
                }]
            """)
            .run();
    }

    @Test
    public void oneDatabaseJoin() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
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
                [{
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
                }]
            """)
            .run();
    }

    @Test
    public void multipleDatabasesJoin() {
        new QueryTestBase(databases.schema)
            .addDatabase(databases.postgreSQL())
            .addDatabase(
                databases.createNewMongoDB()
                    .addMapping(MongoDB.address(databases.schema))
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
                [{
                    "quantity": "1",
                    "street": "hodnotaA"
                }, {
                    "quantity": "2",
                    "street": "hodnotaA"
                }, {
                    "quantity": "7",
                    "street": "hodnotaA2"
                }, {
                    "quantity": "3",
                    "street": "hodnotaA2"
                }]
            """)
            .run();
    }

}
