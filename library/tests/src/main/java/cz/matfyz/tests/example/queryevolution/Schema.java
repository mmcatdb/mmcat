package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Query Evolution Schema";

    
    // Keys
    
    // Version 1
    public static final Key customer = new Key(1);
    public static final Key customerId = new Key(2);
    public static final Key name = new Key(3);
    public static final Key surname = new Key(4);
    public static final Key knows = new Key(5);
    public static final Key order = new Key(6);
    public static final Key orderId = new Key(7);
    public static final Key street = new Key(8);
    public static final Key city = new Key(9);
    public static final Key postCode = new Key(10);
    public static final Key orderPrice = new Key(11);
    public static final Key quantity = new Key(12);
    public static final Key product = new Key(13);
    public static final Key productId = new Key(14);
    public static final Key title = new Key(15);
    public static final Key productPrice = new Key(16);

    // Version 2
    public static final Key item = new Key(17);
    public static final Key ordered = new Key(18);

    // Signatures
    
    // Version 1
    public static final BaseSignature customerToCustomerId = Signature.createBase(1);
    public static final BaseSignature customerToName = Signature.createBase(2);
    public static final BaseSignature customerToSurname = Signature.createBase(3);
    public static final BaseSignature knowsToCustomerA = Signature.createBase(4);
    public static final BaseSignature knowsToCustomerB = Signature.createBase(5);
    public static final BaseSignature orderToOrderId = Signature.createBase(6);
    public static final BaseSignature orderToStreet = Signature.createBase(7);
    public static final BaseSignature orderToCity = Signature.createBase(8);
    public static final BaseSignature orderToPostCode = Signature.createBase(9);
    public static final BaseSignature orderToOrderPrice = Signature.createBase(10);
    public static final BaseSignature orderToQuantity = Signature.createBase(11);
    public static final BaseSignature orderToCustomer = Signature.createBase(12);
    public static final BaseSignature orderToProduct = Signature.createBase(13);
    public static final BaseSignature productToProductId = Signature.createBase(14);
    public static final BaseSignature productToTitle = Signature.createBase(15);
    public static final BaseSignature productToProductPrice = Signature.createBase(16);
    
    public static final Signature orderToCustomerId = orderToCustomer.concatenate(customerToCustomerId);
    public static final Signature orderToProductId = orderToProduct.concatenate(productToProductId);
    
    // Version 2
    public static final BaseSignature itemToOrderPrice = Signature.createBase(10);
    public static final BaseSignature itemToQuantity = Signature.createBase(11);
    public static final BaseSignature itemToProduct = Signature.createBase(13);
    public static final BaseSignature itemToOrder = Signature.createBase(17);

    public static final Signature itemToProductId = itemToProduct.concatenate(productToProductId);
    public static final Signature itemToOrderId = itemToOrder.concatenate(orderToOrderId);

    public static final BaseSignature orderedToCustomer = Signature.createBase(12);
    public static final BaseSignature orderedToOrder = Signature.createBase(18);

    public static final Signature orderedToCustomerId = orderedToCustomer.concatenate(customerToCustomerId);
    public static final Signature orderedToOrderId = orderedToOrder.concatenate(orderToOrderId);

    public SchemaCategory build() {
        return builder.build(schemaLabel);
    }

    /**
     * Create new full schema category (version 1).
     */
    public static SchemaCategory newSchemaCategory1() {
        return new Schema()
            .addCustomer()
            .addProduct()
            .addOrder1()
            .build();
    }

    /**
     * Create new full schema category (version 2).
     */
    public static SchemaCategory newSchemaCategory2() {
        return new Schema()
            .addCustomer()
            .addProduct()
            .addOrder2()
            .addItem()
            .addOrdered()
            .build();
    }

    private final SchemaBuilder builder = new SchemaBuilder();

    private Schema addCustomer() {
        builder.object(customer, "Customer", new ObjectIds(customerToCustomerId));
        builder.object(customerId, "id", ObjectIds.createValue());
        builder.object(name, "name", ObjectIds.createValue());
        builder.object(surname, "surname", ObjectIds.createValue());
        builder.object(knows, "knows", new ObjectIds(knowsToCustomerA, knowsToCustomerB));

        builder.morphism(customerToCustomerId, customer, customerId, Min.ONE);
        builder.morphism(customerToName, customer, name, Min.ONE);
        builder.morphism(customerToSurname, customer, surname, Min.ONE);
        builder.morphism(knowsToCustomerA, knows, customer, Min.ONE);
        builder.morphism(knowsToCustomerB, knows, customer, Min.ONE);

        return this;
    }

    private Schema addProduct() {
        builder.object(product, "Product", new ObjectIds(productToProductId));
        builder.object(productId, "pid", ObjectIds.createValue());
        builder.object(title, "title", ObjectIds.createValue());
        builder.object(productPrice, "price", ObjectIds.createValue());

        builder.morphism(productToProductId, product, productId, Min.ONE);
        builder.morphism(productToTitle, product, title, Min.ZERO);
        builder.morphism(productToProductPrice, product, productPrice, Min.ZERO);

        return this;
    }

    private Schema addOrder1() {
        builder.object(order, "Order", new ObjectIds(orderToOrderId, orderToProductId));
        builder.object(orderId, "oid", ObjectIds.createValue());
        builder.object(street, "street", ObjectIds.createValue());
        builder.object(city, "city", ObjectIds.createValue());
        builder.object(postCode, "postCode", ObjectIds.createValue());
        builder.object(orderPrice, "price", ObjectIds.createValue());
        builder.object(quantity, "quantity", ObjectIds.createValue());

        builder.morphism(orderToOrderId, order, orderId, Min.ONE);
        builder.morphism(orderToStreet, order, street, Min.ONE);
        builder.morphism(orderToCity, order, city, Min.ONE);
        builder.morphism(orderToPostCode, order, postCode, Min.ONE);
        builder.morphism(orderToOrderPrice, order, orderPrice, Min.ONE);
        builder.morphism(orderToQuantity, order, quantity, Min.ONE);

        builder.morphism(orderToCustomer, order, customer, Min.ONE);
        builder.morphism(orderToProduct, order, product, Min.ONE);

        return this;
    }

    private Schema addOrder2() {
        builder.object(order, "Order", new ObjectIds(orderToOrderId));
        builder.object(orderId, "oid", ObjectIds.createValue());
        builder.object(street, "street", ObjectIds.createValue());
        builder.object(city, "city", ObjectIds.createValue());
        builder.object(postCode, "postCode", ObjectIds.createValue());

        builder.morphism(orderToOrderId, order, orderId, Min.ONE);
        builder.morphism(orderToStreet, order, street, Min.ONE);
        builder.morphism(orderToCity, order, city, Min.ONE);
        builder.morphism(orderToPostCode, order, postCode, Min.ONE);

        return this;
    }

    private Schema addItem() {
        builder.object(item, "Item", new ObjectIds(itemToOrderId, itemToProductId));
        builder.object(orderPrice, "price", ObjectIds.createValue());
        builder.object(quantity, "quantity", ObjectIds.createValue());

        builder.morphism(itemToOrderPrice, item, orderPrice, Min.ONE);
        builder.morphism(itemToQuantity, item, quantity, Min.ONE);

        builder.morphism(itemToOrder, item, order, Min.ONE);
        builder.morphism(itemToProduct, item, product, Min.ONE);

        return this;
    }

    private Schema addOrdered() {
        builder.object(ordered, "Ordered", new ObjectIds(orderedToOrderId, orderedToCustomerId));

        builder.morphism(orderedToOrder, ordered, order, Min.ONE);
        builder.morphism(orderedToCustomer, ordered, customer, Min.ONE);

        return this;
    }

}
