package cz.matfyz.tests.querying;

import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.core.querytree.DatasourceNode.SerializedDatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode.SerializedFilterNode;
import cz.matfyz.querying.core.querytree.JoinNode.SerializedJoinNode;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizationTests {
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
    void filterDeepening_path() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .addDatasource(datasources.mongoDB())
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
            .restrictQueryTree(description -> {
                return
                    (description.tree() instanceof SerializedDatasourceNode datasourceNode) &&
                    datasourceNode.filters().size() == 2;
            })
            .run();
    }

    @Test
    void filterDeepening_goodJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .addDatasource(
                datasources.createNewMongoDB()
                    .addMapping(MongoDB.customer(datasources.schema))
            )
            .query("""
                SELECT {
                    ?orderItem
                        orderNumber ?orderNumber ;
                        customer ?customerName ;
                        quantity ?quantity .

                }
                WHERE {
                    ?orderItem
                        12/1 ?orderNumber ;
                        12/3/4 ?customerName ;
                        14 ?quantity .

                    FILTER(?customerName = "Alice")
                }
            """)
            .restrictQueryTree(description -> {
                return
                    (description.tree() instanceof SerializedJoinNode joinNode) &&
                    (joinNode.fromChild() instanceof SerializedDatasourceNode dsn1) &&
                    (joinNode.toChild() instanceof SerializedDatasourceNode dsn2) &&
                    (dsn1.filters().size() > 0 || dsn2.filters().size() > 0);
            })
            .run();
    }

    @Test
    void filterDeepening_badJoin() {
        new QueryTestBase(datasources.schema)
            .addDatasource(datasources.postgreSQL())
            .addDatasource(
                datasources.createNewMongoDB()
                    .addMapping(MongoDB.customer(datasources.schema))
            )
            .query("""
                SELECT {
                    ?orderItem
                        orderNumber ?orderNumber ;
                        customer ?customerName ;
                        quantity ?quantity .

                }
                WHERE {
                    ?orderItem
                        12/1 ?orderNumber ;
                        12/3/4 ?customerName ;
                        14 ?quantity .

                    # FILTER(?customerName = "Alice" || ?quantity >= "2") // TODO: fix the bug which prevents this condition from being parsed
                    FILTER(?customerName != ?quantity)
                }
            """)
            .restrictQueryTree(description -> {
                return
                    (description.tree() instanceof SerializedFilterNode filterNode) &&
                    (filterNode.child() instanceof SerializedJoinNode joinNode) &&
                    (joinNode.fromChild() instanceof SerializedDatasourceNode dsn1) &&
                    (joinNode.toChild() instanceof SerializedDatasourceNode dsn2) &&
                    (dsn1.filters().size() == 0 && dsn2.filters().size() == 0);
            })
            .run();
    }
}
