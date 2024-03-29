package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.InstanceBuilder;
import cz.matfyz.tests.example.common.TestMapping;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final String orderKind = "order";
    public static final String productKind = "product";
    public static final String itemKind = "order_item";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String numberValue) {
        builder.morphism(Schema.orderToNumber,
            builder.value(Schema.orderToNumber, numberValue).object(Schema.order),
            builder.valueObject(numberValue, Schema.number)
        );
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.product,
            productKind,
            b -> b.root(
                b.simple("id", Schema.productToId),
                b.simple("label", Schema.productToLabel),
                b.simple("price", Schema.productToPrice)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, @Nullable String idValue, @Nullable String labelValue, @Nullable String priceValue) {
        final var product = builder.value(Schema.productToId, idValue).object(Schema.product);

        if (idValue != null)
            builder.morphism(Schema.productToId, product, builder.valueObject(idValue, Schema.id));

        if (labelValue != null)
            builder.morphism(Schema.productToLabel, product, builder.valueObject(labelValue, Schema.label));

        if (priceValue != null)
            builder.morphism(Schema.productToPrice, product, builder.valueObject(priceValue, Schema.price));
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("order_number", Schema.itemToNumber),
                b.simple("product_id", Schema.itemToId),
                b.simple("quantity", Schema.itemToQuantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.superId.getValue(Schema.orderToNumber.signature());

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.superId.getValue(Schema.productToId.signature());

        final var item = builder.value(Schema.itemToNumber, numberValue).value(Schema.itemToId, idValue).object(Schema.item);
        builder.morphism(Schema.itemToOrder, item, order);
        builder.morphism(Schema.itemToProduct, item, product);
        builder.morphism(Schema.itemToQuantity, item,
            builder.valueObject(quantityValue, Schema.quantity)
        );
    }

}
