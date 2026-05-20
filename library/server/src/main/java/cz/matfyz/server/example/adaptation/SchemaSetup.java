package cz.matfyz.server.example.adaptation;

import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.tests.example.adaptation.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryEntity categoryEntity) {
        super(categoryEntity, Schema.newSchema());
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryEntity categoryEntity) {
        return new SchemaSetup(categoryEntity).innerCreateNewUpdate();
    }

    @Override protected Position createPosition(double x, double y) {
        return new Position(x, y);
    }

    @Override protected void createOperations() {
        addObjex(Schema.person, 0, -100);
        addObjex(Schema.customer, -200, -100);
        addObjex(Schema.seller, 200, 100);
        addObjex(Schema.product, 0, 100);
        addObjex(Schema.order, -200, 0);
        addObjex(Schema.orderItem, -200, 100);
        addObjex(Schema.review, 0, 0);
        addObjex(Schema.category, 200, -100);
        addObjex(Schema.hasCategory, 200, 0);
        addObjex(Schema.hasInterest, 200, -200);
        addObjex(Schema.follows, 0, -200);

        // person
        addObjex(Schema.personId, 0, 0);
        addObjex(Schema.person$name, 0, 0);
        addObjex(Schema.person$email, 0, 0);
        addObjex(Schema.person$createdAt, 0, 0);
        addObjex(Schema.person$countryCode, 0, 0);
        addObjex(Schema.person$isActive, 0, 0);
        addObjex(Schema.person$profile, 0, 0);

        // customer
        addObjex(Schema.customerId, 0, 0);
        addObjex(Schema.customer$snapshotAt, 0, 0);
        addObjex(Schema.customer$name, 0, 0);
        addObjex(Schema.customer$email, 0, 0);
        addObjex(Schema.customer$countryCode, 0, 0);
        addObjex(Schema.customer$isActive, 0, 0);
        addObjex(Schema.customer$profile, 0, 0);

        // seller
        addObjex(Schema.sellerId, 0, 0);
        addObjex(Schema.seller$displayName, 0, 0);
        addObjex(Schema.seller$createdAt, 0, 0);
        addObjex(Schema.seller$countryCode, 0, 0);
        addObjex(Schema.seller$isActive, 0, 0);

        // product
        addObjex(Schema.productId, 0, 0);
        addObjex(Schema.product$sku, 0, 0);
        addObjex(Schema.product$title, 0, 0);
        addObjex(Schema.product$description, 0, 0);
        addObjex(Schema.product$priceCents, 0, 0);
        addObjex(Schema.product$currency, 0, 0);
        addObjex(Schema.product$stockQty, 0, 0);
        addObjex(Schema.product$isActive, 0, 0);
        addObjex(Schema.product$createdAt, 0, 0);
        addObjex(Schema.product$updatedAt, 0, 0);
        addObjex(Schema.product$attributes, 0, 0);

        // order
        addObjex(Schema.orderId, 0, 0);
        addObjex(Schema.order$orderedAt, 0, 0);
        addObjex(Schema.order$status, 0, 0);
        addObjex(Schema.order$totalCents, 0, 0);
        addObjex(Schema.order$currency, 0, 0);
        addObjex(Schema.order$shipping, 0, 0);
        addObjex(Schema.order$payment, 0, 0);

        // orderItem
        addObjex(Schema.orderItemId, 0, 0);
        addObjex(Schema.orderItem$unitPriceCents, 0, 0);
        addObjex(Schema.orderItem$quantity, 0, 0);
        addObjex(Schema.orderItem$lineTotalCents, 0, 0);
        addObjex(Schema.orderItem$createdAt, 0, 0);
        addObjex(Schema.orderItem$productSnapshot, 0, 0);

        // review
        addObjex(Schema.reviewId, 0, 0);
        addObjex(Schema.review$rating, 0, 0);
        addObjex(Schema.review$title, 0, 0);
        addObjex(Schema.review$body, 0, 0);
        addObjex(Schema.review$createdAt, 0, 0);
        addObjex(Schema.review$helpfulVotes, 0, 0);

        // category
        addObjex(Schema.categoryId, 0, 0);
        addObjex(Schema.category$name, 0, 0);
        addObjex(Schema.category$path, 0, 0);

        // hasCategory
        addObjex(Schema.hasCategory$assignedAt, 0, 0);

        // hasInterest
        addObjex(Schema.hasInterest$strength, 0, 0);
        addObjex(Schema.hasInterest$createdAt, 0, 0);

        // follows
        addObjex(Schema.follows$createdAt, 0, 0);

        addMorphism(Schema.customer_person);
        addMorphism(Schema.follows_person$from);
        addMorphism(Schema.follows_person$to);
        addMorphism(Schema.order_customer);
        addMorphism(Schema.orderItem_order);
        addMorphism(Schema.orderItem_product);
        addMorphism(Schema.review_customer);
        addMorphism(Schema.review_product);
        addMorphism(Schema.hasCategory_product);
        addMorphism(Schema.hasCategory_category);
        addMorphism(Schema.hasInterest_person);
        addMorphism(Schema.hasInterest_category);

        // person
        addMorphism(Schema.person_personId);
        addMorphism(Schema.person_person$name);
        addMorphism(Schema.person_person$email);
        addMorphism(Schema.person_person$createdAt);
        addMorphism(Schema.person_person$countryCode);
        addMorphism(Schema.person_person$isActive);
        addMorphism(Schema.person_person$profile);

        // customer
        addMorphism(Schema.customer_customerId);
        addMorphism(Schema.customer_customer$snapshotAt);
        addMorphism(Schema.customer_customer$name);
        addMorphism(Schema.customer_customer$email);
        addMorphism(Schema.customer_customer$countryCode);
        addMorphism(Schema.customer_customer$isActive);
        addMorphism(Schema.customer_customer$profile);

        // seller
        addMorphism(Schema.seller_sellerId);
        addMorphism(Schema.seller_seller$displayName);
        addMorphism(Schema.seller_seller$createdAt);
        addMorphism(Schema.seller_seller$countryCode);
        addMorphism(Schema.seller_seller$isActive);

        // product
        addMorphism(Schema.product_seller);
        addMorphism(Schema.product_productId);
        addMorphism(Schema.product_product$sku);
        addMorphism(Schema.product_product$title);
        addMorphism(Schema.product_product$description);
        addMorphism(Schema.product_product$priceCents);
        addMorphism(Schema.product_product$currency);
        addMorphism(Schema.product_product$stockQty);
        addMorphism(Schema.product_product$isActive);
        addMorphism(Schema.product_product$createdAt);
        addMorphism(Schema.product_product$updatedAt);
        addMorphism(Schema.product_product$attributes);

        // order
        addMorphism(Schema.order_orderId);
        addMorphism(Schema.order_order$orderedAt);
        addMorphism(Schema.order_order$status);
        addMorphism(Schema.order_order$totalCents);
        addMorphism(Schema.order_order$currency);
        addMorphism(Schema.order_order$shipping);
        addMorphism(Schema.order_order$payment);

        // orderItem
        addMorphism(Schema.orderItem_orderItemId);
        addMorphism(Schema.orderItem_orderItem$unitPriceCents);
        addMorphism(Schema.orderItem_orderItem$quantity);
        addMorphism(Schema.orderItem_orderItem$lineTotalCents);
        addMorphism(Schema.orderItem_orderItem$createdAt);
        addMorphism(Schema.orderItem_orderItem$productSnapshot);

        // review
        addMorphism(Schema.review_reviewId);
        addMorphism(Schema.review_review$rating);
        addMorphism(Schema.review_review$title);
        addMorphism(Schema.review_review$body);
        addMorphism(Schema.review_review$createdAt);
        addMorphism(Schema.review_review$helpfulVotes);

        // category
        addMorphism(Schema.category_categoryId);
        addMorphism(Schema.category_category$name);
        addMorphism(Schema.category_category$path);

        // hasCategory
        addMorphism(Schema.hasCategory_hasCategory$assignedAt);

        // hasInterest
        addMorphism(Schema.hasInterest_hasInterest$strength);
        addMorphism(Schema.hasInterest_hasInterest$createdAt);

        // follows
        addMorphism(Schema.follows_follows$createdAt);
    }

}
