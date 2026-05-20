package cz.matfyz.tests.example.adaptation;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;

public abstract class Schema {

    public static final String schemaLabel = "Adaptation Schema";

    private static final SchemaBuilder b = new SchemaBuilder();

    // Keys

    public static final BuilderObjex
        person =       b.objex("Person", 1),
        customer =     b.objex("Customer", 2),
        seller =       b.objex("Seller", 3),
        product =      b.objex("Product", 4),
        order =        b.objex("Order", 5),
        orderItem =    b.objex("Order Item", 6),
        review =       b.objex("Review", 7),
        category =     b.objex("Category", 8),
        hasCategory =  b.objex("Has Category", 9),
        hasInterest =  b.objex("Has Interest", 10),
        follows =      b.objex("Follows", 11),

        // person
        personId = b.objex("person id", 12),
        person$name = b.objex("name", 13),
        person$email = b.objex("email", 14),
        person$createdAt = b.objex("created at", 15),
        person$countryCode = b.objex("country code", 16),
        person$isActive = b.objex("is active", 17),
        person$profile = b.objex("profile", 18),

        // customer
        customerId = b.objex("customer id", 19),
        customer$snapshotAt = b.objex("snapshot at", 20),
        customer$name = b.objex("name", 21),
        customer$email = b.objex("email", 22),
        customer$countryCode = b.objex("country code", 23),
        customer$isActive = b.objex("is active", 24),
        customer$profile = b.objex("profile", 25),

        // seller
        sellerId = b.objex("seller id", 26),
        seller$displayName = b.objex("display name", 27),
        seller$createdAt = b.objex("created at", 28),
        seller$countryCode = b.objex("country code", 29),
        seller$isActive = b.objex("is active", 30),

        // product
        productId = b.objex("product id", 31),
        product$sku = b.objex("sku", 32),
        product$title = b.objex("title", 33),
        product$description = b.objex("description", 34),
        product$priceCents = b.objex("price cents", 35),
        product$currency = b.objex("currency", 36),
        product$stockQty = b.objex("stock qty", 37),
        product$isActive = b.objex("is active", 38),
        product$createdAt = b.objex("created at", 39),
        product$updatedAt = b.objex("updated at", 40),
        product$attributes = b.objex("attributes", 41),

        // order
        orderId = b.objex("order id", 42),
        order$orderedAt = b.objex("ordered at", 43),
        order$status = b.objex("status", 44),
        order$totalCents = b.objex("total cents", 45),
        order$currency = b.objex("currency", 46),
        order$shipping = b.objex("shipping", 47),
        order$payment = b.objex("payment", 48),

        // orderItem
        orderItemId = b.objex("order item id", 49),
        orderItem$unitPriceCents = b.objex("unit price cents", 50),
        orderItem$quantity = b.objex("quantity", 51),
        orderItem$lineTotalCents = b.objex("line total cents", 52),
        orderItem$createdAt = b.objex("created at", 53),
        orderItem$productSnapshot = b.objex("product snapshot", 54),

        // review
        reviewId = b.objex("review id", 55),
        review$rating = b.objex("rating", 56),
        review$title = b.objex("title", 57),
        review$body = b.objex("body", 58),
        review$createdAt = b.objex("created at", 59),
        review$helpfulVotes = b.objex("helpful votes", 60),

        // category
        categoryId = b.objex("category id", 61),
        category$name = b.objex("name", 62),
        category$path = b.objex("path", 63),

        // hasCategory
        hasCategory$assignedAt = b.objex("assigned at", 64),

        // hasInterest
        hasInterest$strength = b.objex("strength", 65),
        hasInterest$createdAt = b.objex("created at", 66),

        // follows
        follows$createdAt = b.objex("created at", 67);

    // Signatures

    public static final BuilderMorphism
        customer_person = b.morphism(customer, person, 1),
        follows_person$from = b.morphism(follows, person, 2),
        follows_person$to = b.morphism(follows, person, 3),
        order_customer = b.morphism(order, customer, 4),
        orderItem_order = b.morphism(orderItem, order, 5),
        orderItem_product = b.morphism(orderItem, product, 6),
        review_customer = b.morphism(review, customer, 7),
        review_product = b.morphism(review, product, 8),
        hasCategory_product = b.morphism(hasCategory, product, 9),
        hasCategory_category = b.morphism(hasCategory, category, 10),
        hasInterest_person = b.morphism(hasInterest, person, 11),
        hasInterest_category = b.morphism(hasInterest, category, 12),

        // person
        person_personId = b.morphism(person, personId, 13),
        person_person$name = b.morphism(person, person$name, 14),
        person_person$email = b.morphism(person, person$email, 15),
        person_person$createdAt = b.morphism(person, person$createdAt, 16),
        person_person$countryCode = b.morphism(person, person$countryCode, 17),
        person_person$isActive = b.morphism(person, person$isActive, 18),
        person_person$profile = b.morphism(person, person$profile, 19),

        // customer
        customer_customerId = b.morphism(customer, customerId, 20),
        customer_customer$snapshotAt = b.morphism(customer, customer$snapshotAt, 21),
        customer_customer$name = b.morphism(customer, customer$name, 22),
        customer_customer$email = b.morphism(customer, customer$email, 23),
        customer_customer$countryCode = b.morphism(customer, customer$countryCode, 24),
        customer_customer$isActive = b.morphism(customer, customer$isActive, 25),
        customer_customer$profile = b.morphism(customer, customer$profile, 26),

        // seller
        seller_sellerId = b.morphism(seller, sellerId, 27),
        seller_seller$displayName = b.morphism(seller, seller$displayName, 28),
        seller_seller$createdAt = b.morphism(seller, seller$createdAt, 29),
        seller_seller$countryCode = b.morphism(seller, seller$countryCode, 30),
        seller_seller$isActive = b.morphism(seller, seller$isActive, 31),

        // product
        product_seller = b.morphism(product, seller, 32),
        product_productId = b.morphism(product, productId, 33),
        product_product$sku = b.morphism(product, product$sku, 34),
        product_product$title = b.morphism(product, product$title, 35),
        product_product$description = b.morphism(product, product$description, 36),
        product_product$priceCents = b.morphism(product, product$priceCents, 37),
        product_product$currency = b.morphism(product, product$currency, 38),
        product_product$stockQty = b.morphism(product, product$stockQty, 39),
        product_product$isActive = b.morphism(product, product$isActive, 40),
        product_product$createdAt = b.morphism(product, product$createdAt, 41),
        product_product$updatedAt = b.morphism(product, product$updatedAt, 42),
        product_product$attributes = b.morphism(product, product$attributes, 43),

        // order
        order_orderId = b.morphism(order, orderId, 44),
        order_order$orderedAt = b.morphism(order, order$orderedAt, 45),
        order_order$status = b.morphism(order, order$status, 46),
        order_order$totalCents = b.morphism(order, order$totalCents, 47),
        order_order$currency = b.morphism(order, order$currency, 48),
        order_order$shipping = b.morphism(order, order$shipping, 49),
        order_order$payment = b.morphism(order, order$payment, 50),

        // orderItem
        orderItem_orderItemId = b.morphism(orderItem, orderItemId, 51),
        orderItem_orderItem$unitPriceCents = b.morphism(orderItem, orderItem$unitPriceCents, 52),
        orderItem_orderItem$quantity = b.morphism(orderItem, orderItem$quantity, 53),
        orderItem_orderItem$lineTotalCents = b.morphism(orderItem, orderItem$lineTotalCents, 54),
        orderItem_orderItem$createdAt = b.morphism(orderItem, orderItem$createdAt, 55),
        orderItem_orderItem$productSnapshot = b.morphism(orderItem, orderItem$productSnapshot, 56),

        // review
        review_reviewId = b.morphism(review, reviewId, 57),
        review_review$rating = b.morphism(review, review$rating, 58),
        review_review$title = b.morphism(review, review$title, 59),
        review_review$body = b.morphism(review, review$body, 60),
        review_review$createdAt = b.morphism(review, review$createdAt, 61),
        review_review$helpfulVotes = b.morphism(review, review$helpfulVotes, 62),

        // category
        category_categoryId = b.morphism(category, categoryId, 63),
        category_category$name = b.morphism(category, category$name, 64),
        category_category$path = b.morphism(category, category$path, 65),

        // hasCategory
        hasCategory_hasCategory$assignedAt = b.morphism(hasCategory, hasCategory$assignedAt, 66),

        // hasInterest
        hasInterest_hasInterest$strength = b.morphism(hasInterest, hasInterest$strength, 67),
        hasInterest_hasInterest$createdAt = b.morphism(hasInterest, hasInterest$createdAt, 68),

        // follows
        follows_follows$createdAt = b.morphism(follows, follows$createdAt, 69);

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return b.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return b.buildMetadata(schema);
    }

    private Schema() {}

}
