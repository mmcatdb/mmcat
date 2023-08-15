package cz.matfyz.tests.mapping;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.instance.InstanceBuilder;
import cz.matfyz.tests.schema.BasicSchema;

public abstract class MongoDB {

    public static final String orderKind = "order";
    public static final String itemKind = "orderItem";
    public static final String itemEmptyKind = "orderItemEmpty";

    public static TestMapping order(SchemaCategory schema) {
        return PostgreSQL.order(schema);
    }
    
    public static TestMapping address(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber),
                ComplexProperty.create("address", BasicSchema.orderToAddress,
                    new SimpleProperty("street", BasicSchema.addressToStreet),
                    new SimpleProperty("city", BasicSchema.addressToCity),
                    new SimpleProperty("zip", BasicSchema.addressToZip)
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addAddress(builder, 0, "0", "hodnotaA", "hodnotaB", "hodnotaC");
    //     addAddress(builder, 1, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
    // }

    public static void addAddress(InstanceBuilder builder, int orderIndex, String uniqueId, String streetValue, String cityValue, String zipValue) {
        final var address = builder.valueObject(uniqueId, BasicSchema.address);
        builder.morphism(BasicSchema.orderToAddress,
            builder.getRow(BasicSchema.order, orderIndex),
            address
        );

        if (streetValue != null)
            builder.morphism(BasicSchema.addressToStreet, address, builder.valueObject(streetValue, BasicSchema.street));

        if (cityValue != null)
            builder.morphism(BasicSchema.addressToCity, address, builder.valueObject(cityValue, BasicSchema.city));

        if (zipValue != null)
            builder.morphism(BasicSchema.addressToZip, address, builder.valueObject(zipValue, BasicSchema.zip));
    }

    public static TestMapping tag(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber),
                new SimpleProperty("tags", BasicSchema.tagToOrder.dual())
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addTag(builder, 0, new String[]{ "123", "456", "789" });
    //     addTag(builder, 1, new String[]{ "123", "String456", "String789" });
    // }

    public static void addTag(InstanceBuilder builder, int orderIndex, String[] messageValues) {
        final var order = builder.getRow(BasicSchema.order, orderIndex);

        for (String value : messageValues) {
            builder.morphism(BasicSchema.tagToOrder,
                builder.valueObject(value, BasicSchema.tag),
                order
            );
        }
    }

    private static TestMapping createItem(SchemaCategory schema, String kindName) {
        return new TestMapping(schema,
            BasicSchema.order,
            kindName,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber),
                ComplexProperty.create("items", BasicSchema.itemToOrder.dual(),
                    new SimpleProperty("id", BasicSchema.itemToId),
                    new SimpleProperty("label", BasicSchema.itemToLabel),
                    new SimpleProperty("price", BasicSchema.itemToPrice),
                    new SimpleProperty("quantity", BasicSchema.itemToQuantity)
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

    public static TestMapping itemEmpty(SchemaCategory schema) {
        return createItem(schema, itemEmptyKind);
    }

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber),
                ComplexProperty.create("contact", BasicSchema.contactToOrder.dual(),
                    new SimpleProperty(BasicSchema.contactToType, BasicSchema.contactToValue)
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
        final var order = builder.getRow(BasicSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(BasicSchema.orderToNumber);

        final var contact = builder
            .value(BasicSchema.contactToNumber, numberValue)
            .value(BasicSchema.contactToType, typeValue)
            .value(BasicSchema.contactToValue, valueValue).object(BasicSchema.contact);

        builder.morphism(BasicSchema.contactToOrder, contact, order);

        builder.morphism(BasicSchema.contactToType, contact,
            builder.valueObject(typeValue, BasicSchema.type)
        );
        builder.morphism(BasicSchema.contactToValue, contact,
            builder.valueObject(valueValue, BasicSchema.value)
        );
    }

    public static TestMapping customer(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                ComplexProperty.createAuxiliary(new StaticName("customer"),
                    new SimpleProperty("name", BasicSchema.orderToName),
                    new SimpleProperty("number", BasicSchema.orderToNumber)
                )
            )
        );
    }

    // static void addToInstance(InstanceBuilder builder) {
    //     addCustomer(builder, 0, "1");
    //     addCustomer(builder, 1, "1");
    // }

    public static void addCustomer(InstanceBuilder builder, int orderIndex, String name) {
        final var order = builder.getRow(BasicSchema.order, orderIndex);

        final var customer = builder.value(BasicSchema.customerToName, name).object(BasicSchema.customer);
        builder.morphism(BasicSchema.customerToName, customer,
            builder.valueObject(name, BasicSchema.name)
        );

        builder.morphism(BasicSchema.orderToCustomer, order, customer);
    }

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("number", BasicSchema.orderToNumber),
                ComplexProperty.create("note", BasicSchema.noteToOrder.dual(),
                    ComplexProperty.create(BasicSchema.noteToLocale, BasicSchema.noteToData,
                        new SimpleProperty("subject", BasicSchema.dataToSubject),
                        new SimpleProperty("content", BasicSchema.dataToContent)
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
        final var order = builder.getRow(BasicSchema.order, orderIndex);
        final var numberValue = order.superId.getValue(BasicSchema.orderToNumber);

        final var note = builder
            .value(BasicSchema.noteToNumber, numberValue)
            .value(BasicSchema.noteToLocale, localeValue).object(BasicSchema.note);

        builder.morphism(BasicSchema.noteToOrder, note, order);
        builder.morphism(BasicSchema.noteToLocale, note,
            builder.valueObject(localeValue, BasicSchema.locale)
        );

        final var data = builder.valueObject(uniqueId, BasicSchema.data);
        builder.morphism(BasicSchema.noteToData, note, data);

        builder.morphism(BasicSchema.dataToSubject, data, builder.valueObject(subjectValue, BasicSchema.subject));
        builder.morphism(BasicSchema.dataToContent, data, builder.valueObject(contentValue, BasicSchema.content));
    }

}
