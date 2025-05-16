package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Query Evolution Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    // Version 1
    public static final BuilderObject customer =        builder.object("Customer", 1);
    public static final BuilderObject customerId =      builder.object("id", 2);
    public static final BuilderObject name =            builder.object("name", 3);
    public static final BuilderObject surname =         builder.object("surname", 4);
    public static final BuilderObject knows =           builder.object("knows", 5);
    public static final BuilderObject order =           builder.object("Order", 6);
    public static final BuilderObject orderId =         builder.object("oid", 7);
    public static final BuilderObject street =          builder.object("street", 8);
    public static final BuilderObject city =            builder.object("city", 9);
    public static final BuilderObject postCode =        builder.object("postCode", 10);
    public static final BuilderObject orderPrice =      builder.object("price", 11);
    public static final BuilderObject quantity =        builder.object("quantity", 12);
    public static final BuilderObject product =         builder.object("Product", 13);
    public static final BuilderObject productId =       builder.object("pid", 14);
    public static final BuilderObject title =           builder.object("title", 15);
    public static final BuilderObject productPrice =    builder.object("price", 16);

    // Version 2
    public static final BuilderObject item =            builder.object("Item", 17);
    public static final BuilderObject ordered =         builder.object("Ordered", 18);

    // Signatures

    // Version 1
    public static final BuilderMorphism customerToCustomerId =  builder.morphism(customer, customerId, 1);
    public static final BuilderMorphism customerToName =        builder.morphism(customer, name, 2);
    public static final BuilderMorphism customerToSurname =     builder.morphism(customer, surname, 3);
    public static final BuilderMorphism knowsToCustomerA =      builder.morphism(knows, customer, 4);
    public static final BuilderMorphism knowsToCustomerB =      builder.morphism(knows, customer, 5);
    public static final BuilderMorphism orderToOrderId =        builder.morphism(order, orderId, 6);
    public static final BuilderMorphism orderToStreet =         builder.morphism(order, street, 7);
    public static final BuilderMorphism orderToCity =           builder.morphism(order, city, 8);
    public static final BuilderMorphism orderToPostCode =       builder.morphism(order, postCode, 9);
    public static final BuilderMorphism orderToOrderPrice =     builder.morphism(order, orderPrice, 10);
    public static final BuilderMorphism orderToQuantity =       builder.morphism(order, quantity, 11);
    public static final BuilderMorphism orderToCustomer =       builder.morphism(order, customer, 12);
    public static final BuilderMorphism orderToProduct =        builder.morphism(order, product, 13);
    public static final BuilderMorphism productToProductId =    builder.morphism(product, productId, 14);
    public static final BuilderMorphism productToTitle =        builder.min(Min.ZERO).morphism(product, title, 15);
    public static final BuilderMorphism productToProductPrice = builder.min(Min.ZERO).morphism(product, productPrice, 16);

    public static final BuilderMorphism orderToCustomerId =     builder.composite(orderToCustomer, customerToCustomerId);
    public static final BuilderMorphism orderToProductId =      builder.composite(orderToProduct, productToProductId);

    public static final Signature customerAToCustomerB = builder.concatenate(knowsToCustomerA.dual(), knowsToCustomerB);

    // Version 2
    public static final BuilderMorphism itemToOrderPrice =      builder.morphism(item, orderPrice, 10);
    public static final BuilderMorphism itemToQuantity =        builder.morphism(item, quantity, 11);
    public static final BuilderMorphism itemToProduct =         builder.morphism(item, product, 13);
    public static final BuilderMorphism itemToOrder =           builder.morphism(item, order, 17);

    public static final BuilderMorphism itemToProductId =       builder.composite(itemToProduct, productToProductId);
    public static final BuilderMorphism itemToTitle =           builder.composite(itemToProduct, productToTitle);
    public static final BuilderMorphism itemToProductPrice =    builder.composite(itemToProduct, productToProductPrice);
    public static final BuilderMorphism itemToOrderId =         builder.composite(itemToOrder, orderToOrderId);

    // The same key here is intentional - we want to replace the previous morphisms.
    public static final BuilderMorphism orderedToCustomer =     builder.morphism(ordered, customer, 12);
    public static final BuilderMorphism orderedToOrder =        builder.morphism(ordered, order, 18);

    public static final BuilderMorphism orderedToCustomerId =   builder.composite(orderedToCustomer, customerToCustomerId);
    public static final BuilderMorphism orderedToOrderId =      builder.composite(orderedToOrder, orderToOrderId);

    public static final Signature orderToCustomer2 = builder.concatenate(orderedToOrder.dual(), orderedToCustomer);

    // Ids

    static {

        builder
            .ids(customer, customerToCustomerId)
            .ids(knows, knowsToCustomerA, knowsToCustomerB)
            .ids(product, productToProductId)
            .ids(order, orderToOrderId, orderToProductId)
            .ids(item, itemToOrderId, itemToProductId)
            .ids(ordered, orderedToOrderId, orderedToCustomerId);

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
