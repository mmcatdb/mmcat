package cz.cuni.matfyz.tests.mapping;

import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.tests.instance.InstanceBuilder;
import cz.cuni.matfyz.tests.schema.TestSchema;

public abstract class MongoDB {

    public static final String orderKind = "order";
    public static final String itemKind = "orderItem";
    public static final String itemEmptyKind = "orderItemEmpty";

    public static TestMapping order(SchemaCategory schema) {
        return PostgreSQL.order(schema);
    }
    
    public static TestMapping address(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber),
                ComplexProperty.create("address", TestSchema.orderToAddress,
                    new SimpleProperty("street", TestSchema.addressToStreet),
                    new SimpleProperty("city", TestSchema.addressToCity),
                    new SimpleProperty("zip", TestSchema.addressToZip)
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addAddress(builder, 0, "0", "hodnotaA", "hodnotaB", "hodnotaC");
    //     addAddress(builder, 1, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
    // }

    public static void addAddress(InstanceBuilder builder, int orderIndex, String uniqueId, String streetValue, String cityValue, String zipValue) {
        final var address = builder.valueObject(uniqueId, TestSchema.address);
        builder.morphism(TestSchema.orderToAddress,
            builder.getRow(TestSchema.order, orderIndex),
            address
        );

        if (streetValue != null)
            builder.morphism(TestSchema.addressToStreet, address, builder.valueObject(streetValue, TestSchema.street));

        if (cityValue != null)
            builder.morphism(TestSchema.addressToCity, address, builder.valueObject(cityValue, TestSchema.city));

        if (zipValue != null)
            builder.morphism(TestSchema.addressToZip, address, builder.valueObject(zipValue, TestSchema.zip));
    }

    public static TestMapping tag(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber),
                new SimpleProperty("tags", TestSchema.tagToOrder.dual())
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addTag(builder, 0, new String[]{ "123", "456", "789" });
    //     addTag(builder, 1, new String[]{ "123", "String456", "String789" });
    // }

    public static void addTag(InstanceBuilder builder, int orderIndex, String[] messageValues) {
        final var order = builder.getRow(TestSchema.order, orderIndex);

        for (String value : messageValues) {
            builder.morphism(TestSchema.tagToOrder,
                builder.valueObject(value, TestSchema.tag),
                order
            );
        }
    }

    private static TestMapping createItem(SchemaCategory schema, String kindName) {
        return new TestMapping(schema,
            TestSchema.order,
            kindName,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber),
                ComplexProperty.create("items", TestSchema.itemToOrder.dual(),
                    new SimpleProperty("id", TestSchema.itemToId),
                    new SimpleProperty("label", TestSchema.itemToLabel),
                    new SimpleProperty("price", TestSchema.itemToPrice),
                    new SimpleProperty("quantity", TestSchema.itemToQuantity)
                )
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return createItem(schema, itemKind);
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addItem(builder, 0, 0, "1");
    //     addItem(builder, 0, 1, "2");
    //     addItem(builder, 1, 2, "7");
    //     addItem(builder, 1, 3, "3");
    // }

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

    public static TestMapping itemEmpty(SchemaCategory schema) {
        return createItem(schema, itemEmptyKind);
    }

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber),
                ComplexProperty.create("contact", TestSchema.contactToOrder.dual(),
                    new SimpleProperty(TestSchema.contactToType, TestSchema.contactToValue)
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addContact(builder, 0, "email", "anna@seznam.cz");
    //     addContact(builder, 0, "cellphone", "+420777123456");
    //     addContact(builder, 1, "skype", "skype123");
    //     addContact(builder, 1, "cellphone", "+420123456789");
    // }

    public static void addContact(InstanceBuilder builder, int orderIndex, String typeValue, String valueValue) {
        final var order = builder.getRow(TestSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(TestSchema.orderToNumber);

        final var contact = builder
            .value(TestSchema.contactToNumber, numberValue)
            .value(TestSchema.contactToType, typeValue)
            .value(TestSchema.contactToValue, valueValue).object(TestSchema.contact);

        builder.morphism(TestSchema.contactToOrder, contact, order);

        builder.morphism(TestSchema.contactToType, contact,
            builder.valueObject(typeValue, TestSchema.type)
        );
        builder.morphism(TestSchema.contactToValue, contact,
            builder.valueObject(valueValue, TestSchema.value)
        );
    }

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                ComplexProperty.createAuxiliary(new StaticName("customer"),
                    new SimpleProperty("name", TestSchema.orderToName),
                    new SimpleProperty("number", TestSchema.orderToNumber)
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addCustomer(builder, 0, "1");
    //     addCustomer(builder, 1, "1");
    // }

    public static void addCustomer(InstanceBuilder builder, int orderIndex, String name) {
        final var order = builder.getRow(TestSchema.order, orderIndex);

        final var customer = builder.value(TestSchema.customerToName, name).object(TestSchema.customer);
        builder.morphism(TestSchema.customerToName, customer,
            builder.valueObject(name, TestSchema.name)
        );

        builder.morphism(TestSchema.orderToCustomer, order, customer);
    }

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", TestSchema.orderToNumber),
                ComplexProperty.create("note", TestSchema.noteToOrder.dual(),
                    ComplexProperty.create(TestSchema.noteToLocale, TestSchema.noteToData,
                        new SimpleProperty("subject", TestSchema.dataToSubject),
                        new SimpleProperty("content", TestSchema.dataToContent)
                    )
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addNote(builder, 0, "cs-CZ", "0", "subject 1", "content cz");
    //     addNote(builder, 0, "en-US", "1", "subject 1", "content en");
    //     addNote(builder, 1, "cs-CZ", "3", "subject cz", "content 1");
    //     addNote(builder, 1, "en-GB", "2", "subject gb", "content 2");
    // }

    public static void addNote(InstanceBuilder builder, int orderIndex, String localeValue, String uniqueId, String subjectValue, String contentValue) {
        final var order = builder.getRow(TestSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(TestSchema.orderToNumber);

        final var note = builder
            .value(TestSchema.noteToNumber, numberValue)
            .value(TestSchema.noteToLocale, localeValue).object(TestSchema.note);

        builder.morphism(TestSchema.noteToOrder, note, order);
        builder.morphism(TestSchema.noteToLocale, note,
            builder.valueObject(localeValue, TestSchema.locale)
        );

        final var data = builder.valueObject(uniqueId, TestSchema.data);
        builder.morphism(TestSchema.noteToData, note, data);

        builder.morphism(TestSchema.dataToSubject, data, builder.valueObject(subjectValue, TestSchema.subject));
        builder.morphism(TestSchema.dataToContent, data, builder.valueObject(contentValue, TestSchema.content));
    }

}
