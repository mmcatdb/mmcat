package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String customerKind = "customer";
    public static final String knowsKind = "knows";
    public static final String productKind = "product";

    public static final String ordersKind = "orders";
    public static final String orderKind = "order";
    public static final String orderedKind = "ordered";
    public static final String itemKind = "item";

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.customer,
            customerKind,
            b -> b.root(
                b.simple("id", Schema.customer_customerId),
                b.simple("name", Schema.customer_name),
                b.simple("surname", Schema.customer_surname)
            )
        );
    }

    public static void addCustomer(InstanceBuilder builder, String idValue, String nameValue, String surnameValue) {
        builder
            .value(Schema.customer_customerId, idValue)
            .value(Schema.customer_name, nameValue)
            .value(Schema.customer_surname, surnameValue)
            .objex(Schema.customer);
    }

    public static TestMapping knows(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.knows,
            knowsKind,
            b -> b.root(
                b.simple("id1", Schema.knows_customerA),
                b.simple("id2", Schema.knows_customerB)
            )
        );
    }

    public static void addKnows(InstanceBuilder builder, int customerAIndex, int customerBIndex) {
        final var customerA = builder.getRow(Schema.customer, customerAIndex);
        final var customerB = builder.getRow(Schema.customer, customerBIndex);

        final var knows = builder
            .value(Schema.knows_customerA, customerA.tryGetScalarValue(Schema.customer_customerId.signature()))
            .value(Schema.knows_customerB, customerB.tryGetScalarValue(Schema.customer_customerId.signature()))
            .objex(Schema.knows);

        builder.morphism(Schema.knows_customerA, knows, customerA);
        builder.morphism(Schema.knows_customerB, knows, customerB);
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.product,
            productKind,
            b -> b.root(
                b.simple("pid", Schema.product_productId),
                b.simple("title", Schema.product_title),
                b.simple("price", Schema.product_productPrice)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, String idValue, String titleValue, String priceValue) {
        builder
            .value(Schema.product_productId, idValue)
            .value(Schema.product_title, titleValue)
            .value(Schema.product_productPrice, priceValue)
            .objex(Schema.product);
    }

    public static TestMapping orders(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            ordersKind,
            b -> b.root(
                b.simple("id", Schema.order_customerId),
                b.simple("pid", Schema.order_productId),
                b.simple("oid", Schema.order_orderId),
                b.simple("price", Schema.order_orderPrice),
                b.simple("quantity", Schema.order_quantity),
                b.simple("street", Schema.order_street),
                b.simple("city", Schema.order_city),
                b.simple("postCode", Schema.order_postCode)
            )
        );
    }

    public static void addOrders(InstanceBuilder builder, int customerIndex, int productIndex, String orderIdValue, String orderPriceValue, String quantityValue, String streetValue, String cityValue, String postCodeValue) {
        final var customer = builder.getRow(Schema.customer, customerIndex);
        final var product = builder.getRow(Schema.product, productIndex);

        final var order = builder
            .value(Schema.order_orderId, orderIdValue)
            .value(Schema.order_productId, product.tryGetScalarValue(Schema.product_productId.signature()))
            .value(Schema.order_orderPrice, orderPriceValue)
            .value(Schema.order_quantity, quantityValue)
            .value(Schema.order_street, streetValue)
            .value(Schema.order_city, cityValue)
            .value(Schema.order_postCode, postCodeValue)
            .objex(Schema.order);

        builder.morphism(Schema.order_customer, order, customer);
        builder.morphism(Schema.order_product, order, product);
    }

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("oid", Schema.order_orderId),
                b.simple("street", Schema.order_street),
                b.simple("city", Schema.order_city),
                b.simple("postCode", Schema.order_postCode)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String orderIdValue, String streetValue, String cityValue, String postCodeValue) {
        builder
            .value(Schema.order_orderId, orderIdValue)
            .value(Schema.order_city, streetValue)
            .value(Schema.order_postCode, postCodeValue)
            .objex(Schema.order);
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("pid", Schema.item_productId),
                b.simple("oid", Schema.item_orderId),
                b.simple("price", Schema.item_orderPrice),
                b.simple("quantity", Schema.item_quantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int productIndex, int orderIndex, String orderPriceValue, String quantityValue) {
        final var product = builder.getRow(Schema.product, productIndex);
        final var order = builder.getRow(Schema.order, orderIndex);

        final var item = builder
            .value(Schema.item_productId, product.tryGetScalarValue(Schema.product_productId.signature()))
            .value(Schema.item_orderId, order.tryGetScalarValue(Schema.order_orderId.signature()))
            .value(Schema.item_orderPrice, orderPriceValue)
            .value(Schema.item_quantity, quantityValue)
            .objex(Schema.item);

        builder.morphism(Schema.item_product, item, product);
        builder.morphism(Schema.item_order, item, order);
    }

    public static TestMapping ordered(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.ordered,
            orderedKind,
            b -> b.root(
                b.simple("id", Schema.order_customerId),
                b.simple("oid", Schema.ordered_orderId)
            )
        );
    }

    public static void addOrdered(InstanceBuilder builder, int customerIndex, int orderIndex) {
        final var customer = builder.getRow(Schema.customer, customerIndex);
        final var order = builder.getRow(Schema.order, orderIndex);

        final var ordered = builder
            .value(Schema.ordered_customerId, customer.tryGetScalarValue(Schema.customer_customerId.signature()))
            .value(Schema.ordered_orderId, order.tryGetScalarValue(Schema.order_orderId.signature()))
            .objex(Schema.ordered);

        builder.morphism(Schema.ordered_customer, ordered, customer);
        builder.morphism(Schema.ordered_order, ordered, order);
    }

}
