package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;

public abstract class Schema {

    public static final String schemaLabel = "Basic Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObject order =       builder.object("order", 1);
    public static final BuilderObject number =      builder.object("number", 2);
    public static final BuilderObject tag =         builder.object("tag", 3);

    public static final BuilderObject customer =    builder.object("customer", 4);
    public static final BuilderObject name =        builder.object("name", 5);
    public static final BuilderObject friend =      builder.object("friend", 6);
    public static final BuilderObject since =       builder.object("since", 7);

    public static final BuilderObject address =     builder.generatedIds().object("address", 8);
    public static final BuilderObject street =      builder.object("street", 9);
    public static final BuilderObject city =        builder.object("city", 10);
    public static final BuilderObject zip =         builder.object("zip", 11);

    public static final BuilderObject item =        builder.object("item", 12);
    public static final BuilderObject product =     builder.object("product", 13);
    public static final BuilderObject quantity =    builder.object("quantity", 14);
    public static final BuilderObject id =          builder.object("id", 15);
    public static final BuilderObject label =       builder.object("label", 16);
    public static final BuilderObject price =       builder.object("price", 17);

    public static final BuilderObject contact =     builder.object("contact", 18);
    public static final BuilderObject value =       builder.object("value", 19);
    public static final BuilderObject type =        builder.object("type", 20);

    public static final BuilderObject note =        builder.object("note", 21);
    public static final BuilderObject locale =      builder.object("locale", 22);
    public static final BuilderObject data =        builder.generatedIds().object("data", 23);
    public static final BuilderObject subject =     builder.object("subject", 24);
    public static final BuilderObject content =     builder.object("content", 25);

    // Morphisms

    public static final BuilderMorphism orderToNumber =     builder.morphism(order, number, 1);
    public static final BuilderMorphism tagToOrder =        builder.morphism(tag, order, 2);

    public static final BuilderMorphism orderToCustomer =   builder.morphism(order, customer, 3);
    public static final BuilderMorphism customerToName =    builder.morphism(customer, name, 4);
    public static final BuilderMorphism friendToCustomerA = builder.morphism(friend, customer, 5);
    public static final BuilderMorphism friendToCustomerB = builder.morphism(friend, customer, 6);
    public static final BuilderMorphism friendToSince =     builder.morphism(friend, since, 7);

    public static final BuilderMorphism orderToName =       builder.composite(orderToCustomer, customerToName);
    public static final BuilderMorphism friendToNameA =     builder.composite(friendToCustomerA, customerToName);
    public static final BuilderMorphism friendToNameB =     builder.composite(friendToCustomerB, customerToName);

    public static final BuilderMorphism orderToAddress =    builder.morphism(order, address, 8);
    public static final BuilderMorphism addressToStreet =   builder.morphism(address, street, 9);
    public static final BuilderMorphism addressToCity =     builder.morphism(address, city, 10);
    public static final BuilderMorphism addressToZip =      builder.morphism(address, zip, 11);

    public static final BuilderMorphism itemToOrder =       builder.tags(Tag.role).morphism(item, order, 12);
    public static final BuilderMorphism itemToProduct =     builder.tags(Tag.role).morphism(item, product, 13);
    public static final BuilderMorphism itemToQuantity =    builder.morphism(item, quantity, 14);
    public static final BuilderMorphism productToId =       builder.morphism(product, id, 15);
    public static final BuilderMorphism productToLabel =    builder.min(Min.ZERO).morphism(product, label, 16);
    public static final BuilderMorphism productToPrice =    builder.min(Min.ZERO).morphism(product, price, 17);

    public static final BuilderMorphism itemToNumber =      builder.composite(itemToOrder, orderToNumber);
    public static final BuilderMorphism itemToId =          builder.composite(itemToProduct, productToId);
    public static final BuilderMorphism itemToLabel =       builder.composite(itemToProduct, productToLabel);
    public static final BuilderMorphism itemToPrice =       builder.composite(itemToProduct, productToPrice);

    public static final BuilderMorphism contactToOrder =    builder.morphism(contact, order, 18);
    public static final BuilderMorphism contactToValue =    builder.morphism(contact, value, 19);
    public static final BuilderMorphism contactToType =     builder.morphism(contact, type, 20);

    public static final BuilderMorphism contactToNumber =   builder.composite(contactToOrder, orderToNumber);

    public static final BuilderMorphism noteToOrder =       builder.morphism(note, order, 21);
    public static final BuilderMorphism noteToLocale =      builder.morphism(note, locale, 22);
    public static final BuilderMorphism noteToData =        builder.morphism(note, data, 23);
    public static final BuilderMorphism dataToSubject =     builder.morphism(data, subject, 24);
    public static final BuilderMorphism dataToContent =     builder.morphism(data, content, 25);

    public static final BuilderMorphism noteToNumber =      builder.composite(noteToOrder, orderToNumber);

    // Ids

    private static final SchemaBuilder ids = builder
        .ids(order, orderToNumber)
        .ids(customer, customerToName)
        .ids(friend, friendToCustomerA, friendToCustomerB)
        .ids(item, itemToNumber, itemToId)
        .ids(product, productToId)
        .ids(contact, contactToNumber, contactToType, contactToValue)
        .ids(note, noteToNumber, noteToLocale);

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

    private Schema() {}

}
