package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

import java.util.Arrays;

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
        final var address = builder
            .generatedId(uniqueId)
            .value(Schema.addressToStreet, streetValue)
            .value(Schema.addressToCity, cityValue)
            .value(Schema.addressToZip, zipValue)
            .objex(Schema.address);

        builder.morphism(Schema.orderToAddress, builder.getRow(Schema.order, orderIndex), address);
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

    public static void addTag(InstanceBuilder builder, int orderIndex, String ...tagValues) {
        final var order = builder.getRow(Schema.order, orderIndex);
        order.addArrayValues(Schema.tagToOrder.dual(), Arrays.asList(tagValues));
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
        final var numberValue = order.tryGetScalarValue(Schema.orderToNumber.signature());

        final var contact = builder
            .value(Schema.contactToNumber, numberValue)
            .value(Schema.contactToType, typeValue)
            .value(Schema.contactToValue, valueValue)
            .objex(Schema.contact);

        builder.morphism(Schema.contactToOrder, contact, order);
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
        final var numberValue = order.tryGetScalarValue(Schema.orderToNumber.signature());

        final var note = builder
            .value(Schema.noteToNumber, numberValue)
            .value(Schema.noteToLocale, localeValue)
            .objex(Schema.note);

        builder.morphism(Schema.noteToOrder, note, order);

        final var data = builder
            .generatedId(uniqueId)
            .value(Schema.dataToSubject, subjectValue)
            .value(Schema.dataToContent, contentValue)
            .objex(Schema.data);

        builder.morphism(Schema.noteToData, note, data);
    }

}
