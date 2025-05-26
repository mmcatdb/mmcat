package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;

public abstract class Schema {

    public static final String schemaLabel = "Basic Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObjex order =           builder.objex("order", 1);
    public static final BuilderObjex number =          builder.objex("number", 2);
    public static final BuilderObjex tag =             builder.objex("tag", 3);

    public static final BuilderObjex customer =        builder.objex("customer", 4);
    public static final BuilderObjex name =            builder.objex("name", 5);
    public static final BuilderObjex friend =          builder.objex("friend", 6);
    public static final BuilderObjex since =           builder.objex("since", 7);

    public static final BuilderObjex address =         builder.generatedIds().objex("address", 8);
    public static final BuilderObjex street =          builder.objex("street", 9);
    public static final BuilderObjex city =            builder.objex("city", 10);
    public static final BuilderObjex zip =             builder.objex("zip", 11);

    public static final BuilderObjex item =            builder.objex("item", 12);
    public static final BuilderObjex product =         builder.objex("product", 13);
    public static final BuilderObjex quantity =        builder.objex("quantity", 14);
    public static final BuilderObjex id =              builder.objex("id", 15);
    public static final BuilderObjex label =           builder.objex("label", 16);
    public static final BuilderObjex price =           builder.objex("price", 17);

    public static final BuilderObjex contact =         builder.objex("contact", 18);
    public static final BuilderObjex value =           builder.objex("value", 19);
    public static final BuilderObjex type =            builder.objex("type", 20);

    public static final BuilderObjex note =            builder.objex("note", 21);
    public static final BuilderObjex locale =          builder.objex("locale", 22);
    public static final BuilderObjex data =            builder.generatedIds().objex("data", 23);
    public static final BuilderObjex subject =         builder.objex("subject", 24);
    public static final BuilderObjex content =         builder.objex("content", 25);

    public static final BuilderObjex dynamic =         builder.objex("dynamic", 26);
    public static final BuilderObjex dId =             builder.objex("id", 27);
    public static final BuilderObjex dLabel =          builder.objex("label", 28);
    public static final BuilderObjex prefix =          builder.objex("prefix", 29);
    public static final BuilderObjex prefixType =      builder.objex("type-x", 30);
    public static final BuilderObjex prefixValue =     builder.objex("type-x", 31);
    public static final BuilderObjex prefiy =          builder.objex("prefiy", 32);
    public static final BuilderObjex prefiyType =      builder.objex("type-y", 33);
    public static final BuilderObjex prefiyValue =     builder.objex("type-y", 34);
    public static final BuilderObjex catchAll =        builder.objex("catch-all", 35);
    public static final BuilderObjex catchAllType =    builder.objex("catch-all-type", 36);
    public static final BuilderObjex catchAllValue =   builder.objex("catch-all-value", 37);

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
    public static final Signature orderToValue =            contactToOrder.dual().concatenate(contactToValue.signature());
    public static final Signature orderToType =             contactToOrder.dual().concatenate(contactToType.signature());

    public static final BuilderMorphism noteToOrder =       builder.morphism(note, order, 21);
    public static final BuilderMorphism noteToLocale =      builder.morphism(note, locale, 22);
    public static final BuilderMorphism noteToData =        builder.morphism(note, data, 23);
    public static final BuilderMorphism dataToSubject =     builder.morphism(data, subject, 24);
    public static final BuilderMorphism dataToContent =     builder.morphism(data, content, 25);

    public static final BuilderMorphism noteToNumber =      builder.composite(noteToOrder, orderToNumber);
    public static final Signature orderToLocale =           noteToOrder.dual().concatenate(noteToLocale.signature());
    public static final Signature orderToData =             noteToOrder.dual().concatenate(noteToData.signature());


    public static final BuilderMorphism dynamicToId =       builder.morphism(dynamic, dId, 26);
    public static final BuilderMorphism dynamicToLabel =    builder.morphism(dynamic, dLabel, 27);
    public static final BuilderMorphism prefixToDynamic =   builder.morphism(prefix, dynamic, 28);
    public static final BuilderMorphism prefixToType =      builder.morphism(prefix, prefixType, 29);
    public static final BuilderMorphism prefixToValue =     builder.morphism(prefix, prefixValue, 30);
    public static final BuilderMorphism prefiyToDynamic =   builder.morphism(prefiy, dynamic, 31);
    public static final BuilderMorphism prefiyToType =      builder.morphism(prefiy, prefiyType, 32);
    public static final BuilderMorphism prefiyToValue =     builder.morphism(prefiy, prefiyValue, 33);
    public static final BuilderMorphism catchAllToDynamic = builder.morphism(catchAll, dynamic, 34);
    public static final BuilderMorphism catchAllToType =    builder.morphism(catchAll, catchAllType, 35);
    public static final BuilderMorphism catchAllToValue =   builder.morphism(catchAll, catchAllValue, 36);

    public static final BuilderMorphism prefixToId =        builder.composite(prefixToDynamic, dynamicToId);
    public static final Signature dynamicToPrefixType =     prefixToDynamic.dual().concatenate(prefixToType.signature());
    public static final Signature dynamicToPrefixValue =    prefixToDynamic.dual().concatenate(prefixToValue.signature());

    public static final BuilderMorphism prefiyToId =        builder.composite(prefiyToDynamic, dynamicToId);
    public static final Signature dynamicToPrefiyType =     prefiyToDynamic.dual().concatenate(prefiyToType.signature());
    public static final Signature dynamicToPrefiyValue =    prefiyToDynamic.dual().concatenate(prefiyToValue.signature());

    public static final BuilderMorphism catchAllToId =      builder.composite(catchAllToDynamic, dynamicToId);
    public static final Signature dynamicToCatchAllType =   catchAllToDynamic.dual().concatenate(catchAllToType.signature());
    public static final Signature dynamicToCatchAllValue =  catchAllToDynamic.dual().concatenate(catchAllToValue.signature());

    // Ids

    static {

        builder
            .ids(order, orderToNumber)
            .ids(customer, customerToName)
            .ids(friend, friendToCustomerA, friendToCustomerB)
            .ids(item, itemToNumber, itemToId)
            .ids(product, productToId)
            .ids(contact, contactToNumber, contactToType)
            .ids(note, noteToNumber, noteToLocale)
            .ids(dynamic, dynamicToId)
            .ids(prefix, prefixToId, prefixToType)
            .ids(prefiy, prefiyToId, prefiyToType)
            .ids(catchAll, catchAllToId, catchAllToType);

    }

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
