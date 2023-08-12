package cz.cuni.matfyz.tests.schema;

import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaCategory;

public class TestSchema {

    public static final String schemaLabel = "basicExample";

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
    public static final Key content = new Key(25);

    // Signatures

    public static final Signature orderToNumber = Signature.createBase(1);
    public static final Signature tagToOrder = Signature.createBase(2);

    public static final Signature orderToCustomer = Signature.createBase(3);
    public static final Signature customerToName = Signature.createBase(4);
    public static final Signature friendToCustomerA = Signature.createBase(5);
    public static final Signature friendToCustomerB = Signature.createBase(6);
    public static final Signature friendToSince = Signature.createBase(7);

    public static final Signature orderToName = orderToCustomer.concatenate(customerToName);
    public static final Signature friendToNameA = friendToCustomerA.concatenate(customerToName);
    public static final Signature friendToNameB = friendToCustomerB.concatenate(customerToName);

    public static final Signature orderToAddress = Signature.createBase(8);
    public static final Signature addressToStreet = Signature.createBase(9);
    public static final Signature addressToCity = Signature.createBase(10);
    public static final Signature addressToZip = Signature.createBase(11);

    public static final Signature itemToOrder = Signature.createBase(12);
    public static final Signature itemToProduct = Signature.createBase(13);
    public static final Signature itemToQuantity = Signature.createBase(14);
    public static final Signature productToId = Signature.createBase(15);
    public static final Signature productToLabel = Signature.createBase(16);
    public static final Signature productToPrice = Signature.createBase(17);

    public static final Signature itemToNumber = itemToOrder.concatenate(orderToNumber);
    public static final Signature itemToId = itemToProduct.concatenate(productToId);
    public static final Signature itemToLabel = itemToProduct.concatenate(productToLabel);
    public static final Signature itemToPrice = itemToProduct.concatenate(productToPrice);

    public static final Signature contactToOrder = Signature.createBase(18);
    public static final Signature contactToValue = Signature.createBase(19);
    public static final Signature contactToType = Signature.createBase(20);

    public static final Signature contactToNumber = contactToOrder.concatenate(orderToNumber);

    public static final Signature noteToOrder = Signature.createBase(21);
    public static final Signature noteToLocale = Signature.createBase(22);
    public static final Signature noteToData = Signature.createBase(23);
    public static final Signature dataToSubject = Signature.createBase(24);
    public static final Signature dataToContent = Signature.createBase(25);

    public static final Signature noteToNumber = noteToOrder.concatenate(orderToNumber);
    
    public TestSchema() {
        this.addOrder();
    }

    public SchemaCategory build() {
        return builder.build(schemaLabel);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchemaCategory() {
        return new TestSchema()
            .addCustomer()
            .addAddress()
            .addItem()
            .addContact()
            .addNote()
            .build();
    }

    private final SchemaBuilder builder = new SchemaBuilder();

    // This one is mandatory.
    private TestSchema addOrder() {
        builder.object(order, "order", new ObjectIds(orderToNumber));
        builder.object(number, "number", ObjectIds.createValue());
        builder.object(tag, "tag", ObjectIds.createValue());

        builder.morphism(orderToNumber, order, number, Min.ONE);
        builder.morphism(tagToOrder, tag, order, Min.ONE);

        return this;
    }

    public TestSchema addCustomer() {
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

    public TestSchema addAddress() {
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

    public TestSchema addItem() {
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

    public TestSchema addContact() {
        builder.object(contact, "contact", new ObjectIds(contactToNumber, contactToType, contactToValue));
        builder.object(type, "type", ObjectIds.createValue());
        builder.object(value, "value", ObjectIds.createValue());

        builder.morphism(contactToOrder, contact, order, Min.ONE);
        builder.morphism(contactToValue, contact, value, Min.ONE);
        builder.morphism(contactToType, contact, type, Min.ONE, Tag.key);

        return this;
    }

    public TestSchema addNote() {
        builder.object(note, "note", new ObjectIds(noteToNumber, noteToLocale));
        builder.object(locale, "locale", ObjectIds.createValue());
        builder.object(data, "data", ObjectIds.createGenerated());
        builder.object(subject, "subject", ObjectIds.createValue());
        builder.object(content, "content", ObjectIds.createValue());

        builder.morphism(noteToOrder, note, order, Min.ONE);
        builder.morphism(noteToLocale, note, locale, Min.ONE);
        builder.morphism(noteToData, note, data, Min.ONE);
        builder.morphism(dataToSubject, data, subject, Min.ONE, Tag.key);
        builder.morphism(dataToContent, data, content, Min.ONE, Tag.key);

        return this;
    }

}
