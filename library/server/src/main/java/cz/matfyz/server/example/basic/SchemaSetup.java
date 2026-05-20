package cz.matfyz.server.example.basic;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.tests.example.basic.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryEntity categoryEntity) {
        super(categoryEntity, Schema.newSchema());
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryEntity categoryEntity) {
        return new SchemaSetup(categoryEntity).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        // Order
        addObjex(Schema.order, 0, 0);
        addProperty(Schema.number, Schema.order_number, 0, -1);
        addIds(Schema.order);

        // Tag - it's both a set and an array (so that we can test both usecases).
        addObjex(Schema.tag, -1, -1);
        addComposite("custom", () -> {
            addObjex(Schema.tags, -0.5, -0.5);
            addMorphism(Schema.tags_order);
            addMorphism(Schema.tags_tag);
            addObjex(Schema.index, -1, -0.5);
            addMorphism(Schema.tags_index);
            addIds(Schema.tags);
        });

        // Customer
        addObjex(Schema.customer, -2, 0);
        addMorphism(Schema.order_customer);
        addProperty(Schema.name, Schema.customer_name, -2, -1);
        addIds(Schema.customer);

        addObjex(Schema.friend, -3, -0);
        addMorphism(Schema.friend_customerA);
        addMorphism(Schema.friend_customerB);
        addIds(Schema.friend);

        addProperty(Schema.since, Schema.friend_since, -3, -1);

        // Address
        addObjex(Schema.address, -2, 1);
        addMorphism(Schema.order_address);

        addProperty(Schema.street, Schema.address_street, -3, 1);
        addProperty(Schema.city, Schema.address_city, -3, 2);
        addProperty(Schema.zip, Schema.address_zip, -2, 2);

        // Item - Product
        addObjex(Schema.product, 2, 1);
        addProperty(Schema.id, Schema.product_id, 1, 2);
        addIds(Schema.product);

        addProperty(Schema.label, Schema.product_label, 2, 2);
        addProperty(Schema.price, Schema.product_price, 3, 2);

        // Item
        addComposite(ADD_SET, () -> {
            addObjex(Schema.item, 1, 1);
            addMorphism(Schema.item_order);
            addMorphism(Schema.item_product);
            addIds(Schema.item);
        });

        addProperty(Schema.quantity, Schema.item_quantity, 1, 0);

        // Contact
        addObjex(Schema.value, 0, 2);
        addComposite(ADD_MAP, () -> {
            addObjex(Schema.type, -1, 2);
            addObjex(Schema.contact, 0, 1);
            addMorphism(Schema.contact_type);
            addMorphism(Schema.contact_order);
            addMorphism(Schema.contact_value);
            addIds(Schema.contact);
        });

        // Note - Data
        addObjex(Schema.data, 2, -1);
        addProperty(Schema.subject, Schema.data_subject, 3, -1);
        addProperty(Schema.content, Schema.data_content, 3, 0);

        // Note
        addComposite(ADD_MAP, () -> {
            addObjex(Schema.locale, 2, 0);
            addObjex(Schema.note, 1, -1);
            addMorphism(Schema.note_locale);
            addMorphism(Schema.note_order);
            addMorphism(Schema.note_data);
            addIds(Schema.note);
        });
    }

}
