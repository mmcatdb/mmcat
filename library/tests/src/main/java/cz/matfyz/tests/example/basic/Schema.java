package cz.matfyz.tests.example.basic;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SimpleBuilder;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SimpleBuilder.Object;
import cz.matfyz.tests.example.common.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Basic Schema";

    private static final SimpleBuilder builder2 = new SimpleBuilder(schemaLabel);

    // Keys

    public static final Key order = new Key(1);
    public static final Key number = new Key(2);
    public static final Key tag = new Key(3);

    public static final Key customer = new Key(4);
    public static final Key name = new Key(5);
    public static final Key friend = new Key(6);
    public static final Key since = new Key(7);

    public static final Key address = new Key(8);
    public static final Key street = new Key(9);
    public static final Key city = new Key(10);
    public static final Key zip = new Key(11);

    public static final Key item = new Key(12);
    public static final Key product = new Key(13);
    public static final Key quantity = new Key(14);
    public static final Key id = new Key(15);
    public static final Key label = new Key(16);
    public static final Key price = new Key(17);

    public static final Key contact = new Key(18);
    public static final Key value = new Key(19);
    public static final Key type = new Key(20);

    public static final Key note = new Key(21);
    public static final Key locale = new Key(22);
    public static final Key data = new Key(23);
    public static final Key subject = new Key(24);
    public static final SimpleBuilder.Object content = builder2.object("content", 25);

    // Signatures

    public static final BaseSignature orderToNumber = Signature.createBase(1);
    public static final BaseSignature tagToOrder = Signature.createBase(2);

    public static final BaseSignature orderToCustomer = Signature.createBase(3);
    public static final BaseSignature customerToName = Signature.createBase(4);
    public static final BaseSignature friendToCustomerA = Signature.createBase(5);
    public static final BaseSignature friendToCustomerB = Signature.createBase(6);
    public static final BaseSignature friendToSince = Signature.createBase(7);

    public static final Signature orderToName = orderToCustomer.concatenate(customerToName);
    public static final Signature friendToNameA = friendToCustomerA.concatenate(customerToName);
    public static final Signature friendToNameB = friendToCustomerB.concatenate(customerToName);

    public static final BaseSignature orderToAddress = Signature.createBase(8);
    public static final BaseSignature addressToStreet = Signature.createBase(9);
    public static final BaseSignature addressToCity = Signature.createBase(10);
    public static final BaseSignature addressToZip = Signature.createBase(11);

    public static final BaseSignature itemToOrder = Signature.createBase(12);
    public static final BaseSignature itemToProduct = Signature.createBase(13);
    public static final BaseSignature itemToQuantity = Signature.createBase(14);
    public static final BaseSignature productToId = Signature.createBase(15);
    public static final BaseSignature productToLabel = Signature.createBase(16);
    public static final BaseSignature productToPrice = Signature.createBase(17);

    public static final Signature itemToNumber = itemToOrder.concatenate(orderToNumber);
    public static final Signature itemToId = itemToProduct.concatenate(productToId);
    public static final Signature itemToLabel = itemToProduct.concatenate(productToLabel);
    public static final Signature itemToPrice = itemToProduct.concatenate(productToPrice);

    public static final BaseSignature contactToOrder = Signature.createBase(18);
    public static final BaseSignature contactToValue = Signature.createBase(19);
    public static final BaseSignature contactToType = Signature.createBase(20);

    public static final Signature contactToNumber = contactToOrder.concatenate(orderToNumber);

    public static final BaseSignature noteToOrder = Signature.createBase(21);
    public static final BaseSignature noteToLocale = Signature.createBase(22);
    public static final BaseSignature noteToData = Signature.createBase(23);
    public static final BaseSignature dataToSubject = Signature.createBase(24);
    public static final BaseSignature dataToContent = Signature.createBase(25);

    public static final Signature noteToNumber = noteToOrder.concatenate(orderToNumber);

    public Schema() {
        this.addOrder();
    }

    public SchemaCategory build() {
        return builder.build(schemaLabel);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchemaCategory() {
        return new Schema()
            .addCustomer()
            .addAddress()
            .addItem()
            .addContact()
            .addNote()
            .build();
    }

    private final SchemaBuilder builder = new SchemaBuilder();

    // This one is mandatory.
    private Schema addOrder() {
        builder.object(order, "order", new ObjectIds(orderToNumber));
        builder.object(number, "number", ObjectIds.createValue());
        builder.object(tag, "tag", ObjectIds.createValue());

        builder.morphism(orderToNumber, order, number, Min.ONE);
        builder.morphism(tagToOrder, tag, order, Min.ONE);

        return this;
    }

    public Schema addCustomer() {
        builder.object(customer, "customer", new ObjectIds(customerToName));
        builder.object(name, "name", ObjectIds.createValue());
        builder.object(friend, "friend", new ObjectIds(friendToCustomerA, friendToCustomerB));
        builder.object(since, "since", ObjectIds.createValue());

        builder.morphism(orderToCustomer, order, customer, Min.ONE);
        builder.morphism(customerToName, customer, name, Min.ONE);
        builder.morphism(friendToCustomerA, friend, customer, Min.ONE);
        builder.morphism(friendToCustomerB, friend, customer, Min.ONE);
        builder.morphism(friendToSince, friend, since, Min.ONE);

        return this;
    }

    public Schema addAddress() {
        builder.object(address, "address", ObjectIds.createGenerated());
        builder.object(street, "street", ObjectIds.createValue());
        builder.object(city, "city", ObjectIds.createValue());
        builder.object(zip, "zip", ObjectIds.createValue());

        builder.morphism(orderToAddress, order, address, Min.ONE);
        builder.morphism(addressToStreet, address, street, Min.ONE);
        builder.morphism(addressToCity, address, city, Min.ONE);
        builder.morphism(addressToZip, address, zip, Min.ONE);

        return this;
    }

    public Schema addItem() {
        builder.object(item, "item", new ObjectIds(itemToNumber, itemToId));
        builder.object(product, "product", new ObjectIds(productToId));
        builder.object(quantity, "quantity", ObjectIds.createValue());
        builder.object(id, "id", ObjectIds.createValue());
        builder.object(label, "label", ObjectIds.createValue());
        builder.object(price, "price", ObjectIds.createValue());

        builder.morphism(itemToOrder, item, order, Min.ONE, Tag.role);
        builder.morphism(itemToProduct, item, product, Min.ONE, Tag.role);
        builder.morphism(itemToQuantity, item, quantity, Min.ONE);
        builder.morphism(productToId, product, id, Min.ONE);
        builder.morphism(productToLabel, product, label, Min.ZERO);
        builder.morphism(productToPrice, product, price, Min.ZERO);

        return this;
    }

    public Schema addContact() {
        builder.object(contact, "contact", new ObjectIds(contactToNumber, contactToType, contactToValue));
        builder.object(type, "type", ObjectIds.createValue());
        builder.object(value, "value", ObjectIds.createValue());

        builder.morphism(contactToOrder, contact, order, Min.ONE);
        builder.morphism(contactToType, contact, type, Min.ONE, Tag.key);
        builder.morphism(contactToValue, contact, value, Min.ONE);

        return this;
    }

    public Schema addNote() {
        builder.object(note, "note", new ObjectIds(noteToNumber, noteToLocale));
        builder.object(locale, "locale", ObjectIds.createValue());
        builder.object(data, "data", ObjectIds.createGenerated());
        builder.object(subject, "subject", ObjectIds.createValue());
        builder.object(content.key(), "content", ObjectIds.createValue());

        builder.morphism(noteToOrder, note, order, Min.ONE);
        builder.morphism(noteToLocale, note, locale, Min.ONE, Tag.key);
        builder.morphism(noteToData, note, data, Min.ONE);
        builder.morphism(dataToSubject, data, subject, Min.ONE);
        builder.morphism(dataToContent, data, content.key(), Min.ONE);

        return this;
    }

}
