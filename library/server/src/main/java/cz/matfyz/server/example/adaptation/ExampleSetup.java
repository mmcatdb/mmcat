package cz.matfyz.server.example.adaptation;

import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryService;
import cz.matfyz.server.querying.QueryStats;
import cz.matfyz.server.utils.Configuration.SetupProperties;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.tests.example.adaptation.Schema;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.ExampleController.Example;
import cz.matfyz.server.example.common.DatasourceBuilder;
import cz.matfyz.server.example.common.QueryBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("adaptationExampleSetup")
public class ExampleSetup {

    public SchemaCategoryEntity setup() {
        final SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = createDatasources();

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Example.adaptation, Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity);

        createQueries(schemaEntity.id());

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    @Autowired
    private SetupProperties properties;

    @Autowired
    private DatasourceService datasourceService;

    private List<DatasourceEntity> createDatasources() {
        final var builder = new DatasourceBuilder(properties, properties.tpchDatabase());

        return datasourceService.createIfNotExists(List.of(
            builder.createPostgreSQL("PostgreSQL"),
            builder.createMongoDB("MongoDB"),
            builder.createNeo4j("Neo4j")
        ));
    }

    @Autowired
    private QueryService queryService;

    private List<Query> createQueries(Id categoryId) {
        int seed = -1;
        return new QueryBuilder(categoryId)
            .add("Order history for a person", orderHistoryForPersonQuery, mockQueryStats(seed++, 5, 1.2))
            .add("Order details view", orderDetailsQuery, mockQueryStats(seed++, 25, 1.7))
            .add("How many times did this person buy this product?", productPurchasesForPersonQuery, mockQueryStats(seed++, 10, 3.2))
            .add("Seller daily revenue for last 30 days", sellerDailyRevenueQuery, mockQueryStats(seed++, 15, 1.2))
            .add("Top products by revenue inside one category, last 7 days", topProductsByRevenueQuery, mockQueryStats(seed++, 30, 1.4))
            .add("Customer spend buckets", customerSpendBucketsQuery, mockQueryStats(seed++, 10, 1.8))
            .add("Fraud-ish pattern", fraudPatternQuery, mockQueryStats(seed++, 5, 1.2))
            .add("Who should I follow?", whoToFollowQuery, mockQueryStats(seed++, 25, 1.7))
            .add("Personalized feed candidates", personalizedFeedCandidatesQuery, mockQueryStats(seed++, 10, 3.2))
            .add("Product page read", productPageReadQuery, mockQueryStats(seed++, 30, 1.4))
            .add("People also bought", peopleAlsoBoughtQuery, mockQueryStats(seed++, 10, 1.8))
            .build(queryService::create);
    }

    private static QueryStats mockQueryStats(int seed, int executionCount, double factor) {
        return cz.matfyz.server.example.tpch.ExampleSetup.mockQueryStats(seed, executionCount, factor);
    }

    final private String orderHistoryForPersonQuery = """
        SELECT o.order_id, o.ordered_at, o.status, o.total_cents, o.currency
        FROM "order" o
        JOIN customer c ON c.customer_id = o.customer_id
        WHERE c.person_id = '{person_id}'
        ORDER BY o.ordered_at DESC
        LIMIT 20
    """;

    final private String orderDetailsQuery = """
        SELECT
            o.order_id,
            o.ordered_at,
            o.status,
            oi.order_item_id,
            oi.product_id,
            COALESCE(oi.product_snapshot->>'title', p.title) AS product_title,
            oi.unit_price_cents,
            oi.quantity,
            oi.line_total_cents
        FROM "order" o
        JOIN customer c ON c.customer_id = o.customer_id
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE c.person_id = '{person_id}'
        ORDER BY oi.order_item_id
    """;

    final private String productPurchasesForPersonQuery = """
        SELECT COUNT(*)
        FROM customer c
        JOIN "order" o ON o.customer_id = c.customer_id
        JOIN order_item oi ON oi.order_id = o.order_id
        WHERE c.person_id = '{person_id}'
            AND oi.product_id = '{product_id}'
            AND o.status IN ('paid', 'shipped')
    """;

    final private String sellerDailyRevenueQuery = """
        SELECT
            date_trunc('day', o.ordered_at) AS day,
            SUM(oi.line_total_cents) AS revenue_cents,
            COUNT(DISTINCT o.order_id) AS "order"
        FROM "order" o
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE p.seller_id = '{seller_id}'
            AND o.ordered_at >= now() - INTERVAL '30 days'
            AND o.status IN ('paid', 'shipped')
        GROUP BY 1
        ORDER BY 1
    """;

    final private String topProductsByRevenueQuery = """
        SELECT
            oi.product_id,
            MAX(p.title) AS title,
            SUM(oi.line_total_cents) AS revenue_cents,
            SUM(oi.quantity) AS units
        FROM has_category hc
        JOIN product p ON p.product_id = hc.product_id
        JOIN order_item oi ON oi.product_id = hc.product_id
        JOIN "order" o ON o.order_id = oi.order_id
        WHERE hc.category_id = '{category_id}'
            AND o.ordered_at >= now() - INTERVAL '7 days'
            AND o.status IN ('paid', 'shipped')
        GROUP BY oi.product_id
        ORDER BY revenue_cents DESC
        LIMIT 50
    """;

    final private String customerSpendBucketsQuery = """
        WITH spend AS (
            SELECT c.person_id, SUM(o.total_cents) AS spend_cents
            FROM customer c
            JOIN "order" o ON o.customer_id = c.customer_id
            WHERE o.ordered_at >= now() - INTERVAL '90 days'
                AND o.status IN ('paid', 'shipped')
            GROUP BY c.person_id
        )
        SELECT
            CASE
                WHEN spend_cents < 5000 THEN 'low'
                WHEN spend_cents < 20000 THEN 'mid'
                ELSE 'high'
            END AS bucket,
            COUNT(*) AS persons
        FROM spend
        GROUP BY 1
        ORDER BY 1
    """;

    final private String fraudPatternQuery = """
        SELECT
            c.person_id,
            COUNT(DISTINCT p.seller_id) AS distinct_sellers,
            COUNT(DISTINCT o.order_id) AS "order"
        FROM customer c
        JOIN "order" o ON o.customer_id = c.customer_id
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE o.ordered_at >= now() - INTERVAL '24 hours'
            AND o.status IN ('paid', 'shipped')
        GROUP BY c.person_id
        HAVING COUNT(DISTINCT p.seller_id) >= 10
        ORDER BY distinct_sellers DESC
        LIMIT 200
    """;

    final private String whoToFollowQuery = """
        SELECT
            f2.from_id AS person_id,
            COUNT(*) AS paths
        FROM follows f1
        JOIN follows f2 ON f1.from_id = f2.to_id
        WHERE f1.to_id = '{person_id}'
            AND f2.from_id <> '{person_id}'
            AND NOT EXISTS (
                SELECT 1
                FROM follows direct
                WHERE direct.to_id = '{person_id}'
                    AND direct.from_id = f2.from_id
            )
        GROUP BY f2.from_id
        ORDER BY paths DESC
        LIMIT 50
    """;


    final private String personalizedFeedCandidatesQuery = """
        SELECT
            hc.product_id,
            SUM(hi.strength) AS interest_score
        FROM has_interest hi
        JOIN has_category hc ON hc.category_id = hi.category_id
        JOIN product p ON p.product_id = hc.product_id
        WHERE hi.person_id = '{person_id}'
            AND p.is_active = TRUE
        GROUP BY hc.product_id
        ORDER BY interest_score DESC
        LIMIT 200
    """;

    final private String productPageReadQuery = """
        SELECT
            p.product_id,
            p.title,
            p.price_cents,
            p.currency,
            p.stock_qty,
            jsonb_build_object(
                'seller_id', s.seller_id,
                'display_name', s.display_name
            ) AS seller,
            jsonb_build_object(
                'avg', COALESCE(rs.avg_rating, 0),
                'count', COALESCE(rs.review_count, 0)
            ) AS rating_summary,
            COALESCE(tr.top_reviews, '[]'::jsonb) AS top_reviews
        FROM product p
        JOIN seller s ON s.seller_id = p.seller_id
        LEFT JOIN (
            SELECT
                r.product_id,
                AVG(r.rating)::float8 AS avg_rating,
                COUNT(*)::int AS review_count
            FROM review r
            WHERE r.product_id = '{product_id}'
            GROUP BY r.product_id
        ) rs ON rs.product_id = p.product_id
        LEFT JOIN (
            SELECT
                r.product_id,
                jsonb_agg(
                jsonb_build_object(
                    'customer_id', r.customer_id,
                    'rating', r.rating,
                    'title', r.title,
                    'body', r.body,
                    'created_at', r.created_at,
                    'helpful_votes', r.helpful_votes
                )
                ORDER BY r.helpful_votes DESC, r.created_at DESC
                ) AS top_reviews
            FROM (
                SELECT *
                FROM review
                WHERE product_id = '{product_id}'
                ORDER BY helpful_votes DESC, created_at DESC
                LIMIT 5
            ) r
            GROUP BY r.product_id
        ) tr ON tr.product_id = p.product_id
        WHERE p.product_id = '{product_id}'
            AND p.is_active = TRUE
    """;

    final private String peopleAlsoBoughtQuery = """
        SELECT
            oi2.product_id,
            COUNT(*) AS co_buy
        FROM order_item oi1
        JOIN order_item oi2 ON oi1.order_id = oi2.order_id
        JOIN "order" o ON o.order_id = oi1.order_id
        WHERE oi1.product_id = '{product_id}'
            AND oi2.product_id <> '{product_id}'
            AND o.status IN ('paid', 'shipped')
        GROUP BY oi2.product_id
        ORDER BY co_buy DESC
        LIMIT 20
    """;

}
