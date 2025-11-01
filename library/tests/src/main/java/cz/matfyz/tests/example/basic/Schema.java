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

    // #region Basic

    // Keys

    public static final BuilderObjex order =           builder.objex("order", 1);
    public static final BuilderObjex number =          builder.objex("number", 2);
    public static final BuilderObjex tags =            builder.objex("tags", 3);
    public static final BuilderObjex index =           builder.objex("index", 4);
    public static final BuilderObjex tag =             builder.objex("tag", 5);

    public static final BuilderObjex customer =        builder.objex("customer", 21);
    public static final BuilderObjex name =            builder.objex("name", 22);
    public static final BuilderObjex friend =          builder.objex("friend", 23);
    public static final BuilderObjex since =           builder.objex("since", 24);

    public static final BuilderObjex address =         builder.generatedIds().objex("address", 41);
    public static final BuilderObjex street =          builder.objex("street", 42);
    public static final BuilderObjex city =            builder.objex("city", 43);
    public static final BuilderObjex zip =             builder.objex("zip", 44);

    public static final BuilderObjex item =            builder.objex("item", 51);
    public static final BuilderObjex product =         builder.objex("product", 52);
    public static final BuilderObjex quantity =        builder.objex("quantity", 53);
    public static final BuilderObjex id =              builder.objex("id", 54);
    public static final BuilderObjex label =           builder.objex("label", 55);
    public static final BuilderObjex price =           builder.objex("price", 56);

    public static final BuilderObjex contact =         builder.objex("contact", 61);
    public static final BuilderObjex value =           builder.objex("value", 62);
    public static final BuilderObjex type =            builder.objex("type", 63);

    public static final BuilderObjex note =            builder.objex("note", 71);
    public static final BuilderObjex locale =          builder.objex("locale", 72);
    public static final BuilderObjex data =            builder.generatedIds().objex("data", 73);
    public static final BuilderObjex subject =         builder.objex("subject", 74);
    public static final BuilderObjex content =         builder.objex("content", 75);

    // Morphisms

    public static final BuilderMorphism order_number =             builder.morphism(order, number, 1);
    public static final BuilderMorphism tags_order =               builder.tags(Tag.role).morphism(tags, order, 2);
    public static final BuilderMorphism tags_index =               builder.tags(Tag.role).morphism(tags, index, 3);
    public static final BuilderMorphism tags_tag =                 builder.tags(Tag.role).morphism(tags, tag, 4);
    public static final Signature       order_index =              builder.concatenate(tags_order.dual(), tags_index);
    public static final Signature       order_tag =                builder.concatenate(tags_order.dual(), tags_tag);
    public static final Signature       tags_number =              builder.concatenate(tags_order, order_number);

    public static final BuilderMorphism order_customer =           builder.morphism(order, customer, 21);
    public static final BuilderMorphism customer_name =            builder.morphism(customer, name, 22);
    public static final BuilderMorphism friend_customerA =         builder.morphism(friend, customer, 23);
    public static final BuilderMorphism friend_customerB =         builder.morphism(friend, customer, 24);
    public static final BuilderMorphism friend_since =             builder.morphism(friend, since, 25);

    public static final Signature       order_name =               builder.concatenate(order_customer, customer_name);
    public static final Signature       friend_nameA =             builder.concatenate(friend_customerA, customer_name);
    public static final Signature       friend_nameB =             builder.concatenate(friend_customerB, customer_name);

    public static final BuilderMorphism order_address =            builder.morphism(order, address, 41);
    public static final BuilderMorphism address_street =           builder.morphism(address, street, 42);
    public static final BuilderMorphism address_city =             builder.morphism(address, city, 43);
    public static final BuilderMorphism address_zip =              builder.morphism(address, zip, 44);

    public static final BuilderMorphism item_order =               builder.tags(Tag.role).morphism(item, order, 51);
    public static final BuilderMorphism item_product =             builder.tags(Tag.role).morphism(item, product, 52);
    public static final BuilderMorphism item_quantity =            builder.morphism(item, quantity, 53);
    public static final BuilderMorphism product_id =               builder.morphism(product, id, 54);
    public static final BuilderMorphism product_label =            builder.min(Min.ZERO).morphism(product, label, 55);
    public static final BuilderMorphism product_price =            builder.min(Min.ZERO).morphism(product, price, 56);

    public static final Signature       item_number =              builder.concatenate(item_order, order_number);
    public static final Signature       item_id =                  builder.concatenate(item_product, product_id);
    public static final Signature       item_label =               builder.concatenate(item_product, product_label);
    public static final Signature       item_price =               builder.concatenate(item_product, product_price);

    public static final BuilderMorphism contact_order =            builder.morphism(contact, order, 61);
    public static final BuilderMorphism contact_value =            builder.morphism(contact, value, 62);
    public static final BuilderMorphism contact_type =             builder.morphism(contact, type, 63);

    public static final Signature       contact_number =           builder.concatenate(contact_order, order_number);
    public static final Signature       order_value =              builder.concatenate(contact_order.dual(), contact_value);
    public static final Signature       order_type =               builder.concatenate(contact_order.dual(), contact_type);

    public static final BuilderMorphism note_order =               builder.morphism(note, order, 71);
    public static final BuilderMorphism note_locale =              builder.morphism(note, locale, 72);
    public static final BuilderMorphism note_data =                builder.morphism(note, data, 73);
    public static final BuilderMorphism data_subject =             builder.morphism(data, subject, 74);
    public static final BuilderMorphism data_content =             builder.morphism(data, content, 75);

    public static final Signature       note_number =              builder.concatenate(note_order, order_number);
    public static final Signature       order_locale =             builder.concatenate(note_order.dual(), note_locale);
    public static final Signature       order_data =               builder.concatenate(note_order.dual(), note_data);

    static {
        builder
            .ids(order, order_number)
            // Tags are identified in two ways - either as a set (number, value) or as an array (number, index). We want to be able to test both usecases.
            .ids(tags, tags_number, tags_tag)
            .ids(tags, tags_number, tags_index)
            .ids(customer, customer_name)
            .ids(friend, friend_customerA, friend_customerB)
            .ids(item, item_number, item_id)
            .ids(product, product_id)
            .ids(contact, contact_number, contact_type)
            .ids(note, note_number, note_locale);
    }

    // #endregion
    // #region Dynamic patterns

    public static final BuilderObjex dynamic =         builder.objex("dynamic", 101);
    public static final BuilderObjex dId =             builder.objex("id", 102);
    public static final BuilderObjex dLabel =          builder.objex("label", 103);
    public static final BuilderObjex prefix =          builder.objex("prefix", 104);
    public static final BuilderObjex prefixType =      builder.objex("x-type", 105);
    public static final BuilderObjex prefixValue =     builder.objex("x-value", 106);
    public static final BuilderObjex prefiy =          builder.objex("prefiy", 107);
    public static final BuilderObjex prefiyType =      builder.objex("y-type", 108);
    public static final BuilderObjex prefiyValue =     builder.objex("y-value", 109);
    public static final BuilderObjex catchAll =        builder.objex("catch-all", 110);
    public static final BuilderObjex catchAllType =    builder.objex("catch-all-type", 111);
    public static final BuilderObjex catchAllValue =   builder.objex("catch-all-value", 112);

    public static final BuilderMorphism dynamic_id =               builder.morphism(dynamic, dId, 101);
    public static final BuilderMorphism dynamic_label =            builder.morphism(dynamic, dLabel, 102);
    public static final BuilderMorphism prefix_dynamic =           builder.morphism(prefix, dynamic, 103);
    public static final BuilderMorphism prefix_type =              builder.morphism(prefix, prefixType, 104);
    public static final BuilderMorphism prefix_value =             builder.morphism(prefix, prefixValue, 105);
    public static final BuilderMorphism prefiy_dynamic =           builder.morphism(prefiy, dynamic, 106);
    public static final BuilderMorphism prefiy_type =              builder.morphism(prefiy, prefiyType, 107);
    public static final BuilderMorphism prefiy_value =             builder.morphism(prefiy, prefiyValue, 108);
    public static final BuilderMorphism catchAll_dynamic =         builder.morphism(catchAll, dynamic, 109);
    public static final BuilderMorphism catchAll_type =            builder.morphism(catchAll, catchAllType, 110);
    public static final BuilderMorphism catchAll_value =           builder.morphism(catchAll, catchAllValue, 111);

    public static final Signature       prefix_id =                builder.concatenate(prefix_dynamic, dynamic_id);
    public static final Signature       dynamic_prefixType =       builder.concatenate(prefix_dynamic.dual(), prefix_type);
    public static final Signature       dynamic_prefixValue =      builder.concatenate(prefix_dynamic.dual(), prefix_value);

    public static final Signature       prefiy_id =                builder.concatenate(prefiy_dynamic, dynamic_id);
    public static final Signature       dynamic_prefiyType =       builder.concatenate(prefiy_dynamic.dual(), prefiy_type);
    public static final Signature       dynamic_prefiyValue =      builder.concatenate(prefiy_dynamic.dual(), prefiy_value);

    public static final Signature       catchAll_id =              builder.concatenate(catchAll_dynamic, dynamic_id);
    public static final Signature       dynamic_catchAllType =     builder.concatenate(catchAll_dynamic.dual(), catchAll_type);
    public static final Signature       dynamic_catchAllValue =    builder.concatenate(catchAll_dynamic.dual(), catchAll_value);

    static {
        builder
            .ids(dynamic, dynamic_id)
            .ids(prefix, prefix_id, prefix_type)
            .ids(prefiy, prefiy_id, prefiy_type)
            .ids(catchAll, catchAll_id, catchAll_type);
    }

    // #endregion
    // #region Hardcore

    public static final BuilderObjex hardcore = builder.objex("hardcore", 201);
    public static final BuilderObjex hId =      builder.objex("id", 202);

    // Map - 2D array - 1D array branch
    public static final BuilderObjex map =      builder.objex("map", 211);
    public static final BuilderObjex key =      builder.objex("key", 212);

    // Let's use generated IDs because it would be such a royal pain in the ass to connect it with all the identifiers of map (and even more so for array1D).
    public static final BuilderObjex array2D =  builder.generatedIds().objex("array2D", 213);
    public static final BuilderObjex index1 =   builder.objex("index1", 214);
    public static final BuilderObjex index2 =   builder.objex("index2", 215);

    public static final BuilderObjex array1D =  builder.generatedIds().objex("array1D", 216);
    public static final BuilderObjex index3 =   builder.objex("index3", 217);
    public static final BuilderObjex simple =   builder.objex("simple", 219);

    // Array - complex - map (with nested properties) branch
    public static final BuilderObjex array =    builder.objex("array", 221);
    public static final BuilderObjex index4 =   builder.objex("index4", 222);

    public static final BuilderObjex complex =  builder.objex("complex", 223);
    public static final BuilderObjex cId =      builder.objex("cId", 224);

    public static final BuilderObjex cMap =     builder.objex("cMap", 225);
    public static final BuilderObjex cKey =     builder.objex("cKey", 226);
    public static final BuilderObjex cValueI =  builder.objex("cValueI", 227);
    public static final BuilderObjex cValueJ =  builder.objex("cValueJ", 228);

    public static final BuilderMorphism hardcore_id =       builder.morphism(hardcore, hId, 201);

    // Map - 2D array - 1D array branch
    public static final BuilderMorphism map_hardcore =      builder.morphism(map, hardcore, 211);
    public static final BuilderMorphism map_key =           builder.morphism(map, key, 212);

    public static final BuilderMorphism array2D_map =       builder.morphism(array2D, map, 213);
    public static final BuilderMorphism array2D_index1 =    builder.morphism(array2D, index1, 214);
    public static final BuilderMorphism array2D_index2 =    builder.morphism(array2D, index2, 215);

    public static final BuilderMorphism array1D_array2D =   builder.morphism(array1D, array2D, 216);
    public static final BuilderMorphism array1D_index3 =    builder.morphism(array1D, index3, 217);
    public static final BuilderMorphism array1D_simple =    builder.morphism(array1D, simple, 218);

    public static final Signature       map_id =            builder.concatenate(map_hardcore, hardcore_id);

    // Array - complex - map (with nested properties) branch
    public static final BuilderMorphism array_hardcore =    builder.morphism(array, hardcore, 221);
    public static final BuilderMorphism array_index4 =      builder.morphism(array, index4, 222);
    public static final BuilderMorphism array_complex =     builder.morphism(array, complex, 223);

    public static final BuilderMorphism complex_cId =       builder.morphism(complex, cId, 224);

    public static final BuilderMorphism cMap_complex =      builder.morphism(cMap, complex, 225);
    public static final BuilderMorphism cMap_cKey =         builder.morphism(cMap, cKey, 226);
    public static final BuilderMorphism cMap_cValueI =      builder.morphism(cMap, cValueI, 227);
    public static final BuilderMorphism cMap_cValueJ =      builder.morphism(cMap, cValueJ, 228);

    public static final Signature       array_id =          builder.concatenate(array_hardcore, hardcore_id);
    public static final Signature       cMap_cId =          builder.concatenate(cMap_complex, complex_cId);

    static {
        builder
            .ids(hardcore, hardcore_id)
            .ids(map, map_id, map_key)
            .ids(array, array_id, array_index4)
            .ids(complex, complex_cId)
            .ids(cMap, cMap_cId, cMap_cKey);
    }

    // #endregion

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
