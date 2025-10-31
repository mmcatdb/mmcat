package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.mapping.Name.IndexName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String orderKind = "order";
    public static final String addressKind = "address";
    public static final String addressMissingSimpleKind = "addressMissingSimple";
    public static final String addressMissingComplexKind = "addressMissingComplex";
    public static final String tagKind = "tag";
    public static final String itemKind = "orderItem";
    public static final String itemEmptyKind = "orderItemEmpty";
    public static final String contactKind = "contact";
    public static final String customerKind = "customer";
    public static final String noteKind = "note";
    public static final String hardcoreKind = "hardcore";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("number", Schema.order_number)
            )
        );
    }

    private static TestMapping addressInner(SchemaCategory schema, String kindName) {
        return new TestMapping(datasource, schema,
            Schema.order,
            kindName,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.complex("address", Schema.order_address,
                    b.simple("street", Schema.address_street),
                    b.simple("city", Schema.address_city),
                    b.simple("zip", Schema.address_zip)
                )
            )
        );
    }

    public static TestMapping address(SchemaCategory schema) {
        return addressInner(schema, addressKind);
    }

    public static TestMapping addressMissingSimple(SchemaCategory schema) {
        return addressInner(schema, addressMissingSimpleKind);
    }

    public static TestMapping addressMissingComplex(SchemaCategory schema) {
        return addressInner(schema, addressMissingComplexKind);
    }

    public static void addAddress(InstanceBuilder builder, int orderIndex, String uniqueId, @Nullable String streetValue, @Nullable String cityValue, @Nullable String zipValue) {
        final var address = builder
            .generatedId(uniqueId)
            .value(Schema.address_street, streetValue)
            .value(Schema.address_city, cityValue)
            .value(Schema.address_zip, zipValue)
            .objex(Schema.address);

        builder.morphism(Schema.order_address, builder.getRow(Schema.order, orderIndex), address);
    }

    public static TestMapping tagSet(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            tagKind,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.simple("tags", Schema.order_tag)
            )
        );
    }

    public static void addTagSet(InstanceBuilder builder, int orderIndex, String ...tagValues) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.tryGetScalarValue(Schema.order_number.signature());

        for (final var tagValue : tagValues) {
            final var tags = builder
                .value(Schema.tags_number, numberValue)
                .value(Schema.tags_tag, tagValue)
                .objex(Schema.tags);

            builder.morphism(Schema.tags_order, tags, order);
        }
    }

    public static TestMapping tagArray(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            tagKind,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.indexed("tags", Schema.tags_order.dual(), Schema.tags_index, Schema.tags_tag)
            )
        );
    }

    public static void addTagArray(InstanceBuilder builder, int orderIndex, String ...tagValues) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.tryGetScalarValue(Schema.order_number.signature());

        for (int i = 0; i < tagValues.length; i++) {
            final var tags = builder
                .value(Schema.tags_number, numberValue)
                .value(Schema.tags_index, String.valueOf(i))
                .value(Schema.tags_tag, tagValues[i])
                .objex(Schema.tags);

            builder.morphism(Schema.tags_order, tags, order);
        }
    }

    private static TestMapping createItem(SchemaCategory schema, String kindName) {
        return new TestMapping(datasource, schema,
            Schema.order,
            kindName,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.complex("items", Schema.item_order.dual(),
                    b.simple("id", Schema.item_id),
                    b.simple("label", Schema.item_label),
                    b.simple("price", Schema.item_price),
                    b.simple("quantity", Schema.item_quantity)
                )
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return createItem(schema, itemKind);
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

    public static TestMapping itemEmpty(SchemaCategory schema) {
        return createItem(schema, itemEmptyKind);
    }

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            contactKind,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.auxiliary("contact",
                    b.dynamic(Schema.contact_order.dual(), null, Schema.contact_type, Schema.contact_value)
                )
            )
        );
    }

    public static void addContact(InstanceBuilder builder, int orderIndex, String typeValue, String valueValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.tryGetScalarValue(Schema.order_number.signature());

        final var contact = builder
            .value(Schema.contact_number, numberValue)
            .value(Schema.contact_type, typeValue)
            .value(Schema.contact_value, valueValue)
            .objex(Schema.contact);

        builder.morphism(Schema.contact_order, contact, order);
    }

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            customerKind,
            b -> b.root(
                b.auxiliary("customer",
                    b.simple("name", Schema.order_name),
                    b.simple("number", Schema.order_number)
                )
            )
        );
    }

    public static void addCustomer(InstanceBuilder builder, int orderIndex, String name) {
        final var order = builder.getRow(Schema.order, orderIndex);

        final var customer = builder.value(Schema.customer_name, name).objex(Schema.customer);

        builder.morphism(Schema.order_customer, order, customer);
    }

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            noteKind,
            b -> b.root(
                b.simple("number", Schema.order_number),
                b.auxiliary("note",
                    b.dynamic(Schema.note_order.dual(), null, Schema.note_locale, Schema.note_data,
                        b.simple("subject", Schema.data_subject),
                        b.simple("content", Schema.data_content)
                    )
                )
            )
        );
    }

    public static void addNote(InstanceBuilder builder, int orderIndex, String localeValue, String uniqueId, String subjectValue, String contentValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.tryGetScalarValue(Schema.order_number.signature());

        final var note = builder
            .value(Schema.note_number, numberValue)
            .value(Schema.note_locale, localeValue)
            .objex(Schema.note);

        builder.morphism(Schema.note_order, note, order);

        final var data = builder
            .generatedId(uniqueId)
            .value(Schema.data_subject, subjectValue)
            .value(Schema.data_content, contentValue)
            .objex(Schema.data);

        builder.morphism(Schema.note_data, note, data);
    }

    public static TestMapping hardcore(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.hardcore,
            "hardcore",
            b -> b.root(
                b.simple("id", Schema.hardcore_id),
                b.dynamic(Schema.map_hardcore.dual(), null, Schema.map_key, Schema.array2D_map.dual(),
                    b.simple(new IndexName(0), Schema.array2D_index1),
                    b.simple(new IndexName(1), Schema.array2D_index2),
                    b.complex(new TypedName(TypedName.VALUE), Schema.array1D_array2D.dual(),
                        b.simple(new IndexName(0), Schema.array1D_index3),
                        b.simple(new TypedName(TypedName.VALUE), Schema.array1D_simple)
                    )
                ),
                b.indexed("array", Schema.array_hardcore.dual(), Schema.array_index4, Schema.array_complex,
                    b.simple("id", Schema.complex_cId),
                    b.dynamic(Schema.cMap_complex.dual(), null, Schema.cMap_cKey, Signature.empty(),
                        b.simple("i", Schema.cMap_cValueI),
                        b.simple("j", Schema.cMap_cValueJ)
                    )
                )
            )
        );
    }

    public static void addHardcore(InstanceBuilder builder) {
        final var hardcore1 = builder
            .value(Schema.hardcore_id, "h_1")
            .objex(Schema.hardcore);

        addMap(builder, hardcore1);

        final var hardcore2 = builder
            .value(Schema.hardcore_id, "h_2")
            .objex(Schema.hardcore);

        addArray(builder, hardcore2);
    }

    private static void addMap(InstanceBuilder builder, DomainRow hardcore) {
        final var keys = new String[] { "a", "b" };
        for (final var key : keys) {
            final var map = builder
                .value(Schema.map_id, hardcore.tryGetScalarValue(Schema.hardcore_id.signature()))
                .value(Schema.map_key, key)
                .objex(Schema.map);

            builder.morphism(Schema.map_hardcore, map, hardcore);
            addArray2D(builder, map, key);
        }
    }

    private static void addArray2D(InstanceBuilder builder, DomainRow map, String key) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                final var array2D = builder
                    .generatedId()
                    .value(Schema.array2D_index1, String.valueOf(x))
                    .value(Schema.array2D_index2, String.valueOf(y))
                    .objex(Schema.array2D);

                builder.morphism(Schema.array2D_map, array2D, map);
                addArray1D(builder, array2D, key, x, y);
            }
        }
    }

    private static void addArray1D(InstanceBuilder builder, DomainRow array2D, String key, int x, int y) {
        final var valuePrefix = "v_" + key + "-" + x + "" + y + "-";

        for (int i = 0; i < 2; i++) {
            final var array1D = builder
                .generatedId()
                .value(Schema.array1D_index3, String.valueOf(i))
                .value(Schema.array1D_simple, valuePrefix + i)
                .objex(Schema.array1D);

            builder.morphism(Schema.array1D_array2D, array1D, array2D);
        }
    }

    private static void addArray(InstanceBuilder builder, DomainRow hardcore) {
        for (int i = 0; i < 2; i++) {
            final var array = builder
                .value(Schema.array_id, hardcore.tryGetScalarValue(Schema.hardcore_id.signature()))
                .value(Schema.array_index4, String.valueOf(i))
                .objex(Schema.array);

            builder.morphism(Schema.array_hardcore, array, hardcore);

            final var complex = builder
                .value(Schema.complex_cId, "c_" + i)
                .objex(Schema.complex);

            builder.morphism(Schema.array_complex, array, complex);
            addCMap(builder, complex, i);
        }
    }

    private static void addCMap(InstanceBuilder builder, DomainRow complex, int index) {
        final var id = "c_" + index;
        final var keys = new String[] { "x", "y" };

        for (final var key : keys) {
            final var valuePrefix = "v_" + index + "-" + key + "-";
            final var cMap = builder
                .value(Schema.cMap_cId, id)
                .value(Schema.cMap_cKey, key)
                .value(Schema.cMap_cValueI, valuePrefix + "i")
                .value(Schema.cMap_cValueJ, valuePrefix + "j")
                .objex(Schema.cMap);

            builder.morphism(Schema.cMap_complex, cMap, complex);
        }

    }

}
