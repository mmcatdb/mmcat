package cz.matfyz.tests.mapping;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.instance.InstanceBuilder;
import cz.matfyz.tests.schema.TestSchema;

public abstract class PostgreSQL {

    public static final String orderKind = "order";
    public static final String productKind = "product";
    public static final String itemKind = "order_item";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String numberValue) {
        builder.morphism(TestSchema.orderToNumber,
            builder.value(TestSchema.orderToNumber, numberValue).object(TestSchema.order),
            builder.valueObject(numberValue, TestSchema.number)
        );
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.product,
            productKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("id", TestSchema.productToId),
                new SimpleProperty("label", TestSchema.productToLabel),
                new SimpleProperty("price", TestSchema.productToPrice)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, String idValue, String labelValue, String priceValue) {
        final var product = builder.value(TestSchema.productToId, idValue).object(TestSchema.product);
        
        if (idValue != null)
            builder.morphism(TestSchema.productToId, product, builder.valueObject(idValue, TestSchema.id));

        if (labelValue != null)
            builder.morphism(TestSchema.productToLabel, product, builder.valueObject(labelValue, TestSchema.label));

        if (priceValue != null)
            builder.morphism(TestSchema.productToPrice, product, builder.valueObject(priceValue, TestSchema.price));
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.item,
            itemKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("order_number", TestSchema.itemToNumber),
                new SimpleProperty("product_id", TestSchema.itemToId),
                new SimpleProperty("quantity", TestSchema.itemToQuantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(TestSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(TestSchema.orderToNumber);

        final var product = builder.getRow(TestSchema.product, productIndex);
        final var idValue = product.superId.getValue(TestSchema.productToId);

        final var item = builder.value(TestSchema.itemToNumber, numberValue).value(TestSchema.itemToId, idValue).object(TestSchema.item);
        builder.morphism(TestSchema.itemToOrder, item, order);
        builder.morphism(TestSchema.itemToProduct, item, product);
        builder.morphism(TestSchema.itemToQuantity, item,
            builder.valueObject(quantityValue, TestSchema.quantity)
        );
    }

}
