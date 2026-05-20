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

    private static final SchemaBuilder b = new SchemaBuilder();

    // #region Basic

    // Keys

    public static final BuilderObjex
        order =     b.objex("order", 1),
        number =    b.objex("number", 2),
        tags =      b.objex("tags", 3),
        index =     b.objex("index", 4),
        tag =       b.objex("tag", 5),

        customer =  b.objex("customer", 21),
        name =      b.objex("name", 22),
        friend =    b.objex("friend", 23),
        since =     b.objex("since", 24),

        address =   b.objex("address", 41),
        street =    b.objex("street", 42),
        city =      b.objex("city", 43),
        zip =       b.objex("zip", 44),

        item =      b.objex("item", 51),
        product =   b.objex("product", 52),
        quantity =  b.objex("quantity", 53),
        id =        b.objex("id", 54),
        label =     b.objex("label", 55),
        price =     b.objex("price", 56),

        contact =   b.objex("contact", 61),
        value =     b.objex("value", 62),
        type =      b.objex("type", 63),

        note =      b.objex("note", 71),
        locale =    b.objex("locale", 72),
        data =      b.objex("data", 73),
        subject =   b.objex("subject", 74),
        content =   b.objex("content", 75);

    // Signatures

    public static final BuilderMorphism
        order_number =      b.morphism(order, number, 1),
        tags_order =        b.tags(Tag.role).morphism(tags, order, 2),
        tags_index =        b.tags(Tag.role).morphism(tags, index, 3),
        tags_tag =          b.tags(Tag.role).morphism(tags, tag, 4),

        order_customer =    b.morphism(order, customer, 21),
        customer_name =     b.morphism(customer, name, 22),
        friend_customerA =  b.morphism(friend, customer, 23),
        friend_customerB =  b.morphism(friend, customer, 24),
        friend_since =      b.morphism(friend, since, 25),

        order_address =     b.morphism(order, address, 41),
        address_street =    b.morphism(address, street, 42),
        address_city =      b.morphism(address, city, 43),
        address_zip =       b.morphism(address, zip, 44),

        item_order =        b.tags(Tag.role).morphism(item, order, 51),
        item_product =      b.tags(Tag.role).morphism(item, product, 52),
        item_quantity =     b.morphism(item, quantity, 53),
        product_id =        b.morphism(product, id, 54),
        product_label =     b.min(Min.ZERO).morphism(product, label, 55),
        product_price =     b.min(Min.ZERO).morphism(product, price, 56),

        contact_order =     b.morphism(contact, order, 61),
        contact_value =     b.morphism(contact, value, 62),
        contact_type =      b.morphism(contact, type, 63),

        note_order =        b.morphism(note, order, 71),
        note_locale =       b.morphism(note, locale, 72),
        note_data =         b.morphism(note, data, 73),
        data_subject =      b.morphism(data, subject, 74),
        data_content =      b.morphism(data, content, 75);

    public static final Signature
        order_index =       b.concatenate(tags_order.dual(), tags_index),
        order_tag =         b.concatenate(tags_order.dual(), tags_tag),
        tags_number =       b.concatenate(tags_order, order_number),

        order_name =        b.concatenate(order_customer, customer_name),
        friend_nameA =      b.concatenate(friend_customerA, customer_name),
        friend_nameB =      b.concatenate(friend_customerB, customer_name),

        item_number =       b.concatenate(item_order, order_number),
        item_id =           b.concatenate(item_product, product_id),
        item_label =        b.concatenate(item_product, product_label),
        item_price =        b.concatenate(item_product, product_price),

        contact_number =    b.concatenate(contact_order, order_number),
        order_value =       b.concatenate(contact_order.dual(), contact_value),
        order_type =        b.concatenate(contact_order.dual(), contact_type),

        note_number =       b.concatenate(note_order, order_number),
        order_locale =      b.concatenate(note_order.dual(), note_locale),
        order_data =        b.concatenate(note_order.dual(), note_data);

    static {
        b
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

    public static final BuilderObjex
        dynamic =       b.objex("dynamic", 101),
        dId =           b.objex("id", 102),
        dLabel =        b.objex("label", 103),
        prefix =        b.objex("prefix", 104),
        prefixType =    b.objex("x-type", 105),
        prefixValue =   b.objex("x-value", 106),
        prefiy =        b.objex("prefiy", 107),
        prefiyType =    b.objex("y-type", 108),
        prefiyValue =   b.objex("y-value", 109),
        catchAll =      b.objex("catch-all", 110),
        catchAllType =  b.objex("catch-all-type", 111),
        catchAllValue = b.objex("catch-all-value", 112);

    public static final BuilderMorphism
        dynamic_id =        b.morphism(dynamic, dId, 101),
        dynamic_label =     b.morphism(dynamic, dLabel, 102),
        prefix_dynamic =    b.morphism(prefix, dynamic, 103),
        prefix_type =       b.morphism(prefix, prefixType, 104),
        prefix_value =      b.morphism(prefix, prefixValue, 105),
        prefiy_dynamic =    b.morphism(prefiy, dynamic, 106),
        prefiy_type =       b.morphism(prefiy, prefiyType, 107),
        prefiy_value =      b.morphism(prefiy, prefiyValue, 108),
        catchAll_dynamic =  b.morphism(catchAll, dynamic, 109),
        catchAll_type =     b.morphism(catchAll, catchAllType, 110),
        catchAll_value =    b.morphism(catchAll, catchAllValue, 111);

    public static final Signature
        prefix_id =             b.concatenate(prefix_dynamic, dynamic_id),
        dynamic_prefixType =    b.concatenate(prefix_dynamic.dual(), prefix_type),
        dynamic_prefixValue =   b.concatenate(prefix_dynamic.dual(), prefix_value),

        prefiy_id =             b.concatenate(prefiy_dynamic, dynamic_id),
        dynamic_prefiyType =    b.concatenate(prefiy_dynamic.dual(), prefiy_type),
        dynamic_prefiyValue =   b.concatenate(prefiy_dynamic.dual(), prefiy_value),

        catchAll_id =           b.concatenate(catchAll_dynamic, dynamic_id),
        dynamic_catchAllType =  b.concatenate(catchAll_dynamic.dual(), catchAll_type),
        dynamic_catchAllValue = b.concatenate(catchAll_dynamic.dual(), catchAll_value);

    static {
        b
            .ids(dynamic, dynamic_id)
            .ids(prefix, prefix_id, prefix_type)
            .ids(prefiy, prefiy_id, prefiy_type)
            .ids(catchAll, catchAll_id, catchAll_type);
    }

    // #endregion
    // #region Hardcore

    public static final BuilderObjex
        hardcore =  b.objex("hardcore", 201),
        hId =       b.objex("id", 202),

        // Map - 2D array - 1D array branch
        map =       b.objex("map", 211),
        key =       b.objex("key", 212),

        // Let's use generated IDs because it would be such a royal pain in the ass to connect it with all the identifiers of map (and even more so for array1D).
        array2D =   b.objex("array2D", 213),
        index1 =    b.objex("index1", 214),
        index2 =    b.objex("index2", 215),

        array1D =   b.objex("array1D", 216),
        index3 =    b.objex("index3", 217),
        simple =    b.objex("simple", 219),

        // Array - complex - map (with nested properties) branch
        array =     b.objex("array", 221),
        index4 =    b.objex("index4", 222),

        complex =   b.objex("complex", 223),
        cId =       b.objex("cId", 224),

        cMap =      b.objex("cMap", 225),
        cKey =      b.objex("cKey", 226),
        cValueI =   b.objex("cValueI", 227),
        cValueJ =   b.objex("cValueJ", 228);

    public static final BuilderMorphism
        hardcore_id =       b.morphism(hardcore, hId, 201),

        // Map - 2D array - 1D array branch
        map_hardcore =      b.morphism(map, hardcore, 211),
        map_key =           b.morphism(map, key, 212),

        array2D_map =       b.morphism(array2D, map, 213),
        array2D_index1 =    b.morphism(array2D, index1, 214),
        array2D_index2 =    b.morphism(array2D, index2, 215),

        array1D_array2D =   b.morphism(array1D, array2D, 216),
        array1D_index3 =    b.morphism(array1D, index3, 217),
        array1D_simple =    b.morphism(array1D, simple, 218),

        // Array - complex - map (with nested properties) branch
        array_hardcore =    b.morphism(array, hardcore, 221),
        array_index4 =      b.morphism(array, index4, 222),
        array_complex =     b.morphism(array, complex, 223),

        complex_cId =       b.morphism(complex, cId, 224),

        cMap_complex =      b.morphism(cMap, complex, 225),
        cMap_cKey =         b.morphism(cMap, cKey, 226),
        cMap_cValueI =      b.morphism(cMap, cValueI, 227),
        cMap_cValueJ =      b.morphism(cMap, cValueJ, 228);

    public static final Signature
        // Map
        map_id =            b.concatenate(map_hardcore, hardcore_id),

        // Array
        array_id =          b.concatenate(array_hardcore, hardcore_id),
        cMap_cId =          b.concatenate(cMap_complex, complex_cId);

    static {
        b
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
        return b.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return b.buildMetadata(schema);
    }

    private Schema() {}

}
