package cz.matfyz.tests.querying;

import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.DatasourceNode.SerializedDatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode.SerializedFilterNode;
import cz.matfyz.querying.core.querytree.JoinNode.SerializedJoinNode;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;

import java.util.List;
import java.util.Set;

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
            .expected("""
                [
                    {"customer":"Alice","orderNumber":"o_100","quantity":"1"},
                    {"customer":"Alice","orderNumber":"o_100","quantity":"2"}
                ]
            """)
            .run();
    }

    @Test
    void dependentJoin_fromRef() {
        new QueryCustomTreeTest(datasources.schema)
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
            .queryTreeBuilder((plans) -> {
                final var plan = plans.get(0);
                final var customer = plan.stream().filter(pattern -> "customer".equals(pattern.kind.kindName())).findFirst().get();
                final var orderItem = plan.stream().filter(pattern -> "order_item".equals(pattern.kind.kindName())).findFirst().get();
                final var joinVariable = customer.root.children().stream().filter(child -> "orderNumber".equals(child.variable.name())).findFirst().get().variable;

                final var filterVariable = customer.root.children().stream().skip(1).findFirst().get().children().stream().filter(child -> "customerName".equals(child.variable.name())).findFirst().get().variable;

                return new JoinNode(
                    new DatasourceNode(
                        customer.kind.datasource(),
                        Set.of(customer),
                        datasources.schema,
                        List.of(),
                        List.of(
                            scope.computation.create(Operator.Equal, filterVariable, new Constant("Alice"))
                        ),
                        customer.root.variable
                    ),
                    new DatasourceNode(
                        orderItem.kind.datasource(),
                        Set.of(orderItem),
                        datasources.schema,
                        List.of(),
                        List.of(),
                        orderItem.root.variable
                    ),
                    new JoinCandidate(
                        JoinType.IdRef,
                        customer,
                        orderItem,
                        joinVariable,
                        0,
                        false
                    ))
                    .forceDepJoinFromId();
            })
            .expected("""
                [
                    {"customer":"Alice","orderNumber":"o_100","quantity":"1"},
                    {"customer":"Alice","orderNumber":"o_100","quantity":"2"}
                ]
            """)
            .run();
    }
}
