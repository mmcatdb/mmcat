package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.instance.InstanceBuilder;
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

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber)
            )
        );
    }

    private static TestMapping addressInner(SchemaCategory schema, String kindName) {
        return new TestMapping(datasource, schema,
            Schema.order,
            kindName,
            b -> b.root(
                b.simple("number", Schema.orderToNumber),
                b.complex("address", Schema.orderToAddress,
                    b.simple("street", Schema.addressToStreet),
                    b.simple("city", Schema.addressToCity),
                    b.simple("zip", Schema.addressToZip)
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
        final var address = builder.valueObjex(Schema.address, uniqueId);
        builder.morphism(Schema.orderToAddress,
            builder.getRow(Schema.order, orderIndex),
            address
        );

        if (streetValue != null)
            builder.morphism(Schema.addressToStreet, address, builder.valueObjex(Schema.street, streetValue));

        if (cityValue != null)
            builder.morphism(Schema.addressToCity, address, builder.valueObjex(Schema.city, cityValue));

        if (zipValue != null)
            builder.morphism(Schema.addressToZip, address, builder.valueObjex(Schema.zip, zipValue));
    }

    public static TestMapping tag(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            tagKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber),
                b.simple("tags", Schema.tagToOrder.dual())
            )
        );
    }

    public static void addTag(InstanceBuilder builder, int orderIndex, String[] messageValues) {
        final var order = builder.getRow(Schema.order, orderIndex);

        for (String value : messageValues) {
            builder.morphism(Schema.tagToOrder,
                builder.valueObjex(Schema.tag, value),
                order
            );
        }
    }

    private static TestMapping createItem(SchemaCategory schema, String kindName) {
        return new TestMapping(datasource, schema,
            Schema.order,
            kindName,
            b -> b.root(
                b.simple("number", Schema.orderToNumber),
                b.complex("items", Schema.itemToOrder.dual(),
                    b.simple("id", Schema.itemToId),
                    b.simple("label", Schema.itemToLabel),
                    b.simple("price", Schema.itemToPrice),
                    b.simple("quantity", Schema.itemToQuantity)
                )
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return createItem(schema, itemKind);
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.values.getValue(Schema.orderToNumber.signature());

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.values.getValue(Schema.productToId.signature());

        final var item = builder.value(Schema.itemToNumber, numberValue).value(Schema.itemToId, idValue).objex(Schema.item);
        builder.morphism(Schema.itemToOrder, item, order);
        builder.morphism(Schema.itemToProduct, item, product);
        builder.morphism(Schema.itemToQuantity, item,
            builder.valueObjex(Schema.quantity, quantityValue)
        );
    }

    public static TestMapping itemEmpty(SchemaCategory schema) {
        return createItem(schema, itemEmptyKind);
    }

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            contactKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber),
                b.auxiliary("contact",
                    b.simple(Schema.orderToType, true, Schema.orderToValue)
                )
            )
        );
    }

    public static void addContact(InstanceBuilder builder, int orderIndex, String typeValue, String valueValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.values.getValue(Schema.orderToNumber.signature());

        final var contact = builder
            .value(Schema.contactToNumber, numberValue)
            .value(Schema.contactToType, typeValue)
            .objex(Schema.contact);

        builder.morphism(Schema.contactToOrder, contact, order);

        builder.morphism(Schema.contactToType, contact,
            builder.valueObjex(Schema.type, typeValue)
        );
        builder.morphism(Schema.contactToValue, contact,
            builder.valueObjex(Schema.value, valueValue)
        );
    }

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            customerKind,
            b -> b.root(
                b.auxiliary("customer",
                    b.simple("name", Schema.orderToName),
                    b.simple("number", Schema.orderToNumber)
                )
            )
        );
    }

    public static void addCustomer(InstanceBuilder builder, int orderIndex, String name) {
        final var order = builder.getRow(Schema.order, orderIndex);

        final var customer = builder.value(Schema.customerToName, name).objex(Schema.customer);
        builder.morphism(Schema.customerToName, customer,
            builder.valueObjex(Schema.name, name)
        );

        builder.morphism(Schema.orderToCustomer, order, customer);
    }

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            noteKind,
            b -> b.root(
                b.simple("number", Schema.orderToNumber),
                b.auxiliary("note",
                    b.complex(Schema.orderToLocale, true, Schema.orderToData,
                        b.simple("subject", Schema.dataToSubject),
                        b.simple("content", Schema.dataToContent)
                    )
                )
            )
        );
    }

    public static void addNote(InstanceBuilder builder, int orderIndex, String localeValue, String uniqueId, String subjectValue, String contentValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.values.getValue(Schema.orderToNumber.signature());

        final var note = builder
            .value(Schema.noteToNumber, numberValue)
            .value(Schema.noteToLocale, localeValue)
            .objex(Schema.note);

        builder.morphism(Schema.noteToOrder, note, order);
        builder.morphism(Schema.noteToLocale, note,
            builder.valueObjex(Schema.locale, localeValue)
        );

        final var data = builder.valueObjex(Schema.data, uniqueId);
        builder.morphism(Schema.noteToData, note, data);

        builder.morphism(Schema.dataToSubject, data, builder.valueObjex(Schema.subject, subjectValue));
        builder.morphism(Schema.dataToContent, data, builder.valueObjex(Schema.content, contentValue));
    }

}
