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
            .add("Order history for a person (order, customer)", orderHistoryForPerson, mockQueryStats(seed++, 482, 1.2))
            .add("Order details view (order, customer, order_item, product)", orderDetails, mockQueryStats(seed++, 145, 1.7))
            .add("How many times did this person bought these products? (order, customer, order_item)", productPurchasesForPerson, mockQueryStats(seed++, 739, 3.2))
            .add("Seller daily revenue for last 30 days (order, order_item, product)", sellerDailyRevenue, mockQueryStats(seed++, 12, 1.2))
            .add("Top products by revenue inside one category, last 7-30 days (order, order_item, product, has_category)", topProductsByRevenue, mockQueryStats(seed++, 956, 1.4))
            .add("Customer spend buckets (order, customer)", customerSpendBuckets, mockQueryStats(seed++, 321, 1.8))
            .add("Fraud-ish pattern (order, customer, order_item, product)", fraudPattern, mockQueryStats(seed++, 678, 1.2))
            .add("Who should I follow? (follows)", whoToFollow, mockQueryStats(seed++, 504, 1.7))
            .add("Personalized feed candidates (product, has_category, has_interest)", personalizedFeedCandidates, mockQueryStats(seed++, 219, 3.2))
            .add("Product page read (product, seller, review)", productPageRead, mockQueryStats(seed++, 843, 1.4))
            .add("People also bought using shared orders (order, order_item)", peopleAlsoBought, mockQueryStats(seed++, 67, 1.8))
            .add("Order revenue by status and currency (order)", orderRevenueByStatusCurrency, mockQueryStats(seed++, 411, 1.2))
            .add("Active product inventory by price band and currency (product)", productInventoryByPriceBand, mockQueryStats(seed++, 795, 1.7))
            .add("Review rating distribution by helpfulness (review)", reviewRatingDistribution, mockQueryStats(seed++, 362, 3.2))
            .add("Customer snapshot activity by country (customer)", customerSnapshotActivity, mockQueryStats(seed++, 188, 1.2))
            .add("Seller activity by country (seller)", sellerActivityRollupByCountry, mockQueryStats(seed++, 903, 1.4))
            .add("Line item quantity distribution (order_item)", lineItemQuantityDistribution, mockQueryStats(seed++, 550, 1.8))
            .add("Seller sales summary for recent paid/shipped orders (order, order_item, product, seller)", sellerSalesSummary, mockQueryStats(seed++, 234, 1.2))
            .add("Customer-country order status summary (customer, order)", customerCountryOrderStatus, mockQueryStats(seed++, 617, 1.7))
            .add("Product review summary for selected products (review)", productReviewSummary, mockQueryStats(seed++, 876, 3.2))
            .add("Category audience strength summary (person, has_interest, category)", categoryInterestSummary, mockQueryStats(seed++, 49, 1.4))
            .add("Active category catalog summary (product, has_category)", categoryCatalogSummary, mockQueryStats(seed++, 388, 1.8))
            .add("Seller catalog health summary (seller, product)", sellerCatalogHealth, mockQueryStats(seed++, 724, 1.2))
            .add("Follow graph country rollup (person, follows)", followCountryRollup, mockQueryStats(seed++, 105, 1.7))
            .build(queryService::create);
    }

    private static QueryStats mockQueryStats(int seed, int executionCount, double factor) {
        return cz.matfyz.server.example.tpch.ExampleSetup.mockQueryStats(seed, executionCount, factor);
    }

    final private String orderHistoryForPerson = """
        SELECT
            o.order_id,
            o.ordered_at,
            o.status,
            o.total_cents,
            o.currency
        FROM "order" o
        JOIN customer c ON c.customer_id = o.customer_id
        WHERE c.person_id = '{person_id}'
        ORDER BY o.ordered_at DESC
    """;

    final private String orderDetails = """
        SELECT
            o.order_id,
            o.ordered_at,
            o.status,
            oi.order_item_id,
            oi.product_id,
            p.title,
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

    final private String productPurchasesForPerson = """
        SELECT COUNT(*)
        FROM customer c
        JOIN "order" o ON o.customer_id = c.customer_id
        JOIN order_item oi ON oi.order_id = o.order_id
        WHERE c.person_id IN ({person_ids})
            AND oi.product_id IN ({product_ids})
            AND o.status IN ('paid', 'shipped')
    """;

    final private String sellerDailyRevenue = """
        SELECT
            date_trunc('day', o.ordered_at) AS day,
            SUM(oi.line_total_cents) AS revenue_cents,
            COUNT(DISTINCT o.order_id) AS "order"
        FROM "order" o
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE p.seller_id IN ({seller_ids})
            AND o.ordered_at >= '{date}'
            AND o.status IN ('paid', 'shipped')
        GROUP BY 1
        ORDER BY 1
    """;

    final private String topProductsByRevenue = """
        SELECT
            oi.product_id,
            p.title,
            SUM(oi.line_total_cents) AS revenue_cents,
            SUM(oi.quantity) AS units
        FROM has_category hc
        JOIN product p ON p.product_id = hc.product_id
        JOIN order_item oi ON oi.product_id = hc.product_id
        JOIN "order" o ON o.order_id = oi.order_id
        WHERE hc.category_id IN ({category_ids})
            AND o.ordered_at >= '{date}'
            AND o.status IN ('paid', 'shipped')
        GROUP BY oi.product_id, p.title
        ORDER BY revenue_cents DESC
        LIMIT 200
    """;

    final private String customerSpendBuckets = """
        WITH spend AS (
            SELECT c.person_id, SUM(o.total_cents) AS spend_cents
            FROM customer c
            JOIN "order" o ON o.customer_id = c.customer_id
            WHERE o.ordered_at >= '{date}'
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

    final private String fraudPattern = """
        SELECT
            c.person_id,
            COUNT(DISTINCT p.seller_id) AS distinct_sellers,
            COUNT(DISTINCT o.order_id) AS "order"
        FROM customer c
        JOIN "order" o ON o.customer_id = c.customer_id
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE o.ordered_at >= '{date}'
            AND o.status IN ('paid', 'shipped')
        GROUP BY c.person_id
        HAVING COUNT(DISTINCT p.seller_id) >= {distinct_sellers_threshold}
        ORDER BY distinct_sellers DESC
        LIMIT 200
    """;

    final private String whoToFollow = """
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
        LIMIT 200
    """;

    final private String personalizedFeedCandidates = """
        SELECT
            hc.product_id,
            SUM(hi.strength) AS interest_score
        FROM has_interest hi
        JOIN has_category hc ON hc.category_id = hi.category_id
        JOIN product p ON p.product_id = hc.product_id
        WHERE hi.person_id IN ({person_ids})
            AND p.is_active = TRUE
        GROUP BY hc.product_id
        ORDER BY interest_score DESC
        LIMIT 200
    """;

    final private String productPageRead = """
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
                LIMIT 50
            ) r
            GROUP BY r.product_id
        ) tr ON tr.product_id = p.product_id
        WHERE p.product_id = '{product_id}'
            AND p.is_active = TRUE
    """;

    final private String peopleAlsoBought = """
        SELECT
            oi2.product_id,
            COUNT(*) AS co_buy
        FROM order_item oi1
        JOIN order_item oi2 ON oi1.order_id = oi2.order_id
        JOIN "order" o ON o.order_id = oi1.order_id
        WHERE oi1.product_id IN ({product_ids})
            AND oi2.product_id NOT IN ({product_ids})
            AND o.status IN ('paid', 'shipped')
        GROUP BY oi2.product_id
        ORDER BY co_buy DESC
        LIMIT 20
    """;

    final private String orderRevenueByStatusCurrency = """
        SELECT
            o.status,
            o.currency,
            COUNT(*)::int AS orders,
            SUM(o.total_cents)::bigint AS revenue_cents,
            AVG(o.total_cents)::float8 AS avg_order_cents
        FROM "order" o
        WHERE o.status IN ({statuses})
        GROUP BY o.status, o.currency
        ORDER BY o.status, o.currency
    """;

    final private String productInventoryByPriceBand = """
        WITH filtered AS (
            SELECT
                CASE
                    WHEN p.price_cents < 1000 THEN 0
                    WHEN p.price_cents < 5000 THEN 1
                    WHEN p.price_cents < 20000 THEN 2
                    ELSE 3
                END AS price_band,
                p.currency,
                p.stock_qty,
                p.price_cents
            FROM product p
            WHERE p.is_active = TRUE
                AND p.price_cents <= {max_price_cents}
                AND p.stock_qty >= {min_stock_qty}
        )
        SELECT
            price_band,
            currency,
            COUNT(*)::int AS products,
            SUM(stock_qty)::bigint AS stock_qty,
            AVG(price_cents)::float8 AS avg_price_cents
        FROM filtered
        GROUP BY price_band, currency
        ORDER BY currency, price_band
    """;

    final private String reviewRatingDistribution = """
        SELECT
            r.rating,
            COUNT(*)::int AS reviews,
            AVG(r.helpful_votes)::float8 AS avg_helpful_votes
        FROM review r
        WHERE r.helpful_votes >= {min_helpful_votes}
        GROUP BY r.rating
        ORDER BY r.rating
    """;

    final private String customerSnapshotActivity = """
        SELECT
            c.country_code,
            c.is_active,
            COUNT(*)::int AS customers,
            COUNT(DISTINCT c.person_id)::int AS persons
        FROM customer c
        WHERE c.country_code IN ({country_codes})
        GROUP BY c.country_code, c.is_active
        ORDER BY c.country_code, c.is_active
    """;

    final private String sellerActivityRollupByCountry = """
        SELECT
            s.country_code,
            s.is_active,
            COUNT(*)::int AS sellers
        FROM seller s
        WHERE s.country_code IN ({country_codes})
        GROUP BY s.country_code, s.is_active
        ORDER BY s.country_code, s.is_active
    """;

    final private String lineItemQuantityDistribution = """
        SELECT
            oi.quantity,
            COUNT(*)::int AS items,
            SUM(oi.quantity)::bigint AS units,
            SUM(oi.line_total_cents)::bigint AS revenue_cents,
            AVG(oi.unit_price_cents)::float8 AS avg_unit_price_cents
        FROM order_item oi
        WHERE oi.unit_price_cents >= {min_unit_price_cents}
        GROUP BY oi.quantity
        ORDER BY oi.quantity
    """;

    final private String sellerSalesSummary = """
        SELECT
            p.seller_id,
            COUNT(DISTINCT o.order_id)::int AS orders,
            COUNT(*)::int AS items,
            SUM(oi.quantity)::bigint AS units,
            SUM(oi.line_total_cents)::bigint AS revenue_cents
        FROM "order" o
        JOIN order_item oi ON oi.order_id = o.order_id
        JOIN product p ON p.product_id = oi.product_id
        WHERE p.seller_id IN ({seller_ids})
            AND o.ordered_at >= '{date}'
            AND o.status IN ('paid', 'shipped')
        GROUP BY p.seller_id
        ORDER BY revenue_cents DESC, p.seller_id
        LIMIT 200
    """;

    final private String customerCountryOrderStatus = """
        SELECT
            c.country_code,
            o.status,
            COUNT(*)::int AS orders,
            SUM(o.total_cents)::bigint AS revenue_cents,
            AVG(o.total_cents)::float8 AS avg_order_cents
        FROM customer c
        JOIN "order" o ON o.customer_id = c.customer_id
        WHERE c.country_code IN ({country_codes})
            AND o.ordered_at >= '{date}'
        GROUP BY c.country_code, o.status
        ORDER BY c.country_code, o.status
    """;

    final private String productReviewSummary = """
        SELECT
            r.product_id,
            COUNT(*)::int AS reviews,
            AVG(r.rating)::float8 AS avg_rating,
            SUM(r.helpful_votes)::bigint AS helpful_votes,
            MAX(r.helpful_votes)::int AS max_helpful_votes
        FROM review r
        WHERE r.product_id IN ({product_ids})
            AND r.helpful_votes >= {min_helpful_votes}
        GROUP BY r.product_id
        ORDER BY reviews DESC, r.product_id
        LIMIT 200
    """;

    final private String categoryInterestSummary = """
        SELECT
            hi.category_id,
            COUNT(DISTINCT hi.person_id)::int AS interested_persons,
            AVG(hi.strength)::float8 AS avg_strength,
            MAX(hi.strength)::int AS max_strength
        FROM has_interest hi
        JOIN person p ON p.person_id = hi.person_id
        WHERE hi.category_id IN ({category_ids})
            AND hi.strength >= {min_strength}
            AND p.is_active = TRUE
        GROUP BY hi.category_id
        ORDER BY interested_persons DESC, hi.category_id
        LIMIT 200
    """;

    final private String categoryCatalogSummary = """
        SELECT
            hc.category_id,
            COUNT(DISTINCT p.product_id)::int AS products,
            SUM(p.stock_qty)::bigint AS stock_qty,
            AVG(p.price_cents)::float8 AS avg_price_cents
        FROM has_category hc
        JOIN product p ON p.product_id = hc.product_id
        WHERE hc.category_id IN ({category_ids})
            AND p.is_active = TRUE
            AND p.price_cents <= {max_price_cents}
            AND p.stock_qty >= {min_stock_qty}
        GROUP BY hc.category_id
        ORDER BY products DESC, hc.category_id
        LIMIT 200
    """;

    final private String sellerCatalogHealth = """
        SELECT
            p.seller_id,
            COUNT(*)::int AS products,
            SUM(CASE WHEN p.is_active THEN 1 ELSE 0 END)::int AS active_products,
            SUM(CASE WHEN p.is_active THEN 0 ELSE 1 END)::int AS inactive_products,
            SUM(p.stock_qty)::bigint AS stock_qty,
            AVG(p.price_cents)::float8 AS avg_price_cents
        FROM product p
        WHERE p.seller_id IN ({seller_ids})
        GROUP BY p.seller_id
        ORDER BY products DESC, p.seller_id
        LIMIT 200
    """;

    final private String followCountryRollup = """
        SELECT
            p_from.country_code AS from_country,
            p_to.country_code AS to_country,
            COUNT(*)::int AS edges,
            SUM(CASE WHEN p_to.is_active THEN 1 ELSE 0 END)::int AS active_targets
        FROM follows f
        JOIN person p_from ON p_from.person_id = f.from_id
        JOIN person p_to ON p_to.person_id = f.to_id
        WHERE p_from.country_code IN ({country_codes})
        GROUP BY p_from.country_code, p_to.country_code
        ORDER BY edges DESC, from_country, to_country
        LIMIT 200
    """;

}
