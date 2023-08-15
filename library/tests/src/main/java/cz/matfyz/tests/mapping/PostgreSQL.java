package cz.matfyz.tests.mapping;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.instance.InstanceBuilder;
import cz.matfyz.tests.schema.BasicSchema;

public abstract class PostgreSQL {

    public static final String orderKind = "order";
    public static final String productKind = "product";
    public static final String itemKind = "order_item";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String numberValue) {
        builder.morphism(BasicSchema.orderToNumber,
            builder.value(BasicSchema.orderToNumber, numberValue).object(BasicSchema.order),
            builder.valueObject(numberValue, BasicSchema.number)
        );
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.product,
            productKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("id", BasicSchema.productToId),
                new SimpleProperty("label", BasicSchema.productToLabel),
                new SimpleProperty("price", BasicSchema.productToPrice)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, String idValue, String labelValue, String priceValue) {
        final var product = builder.value(BasicSchema.productToId, idValue).object(BasicSchema.product);
        
        if (idValue != null)
            builder.morphism(BasicSchema.productToId, product, builder.valueObject(idValue, BasicSchema.id));

        if (labelValue != null)
            builder.morphism(BasicSchema.productToLabel, product, builder.valueObject(labelValue, BasicSchema.label));

        if (priceValue != null)
            builder.morphism(BasicSchema.productToPrice, product, builder.valueObject(priceValue, BasicSchema.price));
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.item,
            itemKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("order_number", BasicSchema.itemToNumber),
                new SimpleProperty("product_id", BasicSchema.itemToId),
                new SimpleProperty("quantity", BasicSchema.itemToQuantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(BasicSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(BasicSchema.orderToNumber);

        final var product = builder.getRow(BasicSchema.product, productIndex);
        final var idValue = product.superId.getValue(BasicSchema.productToId);

        final var item = builder.value(BasicSchema.itemToNumber, numberValue).value(BasicSchema.itemToId, idValue).object(BasicSchema.item);
        builder.morphism(BasicSchema.itemToOrder, item, order);
        builder.morphism(BasicSchema.itemToProduct, item, product);
        builder.morphism(BasicSchema.itemToQuantity, item,
            builder.valueObject(quantityValue, BasicSchema.quantity)
        );
    }

}
