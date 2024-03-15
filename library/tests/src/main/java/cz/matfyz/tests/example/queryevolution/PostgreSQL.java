package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.InstanceBuilder;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final String customerKind = "customer";
    public static final String knowsKind = "knows";
    public static final String productKind = "product";

    public static final String ordersKind = "orders";
    public static final String orderKind = "order";
    public static final String orderedKind = "ordered";
    public static final String itemKind = "item";

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.customer,
            customerKind,
            b -> b.root(
                b.simple("id", Schema.customerToCustomerId),
                b.simple("name", Schema.customerToName),
                b.simple("surname", Schema.customerToSurname)
            )
        );
    }

    public static void addCustomer(InstanceBuilder builder, String customerIdValue, String nameValue, String surnameValue) {
        final var customer = builder.value(Schema.customerToCustomerId, customerIdValue).object(Schema.customer);

        builder.morphism(Schema.customerToCustomerId, customer, builder.valueObject(customerIdValue, Schema.customerId));
        builder.morphism(Schema.customerToName, customer, builder.valueObject(nameValue, Schema.name));
        builder.morphism(Schema.customerToSurname, customer, builder.valueObject(surnameValue, Schema.surname));
    }

    public static TestMapping knows(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.knows,
            knowsKind,
            b -> b.root(
                b.simple("id1", Schema.knowsToCustomerA),
                b.simple("id2", Schema.knowsToCustomerB)
            )
        );
    }

    public static void addKnows(InstanceBuilder builder, int customerAIndex, int customerBIndex) {
        final var customerA = builder.getRow(Schema.customer, customerAIndex);
        final var customerB = builder.getRow(Schema.customer, customerBIndex);

        final var knows = builder
            .value(Schema.knowsToCustomerA, customerA.superId.getValue(Schema.customerToCustomerId.signature()))
            .value(Schema.knowsToCustomerB, customerB.superId.getValue(Schema.customerToCustomerId.signature()))
            .object(Schema.knows);

        builder.morphism(Schema.knowsToCustomerA, knows, customerA);
        builder.morphism(Schema.knowsToCustomerB, knows, customerB);
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.product,
            productKind,
            b -> b.root(
                b.simple("pid", Schema.productToProductId),
                b.simple("title", Schema.productToTitle),
                b.simple("price", Schema.productToProductPrice)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, String productIdValue, String titleValue, String productPriceValue) {
        final var product = builder.value(Schema.productToProductId, productIdValue).object(Schema.product);

        builder.morphism(Schema.productToProductId, product, builder.valueObject(productIdValue, Schema.productId));
        builder.morphism(Schema.productToTitle, product, builder.valueObject(titleValue, Schema.title));
        builder.morphism(Schema.productToProductPrice, product, builder.valueObject(productPriceValue, Schema.productPrice));
    }

    public static TestMapping orders(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            ordersKind,
            b -> b.root(
                b.simple("id", Schema.orderToCustomerId),
                b.simple("pid", Schema.orderToProductId),
                b.simple("oid", Schema.orderToOrderId),
                b.simple("price", Schema.orderToOrderPrice),
                b.simple("quantity", Schema.orderToQuantity),
                b.simple("street", Schema.orderToStreet),
                b.simple("city", Schema.orderToCity),
                b.simple("postCode", Schema.orderToPostCode)
            )
        );
    }

    public static void addOrders(InstanceBuilder builder, int customerIndex, int productIndex, String orderIdValue, String orderPriceValue, String quantityValue, String streetValue, String cityValue, String postCodeValue) {
        final var customer = builder.getRow(Schema.customer, customerIndex);
        final var product = builder.getRow(Schema.product, productIndex);

        final var order = builder
            .value(Schema.orderToOrderId, orderIdValue)
            .value(Schema.orderToProductId, product.superId.getValue(Schema.productToProductId.signature()))
            .object(Schema.order);

        builder.morphism(Schema.orderToCustomer, order, customer);
        builder.morphism(Schema.orderToProduct, order, product);

        builder.morphism(Schema.orderToOrderPrice, order, builder.valueObject(orderPriceValue, Schema.orderPrice));
        builder.morphism(Schema.orderToQuantity, order, builder.valueObject(quantityValue, Schema.quantity));
        builder.morphism(Schema.orderToStreet, order, builder.valueObject(streetValue, Schema.street));
        builder.morphism(Schema.orderToCity, order, builder.valueObject(cityValue, Schema.city));
        builder.morphism(Schema.orderToPostCode, order, builder.valueObject(postCodeValue, Schema.postCode));
    }

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("oid", Schema.orderToOrderId),
                b.simple("street", Schema.orderToStreet),
                b.simple("city", Schema.orderToCity),
                b.simple("postCode", Schema.orderToPostCode)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String orderIdValue, String streetValue, String cityValue, String postCodeValue) {
        final var order = builder.value(Schema.orderToOrderId, orderIdValue).object(Schema.order);

        builder.morphism(Schema.orderToStreet, order, builder.valueObject(streetValue, Schema.street));
        builder.morphism(Schema.orderToCity, order, builder.valueObject(cityValue, Schema.city));
        builder.morphism(Schema.orderToPostCode, order, builder.valueObject(postCodeValue, Schema.postCode));
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("pid", Schema.itemToProductId),
                b.simple("oid", Schema.itemToOrderId),
                b.simple("price", Schema.itemToOrderPrice),
                b.simple("quantity", Schema.itemToQuantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int productIndex, int orderIndex, String orderPriceValue, String quantityValue) {
        final var product = builder.getRow(Schema.product, productIndex);
        final var order = builder.getRow(Schema.order, orderIndex);

        final var item = builder
            .value(Schema.itemToProductId, product.superId.getValue(Schema.productToProductId.signature()))
            .value(Schema.itemToOrderId, order.superId.getValue(Schema.orderToOrderId.signature()))
            .object(Schema.item);

        builder.morphism(Schema.itemToProduct, item, product);
        builder.morphism(Schema.itemToOrder, item, order);

        builder.morphism(Schema.itemToOrderPrice, item, builder.valueObject(orderPriceValue, Schema.orderPrice));
        builder.morphism(Schema.itemToQuantity, item, builder.valueObject(quantityValue, Schema.quantity));
    }

    public static TestMapping ordered(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.ordered,
            orderedKind,
            b -> b.root(
                b.simple("id", Schema.orderToCustomerId),
                b.simple("oid", Schema.orderedToOrderId)
            )
        );
    }

    public static void addOrdered(InstanceBuilder builder, int customerIndex, int orderIndex) {
        final var customer = builder.getRow(Schema.customer, customerIndex);
        final var order = builder.getRow(Schema.order, orderIndex);

        final var ordered = builder
            .value(Schema.orderedToCustomerId, customer.superId.getValue(Schema.customerToCustomerId.signature()))
            .value(Schema.orderedToOrderId, order.superId.getValue(Schema.orderToOrderId.signature()))
            .object(Schema.ordered);

        builder.morphism(Schema.orderedToCustomer, ordered, customer);
        builder.morphism(Schema.orderedToOrder, ordered, order);
    }

}
