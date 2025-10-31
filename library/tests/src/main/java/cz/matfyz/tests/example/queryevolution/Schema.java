package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Query Evolution Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    // Version 1
    public static final BuilderObjex customer =        builder.objex("Customer", 1);
    public static final BuilderObjex customerId =      builder.objex("id", 2);
    public static final BuilderObjex name =            builder.objex("name", 3);
    public static final BuilderObjex surname =         builder.objex("surname", 4);
    public static final BuilderObjex knows =           builder.objex("knows", 5);
    public static final BuilderObjex order =           builder.objex("Order", 6);
    public static final BuilderObjex orderId =         builder.objex("oid", 7);
    public static final BuilderObjex street =          builder.objex("street", 8);
    public static final BuilderObjex city =            builder.objex("city", 9);
    public static final BuilderObjex postCode =        builder.objex("postCode", 10);
    public static final BuilderObjex orderPrice =      builder.objex("price", 11);
    public static final BuilderObjex quantity =        builder.objex("quantity", 12);
    public static final BuilderObjex product =         builder.objex("Product", 13);
    public static final BuilderObjex productId =       builder.objex("pid", 14);
    public static final BuilderObjex title =           builder.objex("title", 15);
    public static final BuilderObjex productPrice =    builder.objex("price", 16);

    // Version 2
    public static final BuilderObjex item =            builder.objex("Item", 17);
    public static final BuilderObjex ordered =         builder.objex("Ordered", 18);

    // Signatures

    // Version 1
    public static final BuilderMorphism customer_customerId =  builder.morphism(customer, customerId, 1);
    public static final BuilderMorphism customer_name =        builder.morphism(customer, name, 2);
    public static final BuilderMorphism customer_surname =     builder.morphism(customer, surname, 3);
    public static final BuilderMorphism knows_customerA =      builder.morphism(knows, customer, 4);
    public static final BuilderMorphism knows_customerB =      builder.morphism(knows, customer, 5);
    public static final BuilderMorphism order_orderId =        builder.morphism(order, orderId, 6);
    public static final BuilderMorphism order_street =         builder.morphism(order, street, 7);
    public static final BuilderMorphism order_city =           builder.morphism(order, city, 8);
    public static final BuilderMorphism order_postCode =       builder.morphism(order, postCode, 9);
    public static final BuilderMorphism order_orderPrice =     builder.morphism(order, orderPrice, 10);
    public static final BuilderMorphism order_quantity =       builder.morphism(order, quantity, 11);
    public static final BuilderMorphism order_customer =       builder.morphism(order, customer, 12);
    public static final BuilderMorphism order_product =        builder.morphism(order, product, 13);
    public static final BuilderMorphism product_productId =    builder.morphism(product, productId, 14);
    public static final BuilderMorphism product_title =        builder.min(Min.ZERO).morphism(product, title, 15);
    public static final BuilderMorphism product_productPrice = builder.min(Min.ZERO).morphism(product, productPrice, 16);

    public static final Signature order_customerId =           builder.concatenate(order_customer, customer_customerId);
    public static final Signature order_productId =            builder.concatenate(order_product, product_productId);

    public static final Signature customerA_customerB =        builder.concatenate(knows_customerA.dual(), knows_customerB);

    // Version 2
    public static final BuilderMorphism item_orderPrice =      builder.morphism(item, orderPrice, 10);
    public static final BuilderMorphism item_quantity =        builder.morphism(item, quantity, 11);
    public static final BuilderMorphism item_product =         builder.morphism(item, product, 13);
    public static final BuilderMorphism item_order =           builder.morphism(item, order, 17);

    public static final Signature item_productId =             builder.concatenate(item_product, product_productId);
    public static final Signature item_title =                 builder.concatenate(item_product, product_title);
    public static final Signature item_productPrice =          builder.concatenate(item_product, product_productPrice);
    public static final Signature item_orderId =               builder.concatenate(item_order, order_orderId);

    // The same key here is intentional - we want to replace the previous morphisms.
    public static final BuilderMorphism ordered_customer =     builder.morphism(ordered, customer, 12);
    public static final BuilderMorphism ordered_order =        builder.morphism(ordered, order, 18);

    public static final Signature ordered_customerId =         builder.concatenate(ordered_customer, customer_customerId);
    public static final Signature ordered_orderId =            builder.concatenate(ordered_order, order_orderId);

    public static final Signature order_customer2 =            builder.concatenate(ordered_order.dual(), ordered_customer);

    // Ids

    static {
        builder
            .ids(customer, customer_customerId)
            .ids(knows, knows_customerA, knows_customerB)
            .ids(product, product_productId)
            .ids(order, order_orderId, order_productId)
            .ids(item, item_orderId, item_productId)
            .ids(ordered, ordered_orderId, ordered_customerId);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema(int version) {
        if (version < 1) {
            builder.skip(
                item,
                ordered
            );
        }

        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

    private Schema() {}

}
