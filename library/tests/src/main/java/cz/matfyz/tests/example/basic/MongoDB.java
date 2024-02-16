package cz.matfyz.tests.example.basic;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.InstanceBuilder;
import cz.matfyz.tests.example.common.TestMapping;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class MongoDB {

    private MongoDB() {}

    public static final String orderKind = "order";
    public static final String addressKind = "address";
    public static final String tagKind = "tag";
    public static final String itemKind = "orderItem";
    public static final String itemEmptyKind = "orderItemEmpty";
    public static final String contactKind = "contact";
    public static final String customerKind = "customer";
    public static final String noteKind = "note";

    public static TestMapping order(SchemaCategory schema) {
        return PostgreSQL.order(schema);
    }
    
    public static TestMapping address(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            addressKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", Schema.orderToNumber),
                ComplexProperty.create("address", Schema.orderToAddress,
                    new SimpleProperty("street", Schema.addressToStreet),
                    new SimpleProperty("city", Schema.addressToCity),
                    new SimpleProperty("zip", Schema.addressToZip)
                )
            )
        );
    }

    public static void addAddress(InstanceBuilder builder, int orderIndex, String uniqueId, @Nullable String streetValue, @Nullable String cityValue, @Nullable String zipValue) {
        final var address = builder.valueObject(uniqueId, Schema.address);
        builder.morphism(Schema.orderToAddress,
            builder.getRow(Schema.order, orderIndex),
            address
        );

        if (streetValue != null)
            builder.morphism(Schema.addressToStreet, address, builder.valueObject(streetValue, Schema.street));

        if (cityValue != null)
            builder.morphism(Schema.addressToCity, address, builder.valueObject(cityValue, Schema.city));

        if (zipValue != null)
            builder.morphism(Schema.addressToZip, address, builder.valueObject(zipValue, Schema.zip));
    }

    public static TestMapping tag(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            tagKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", Schema.orderToNumber),
                new SimpleProperty("tags", Schema.tagToOrder.dual())
            )
        );
    }

    public static void addTag(InstanceBuilder builder, int orderIndex, String[] messageValues) {
        final var order = builder.getRow(Schema.order, orderIndex);

        for (String value : messageValues) {
            builder.morphism(Schema.tagToOrder,
                builder.valueObject(value, Schema.tag),
                order
            );
        }
    }

    private static TestMapping createItem(SchemaCategory schema, String kindName) {
        return new TestMapping(schema,
            Schema.order,
            kindName,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", Schema.orderToNumber),
                ComplexProperty.create("items", Schema.itemToOrder.dual(),
                    new SimpleProperty("id", Schema.itemToId),
                    new SimpleProperty("label", Schema.itemToLabel),
                    new SimpleProperty("price", Schema.itemToPrice),
                    new SimpleProperty("quantity", Schema.itemToQuantity)
                )
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return createItem(schema, itemKind);
    }

    public static void addItem(InstanceBuilder builder, int orderIndex, int productIndex, String quantityValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.superId.getValue(Schema.orderToNumber);

        final var product = builder.getRow(Schema.product, productIndex);
        final var idValue = product.superId.getValue(Schema.productToId);

        final var item = builder.value(Schema.itemToNumber, numberValue).value(Schema.itemToId, idValue).object(Schema.item);
        builder.morphism(Schema.itemToOrder, item, order);
        builder.morphism(Schema.itemToProduct, item, product);
        builder.morphism(Schema.itemToQuantity, item,
            builder.valueObject(quantityValue, Schema.quantity)
        );
    }

    public static TestMapping itemEmpty(SchemaCategory schema) {
        return createItem(schema, itemEmptyKind);
    }

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            contactKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", Schema.orderToNumber),
                ComplexProperty.create("contact", Schema.contactToOrder.dual(),
                    new SimpleProperty(Schema.contactToType, Schema.contactToValue)
                )
            )
        );
    }

    public static void addContact(InstanceBuilder builder, int orderIndex, String typeValue, String valueValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.superId.getValue(Schema.orderToNumber);

        final var contact = builder
            .value(Schema.contactToNumber, numberValue)
            .value(Schema.contactToType, typeValue)
            .value(Schema.contactToValue, valueValue).object(Schema.contact);

        builder.morphism(Schema.contactToOrder, contact, order);

        builder.morphism(Schema.contactToType, contact,
            builder.valueObject(typeValue, Schema.type)
        );
        builder.morphism(Schema.contactToValue, contact,
            builder.valueObject(valueValue, Schema.value)
        );
    }

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            customerKind,
            () -> ComplexProperty.createRoot(
                ComplexProperty.createAuxiliary(new StaticName("customer"),
                    new SimpleProperty("name", Schema.orderToName),
                    new SimpleProperty("number", Schema.orderToNumber)
                )
            )
        );
    }

    public static void addCustomer(InstanceBuilder builder, int orderIndex, String name) {
        final var order = builder.getRow(Schema.order, orderIndex);

        final var customer = builder.value(Schema.customerToName, name).object(Schema.customer);
        builder.morphism(Schema.customerToName, customer,
            builder.valueObject(name, Schema.name)
        );

        builder.morphism(Schema.orderToCustomer, order, customer);
    }

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            noteKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", Schema.orderToNumber),
                ComplexProperty.create("note", Schema.noteToOrder.dual(),
                    ComplexProperty.create(Schema.noteToLocale, Schema.noteToData,
                        new SimpleProperty("subject", Schema.dataToSubject),
                        new SimpleProperty("content", Schema.dataToContent)
                    )
                )
            )
        );
    }

    public static void addNote(InstanceBuilder builder, int orderIndex, String localeValue, String uniqueId, String subjectValue, String contentValue) {
        final var order = builder.getRow(Schema.order, orderIndex);
        final var numberValue = order.superId.getValue(Schema.orderToNumber);

        final var note = builder
            .value(Schema.noteToNumber, numberValue)
            .value(Schema.noteToLocale, localeValue).object(Schema.note);

        builder.morphism(Schema.noteToOrder, note, order);
        builder.morphism(Schema.noteToLocale, note,
            builder.valueObject(localeValue, Schema.locale)
        );

        final var data = builder.valueObject(uniqueId, Schema.data);
        builder.morphism(Schema.noteToData, note, data);

        builder.morphism(Schema.dataToSubject, data, builder.valueObject(subjectValue, Schema.subject));
        builder.morphism(Schema.dataToContent, data, builder.valueObject(contentValue, Schema.content));
    }

}
