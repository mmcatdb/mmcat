package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String orderKind = "order";
    public static final String productKind = "product";
    public static final String itemKind = "order_item";
    public static final String dynamicKind = "dynamic";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String numberValue) {
        builder.morphism(Schema.orderToNumber,
            builder.value(Schema.orderToNumber, numberValue).objex(Schema.order),
            builder.valueObjex(Schema.number, numberValue)
        );
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
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
        final var product = builder.value(Schema.productToId, idValue).objex(Schema.product);

        if (idValue != null)
            builder.morphism(Schema.productToId, product, builder.valueObjex(Schema.id, idValue));

        if (labelValue != null)
            builder.morphism(Schema.productToLabel, product, builder.valueObjex(Schema.label, labelValue));

        if (priceValue != null)
            builder.morphism(Schema.productToPrice, product, builder.valueObjex(Schema.price, priceValue));
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
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
        final var numberValue = order.values.getValue(Schema.orderToNumber.signature());

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.values.getValue(Schema.productToId.signature());

        final var item = builder.value(Schema.itemToNumber, numberValue).value(Schema.itemToId, idValue).objex(Schema.item);
        builder.morphism(Schema.itemToOrder, item, order);
        builder.morphism(Schema.itemToProduct, item, product);
        builder.morphism(Schema.itemToQuantity, item, builder.valueObjex(Schema.quantity, quantityValue));
    }

    public static TestMapping dynamic(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.dynamic,
            dynamicKind,
            b -> b.root(
                b.simple("id", Schema.dynamicToId),
                b.simple("label", Schema.dynamicToLabel),
                b.simple(Schema.dynamicToPrefixType, "px_*", Schema.dynamicToPrefixValue),
                b.simple(Schema.dynamicToPrefiyType, "py_*", Schema.dynamicToPrefiyValue),
                b.simple(Schema.dynamicToCatchAllType, true, Schema.dynamicToCatchAllValue)
            )
        );
    }

    public static void addDynamic(InstanceBuilder builder, int index) {
        final var idValue =  "id-" + index;
        final var dynamic = builder.value(Schema.dynamicToId, idValue).objex(Schema.dynamic);
        builder.morphism(Schema.dynamicToId, dynamic, builder.valueObjex(Schema.dId, idValue));
        builder.morphism(Schema.dynamicToLabel, dynamic, builder.valueObjex(Schema.dLabel, "label-" + index));

        addPrefix(builder, index, "a");
        addPrefix(builder, index, "b");
        addPrefiy(builder, index, "a");
        addPrefiy(builder, index, "b");
        addCatchAll(builder, index, "a");
        addCatchAll(builder, index, "b");
    }

    private static void addPrefix(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var typeValue = "px_" + value;
        final var prefixValue = "px-" + value + "-" + index;
        final var prefix = builder
            .value(Schema.prefixToId, dynamic.values.getValue(Schema.dynamicToId.signature()))
            .value(Schema.prefixToType, typeValue)
            .objex(Schema.prefix);

        builder.morphism(Schema.prefixToType, prefix,
            builder.valueObjex(Schema.prefixType, typeValue)
        );
        builder.morphism(Schema.prefixToValue, prefix,
            builder.valueObjex(Schema.prefixValue, prefixValue)
        );
        builder.morphism(Schema.prefixToDynamic, prefix, dynamic);
    }

    private static void addPrefiy(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var typeValue = "py_" + value;
        final var prefiyValue = "py-" + value + "-" + index;
        final var prefiy = builder
            .value(Schema.prefiyToId, dynamic.values.getValue(Schema.dynamicToId.signature()))
            .value(Schema.prefiyToType, typeValue)
            .objex(Schema.prefiy);

        builder.morphism(Schema.prefiyToType, prefiy,
            builder.valueObjex(Schema.prefiyType, typeValue)
        );
        builder.morphism(Schema.prefiyToValue, prefiy,
            builder.valueObjex(Schema.prefiyValue, prefiyValue)
        );
        builder.morphism(Schema.prefiyToDynamic, prefiy, dynamic);
    }

    private static void addCatchAll(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var typeValue = "catch_all_" + value;
        final var catchAllValue = "catch-all-" + value + "-" + index;
        final var catchAll = builder
            .value(Schema.catchAllToId, dynamic.values.getValue(Schema.dynamicToId.signature()))
            .value(Schema.catchAllToType, typeValue)
            .objex(Schema.catchAll);

        builder.morphism(Schema.catchAllToType, catchAll,
            builder.valueObjex(Schema.catchAllType, typeValue)
        );
        builder.morphism(Schema.catchAllToValue, catchAll,
            builder.valueObjex(Schema.catchAllValue, catchAllValue)
        );
        builder.morphism(Schema.catchAllToDynamic, catchAll, dynamic);
    }

}
