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
    public static final BuilderObjex tags =            builder.objex("tags", 3);
    public static final BuilderObjex tag =             builder.objex("tag", 4);
    public static final BuilderObjex index =           builder.objex("index", 5);

    public static final BuilderObjex customer =        builder.objex("customer", 6);
    public static final BuilderObjex name =            builder.objex("name", 7);
    public static final BuilderObjex friend =          builder.objex("friend", 8);
    public static final BuilderObjex since =           builder.objex("since", 9);

    public static final BuilderObjex address =         builder.generatedIds().objex("address", 10);
    public static final BuilderObjex street =          builder.objex("street", 11);
    public static final BuilderObjex city =            builder.objex("city", 12);
    public static final BuilderObjex zip =             builder.objex("zip", 13);

    public static final BuilderObjex item =            builder.objex("item", 14);
    public static final BuilderObjex product =         builder.objex("product", 15);
    public static final BuilderObjex quantity =        builder.objex("quantity", 16);
    public static final BuilderObjex id =              builder.objex("id", 17);
    public static final BuilderObjex label =           builder.objex("label", 18);
    public static final BuilderObjex price =           builder.objex("price", 19);

    public static final BuilderObjex contact =         builder.objex("contact", 20);
    public static final BuilderObjex value =           builder.objex("value", 21);
    public static final BuilderObjex type =            builder.objex("type", 22);

    public static final BuilderObjex note =            builder.objex("note", 23);
    public static final BuilderObjex locale =          builder.objex("locale", 24);
    public static final BuilderObjex data =            builder.generatedIds().objex("data", 25);
    public static final BuilderObjex subject =         builder.objex("subject", 26);
    public static final BuilderObjex content =         builder.objex("content", 27);

    public static final BuilderObjex dynamic =         builder.objex("dynamic", 28);
    public static final BuilderObjex dId =             builder.objex("id", 29);
    public static final BuilderObjex dLabel =          builder.objex("label", 30);
    public static final BuilderObjex prefix =          builder.objex("prefix", 31);
    public static final BuilderObjex prefixType =      builder.objex("type-x", 32);
    public static final BuilderObjex prefixValue =     builder.objex("type-x", 33);
    public static final BuilderObjex prefiy =          builder.objex("prefiy", 34);
    public static final BuilderObjex prefiyType =      builder.objex("type-y", 35);
    public static final BuilderObjex prefiyValue =     builder.objex("type-y", 36);
    public static final BuilderObjex catchAll =        builder.objex("catch-all", 37);
    public static final BuilderObjex catchAllType =    builder.objex("catch-all-type", 38);
    public static final BuilderObjex catchAllValue =   builder.objex("catch-all-value", 39);

    // Morphisms

    public static final BuilderMorphism orderToNumber =             builder.morphism(order, number, 1);
    // FIXME remove this from everywhere
    // public static final BuilderMorphism tagToOrder =                builder.morphism(tag, order, 2);

    // FIXME rename all morphisms
    // TODO use a different numbering (e.g., 1, 2, 3, 11, 12, 13, 21, ...) to allow easier insertions later.
    public static final BuilderMorphism tagsToOrder =               builder.tags(Tag.role).morphism(tags, order, 101);
    public static final BuilderMorphism tagsToTag =                 builder.tags(Tag.role).morphism(tags, tag, 102);
    public static final BuilderMorphism tagsToIndex =               builder.tags(Tag.role).morphism(tags, index, 103);
    public static final Signature       orderToTag =                builder.concatenate(tagsToOrder.dual(), tagsToTag);
    public static final Signature       orderToIndex =              builder.concatenate(tagsToOrder.dual(), tagsToIndex);
    public static final Signature       tagsToNumber =              builder.concatenate(tagsToOrder, orderToNumber);

    public static final BuilderMorphism orderToCustomer =           builder.morphism(order, customer, 3);
    public static final BuilderMorphism customerToName =            builder.morphism(customer, name, 4);
    public static final BuilderMorphism friendToCustomerA =         builder.morphism(friend, customer, 5);
    public static final BuilderMorphism friendToCustomerB =         builder.morphism(friend, customer, 6);
    public static final BuilderMorphism friendToSince =             builder.morphism(friend, since, 7);

    public static final Signature       orderToName =               builder.concatenate(orderToCustomer, customerToName);
    public static final Signature       friendToNameA =             builder.concatenate(friendToCustomerA, customerToName);
    public static final Signature       friendToNameB =             builder.concatenate(friendToCustomerB, customerToName);

    public static final BuilderMorphism orderToAddress =            builder.morphism(order, address, 8);
    public static final BuilderMorphism addressToStreet =           builder.morphism(address, street, 9);
    public static final BuilderMorphism addressToCity =             builder.morphism(address, city, 10);
    public static final BuilderMorphism addressToZip =              builder.morphism(address, zip, 11);

    public static final BuilderMorphism itemToOrder =               builder.tags(Tag.role).morphism(item, order, 12);
    public static final BuilderMorphism itemToProduct =             builder.tags(Tag.role).morphism(item, product, 13);
    public static final BuilderMorphism itemToQuantity =            builder.morphism(item, quantity, 14);
    public static final BuilderMorphism productToId =               builder.morphism(product, id, 15);
    public static final BuilderMorphism productToLabel =            builder.min(Min.ZERO).morphism(product, label, 16);
    public static final BuilderMorphism productToPrice =            builder.min(Min.ZERO).morphism(product, price, 17);

    public static final Signature       itemToNumber =              builder.concatenate(itemToOrder, orderToNumber);
    public static final Signature       itemToId =                  builder.concatenate(itemToProduct, productToId);
    public static final Signature       itemToLabel =               builder.concatenate(itemToProduct, productToLabel);
    public static final Signature       itemToPrice =               builder.concatenate(itemToProduct, productToPrice);

    public static final BuilderMorphism contactToOrder =            builder.morphism(contact, order, 18);
    public static final BuilderMorphism contactToValue =            builder.morphism(contact, value, 19);
    public static final BuilderMorphism contactToType =             builder.morphism(contact, type, 20);

    public static final Signature       contactToNumber =           builder.concatenate(contactToOrder, orderToNumber);
    public static final Signature       orderToValue =              builder.concatenate(contactToOrder.dual(), contactToValue);
    public static final Signature       orderToType =               builder.concatenate(contactToOrder.dual(), contactToType);

    public static final BuilderMorphism noteToOrder =               builder.morphism(note, order, 21);
    public static final BuilderMorphism noteToLocale =              builder.morphism(note, locale, 22);
    public static final BuilderMorphism noteToData =                builder.morphism(note, data, 23);
    public static final BuilderMorphism dataToSubject =             builder.morphism(data, subject, 24);
    public static final BuilderMorphism dataToContent =             builder.morphism(data, content, 25);

    public static final Signature       noteToNumber =              builder.concatenate(noteToOrder, orderToNumber);
    public static final Signature       orderToLocale =             builder.concatenate(noteToOrder.dual(), noteToLocale);
    public static final Signature       orderToData =               builder.concatenate(noteToOrder.dual(), noteToData);


    public static final BuilderMorphism dynamicToId =               builder.morphism(dynamic, dId, 26);
    public static final BuilderMorphism dynamicToLabel =            builder.morphism(dynamic, dLabel, 27);
    public static final BuilderMorphism prefixToDynamic =           builder.morphism(prefix, dynamic, 28);
    public static final BuilderMorphism prefixToType =              builder.morphism(prefix, prefixType, 29);
    public static final BuilderMorphism prefixToValue =             builder.morphism(prefix, prefixValue, 30);
    public static final BuilderMorphism prefiyToDynamic =           builder.morphism(prefiy, dynamic, 31);
    public static final BuilderMorphism prefiyToType =              builder.morphism(prefiy, prefiyType, 32);
    public static final BuilderMorphism prefiyToValue =             builder.morphism(prefiy, prefiyValue, 33);
    public static final BuilderMorphism catchAllToDynamic =         builder.morphism(catchAll, dynamic, 34);
    public static final BuilderMorphism catchAllToType =            builder.morphism(catchAll, catchAllType, 35);
    public static final BuilderMorphism catchAllToValue =           builder.morphism(catchAll, catchAllValue, 36);

    public static final Signature       prefixToId =                builder.concatenate(prefixToDynamic, dynamicToId);
    public static final Signature       dynamicToPrefixType =       builder.concatenate(prefixToDynamic.dual(), prefixToType);
    public static final Signature       dynamicToPrefixValue =      builder.concatenate(prefixToDynamic.dual(), prefixToValue);

    public static final Signature       prefiyToId =                builder.concatenate(prefiyToDynamic, dynamicToId);
    public static final Signature       dynamicToPrefiyType =       builder.concatenate(prefiyToDynamic.dual(), prefiyToType);
    public static final Signature       dynamicToPrefiyValue =      builder.concatenate(prefiyToDynamic.dual(), prefiyToValue);

    public static final Signature       catchAllToId =              builder.concatenate(catchAllToDynamic, dynamicToId);
    public static final Signature       dynamicToCatchAllType =     builder.concatenate(catchAllToDynamic.dual(), catchAllToType);
    public static final Signature       dynamicToCatchAllValue =    builder.concatenate(catchAllToDynamic.dual(), catchAllToValue);

    // Ids

    static {

        builder
            .ids(order, orderToNumber)
            // Tags are identified in two ways - either as a set (number, value) or as an array (number, index). We want to be able to test both usecases.
            .ids(tags, tagsToNumber, tagsToTag)
            .ids(tags, tagsToNumber, tagsToIndex)
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
