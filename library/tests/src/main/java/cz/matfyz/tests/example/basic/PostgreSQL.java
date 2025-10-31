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
                b.simple("number", Schema.order_number)
            )
        );
    }

    public static void addOrder(InstanceBuilder builder, String numberValue) {
        builder.value(Schema.order_number, numberValue).objex(Schema.order);
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.product,
            productKind,
            b -> b.root(
                b.simple("id", Schema.product_id),
                b.simple("label", Schema.product_label),
                b.simple("price", Schema.product_price)
            )
        );
    }

    public static void addProduct(InstanceBuilder builder, @Nullable String idValue, @Nullable String labelValue, @Nullable String priceValue) {
        builder
            .value(Schema.product_id, idValue)
            .value(Schema.product_label, labelValue)
            .value(Schema.product_price, priceValue)
            .objex(Schema.product);
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("order_number", Schema.item_number),
                b.simple("product_id", Schema.item_id),
                b.simple("quantity", Schema.item_quantity)
            )
        );
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.tryGetScalarValue(Schema.order_number.signature());

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.tryGetScalarValue(Schema.product_id.signature());

        final var item = builder
            .value(Schema.item_number, numberValue)
            .value(Schema.item_id, idValue)
            .value(Schema.item_quantity, quantityValue)
            .objex(Schema.item);
        builder.morphism(Schema.item_order, item, order);
        builder.morphism(Schema.item_product, item, product);
    }

    public static TestMapping dynamic(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.dynamic,
            dynamicKind,
            b -> b.root(
                b.simple("id", Schema.dynamic_id),
                b.simple("label", Schema.dynamic_label),
                b.dynamic(Schema.prefix_dynamic.dual(), "px_*", Schema.prefix_type, Schema.prefix_value),
                b.dynamic(Schema.prefiy_dynamic.dual(), "py_*", Schema.prefiy_type, Schema.prefiy_value),
                b.dynamic(Schema.catchAll_dynamic.dual(), null, Schema.catchAll_type, Schema.catchAll_value)
            )
        );
    }

    public static void addDynamic(InstanceBuilder builder, int index) {
        builder
            .value(Schema.dynamic_id, "id-" + index)
            .value(Schema.dynamic_label, "label-" + index)
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
            .value(Schema.prefix_id, dynamic.tryGetScalarValue(Schema.dynamic_id.signature()))
            .value(Schema.prefix_type, "px_" + value)
            .value(Schema.prefix_value, "px-" + value + "-" + index)
            .objex(Schema.prefix);

        builder.morphism(Schema.prefix_dynamic, prefix, dynamic);
    }

    private static void addPrefiy(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var prefiy = builder
            .value(Schema.prefiy_id, dynamic.tryGetScalarValue(Schema.dynamic_id.signature()))
            .value(Schema.prefiy_type, "py_" + value)
            .value(Schema.prefiy_value, "py-" + value + "-" + index)
            .objex(Schema.prefiy);

        builder.morphism(Schema.prefiy_dynamic, prefiy, dynamic);
    }

    private static void addCatchAll(InstanceBuilder builder, int index, String value) {
        final var dynamic = builder.getRow(Schema.dynamic, index);

        final var catchAll = builder
            .value(Schema.catchAll_id, dynamic.tryGetScalarValue(Schema.dynamic_id.signature()))
            .value(Schema.catchAll_type, "catch_all_" + value)
            .value(Schema.catchAll_value, "catch-all-" + value + "-" + index)
            .objex(Schema.catchAll);

        builder.morphism(Schema.catchAll_dynamic, catchAll, dynamic);
    }

}
