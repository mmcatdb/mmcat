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
        builder.value(Schema.orderToNumber, numberValue).objex(Schema.order);
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
        builder
            .value(Schema.productToId, idValue)
            .value(Schema.productToLabel, labelValue)
            .value(Schema.productToPrice, priceValue)
            .objex(Schema.product);
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
        final var numberValue = order.tryGetScalarValue(Schema.orderToNumber.signature());

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.tryGetScalarValue(Schema.productToId.signature());

        final var item = builder
            .value(Schema.itemToNumber, numberValue)
            .value(Schema.itemToId, idValue)
            .value(Schema.itemToQuantity, quantityValue)
            .objex(Schema.item);
        builder.morphism(Schema.itemToOrder, item, order);
        builder.morphism(Schema.itemToProduct, item, product);
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
        builder
            .value(Schema.dynamicToId, "id-" + index)
            .value(Schema.dynamicToLabel, "label-" + index)
            .objex(Schema.dynamic);

        addPrefix(builder, index, "a");
        addPrefix(builder, index, "b");
        addPrefiy(builder, index, "a");
        addPrefiy(builder, index, "b");
        addCatchAll(builder, index, "a");
        addCatchAll(builder, index, "b");
    }

    private static void addPrefix(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var prefix = builder
            .value(Schema.prefixToId, dynamic.tryGetScalarValue(Schema.dynamicToId.signature()))
            .value(Schema.prefixToType, "px_" + value)
            .value(Schema.prefixToValue, "px-" + value + "-" + index)
            .objex(Schema.prefix);

        builder.morphism(Schema.prefixToDynamic, prefix, dynamic);
    }

    private static void addPrefiy(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var prefiy = builder
            .value(Schema.prefiyToId, dynamic.tryGetScalarValue(Schema.dynamicToId.signature()))
            .value(Schema.prefiyToType, "py_" + value)
            .value(Schema.prefiyToValue, "py-" + value + "-" + index)
            .objex(Schema.prefiy);

        builder.morphism(Schema.prefiyToDynamic, prefiy, dynamic);
    }

    private static void addCatchAll(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var catchAll = builder
            .value(Schema.catchAllToId, dynamic.tryGetScalarValue(Schema.dynamicToId.signature()))
            .value(Schema.catchAllToType, "catch_all_" + value)
            .value(Schema.catchAllToValue, "catch-all-" + value + "-" + index)
            .objex(Schema.catchAll);

        builder.morphism(Schema.catchAllToDynamic, catchAll, dynamic);
    }

}
