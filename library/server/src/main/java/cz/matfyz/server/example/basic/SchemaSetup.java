package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
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
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.number, 0, -1);
            addMorphism(Schema.order_number);
        });
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
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.name, -2, -1);
            addMorphism(Schema.customer_name);
        });
        addIds(Schema.customer);

        addObjex(Schema.friend, -3, -0);
        addMorphism(Schema.friend_customerA);
        addMorphism(Schema.friend_customerB);
        addIds(Schema.friend);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.since, -3, -1);
            addMorphism(Schema.friend_since);
        });

        // Address
        addObjex(Schema.address, -2, 1);
        addMorphism(Schema.order_address);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.street, -3, 1);
            addMorphism(Schema.address_street);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.city, -3, 2);
            addMorphism(Schema.address_city);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.zip, -2, 2);
            addMorphism(Schema.address_zip);
        });

        // Item - Product
        addObjex(Schema.product, 2, 1);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.id, 1, 2);
            addMorphism(Schema.product_id);
        });
        addIds(Schema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.label, 2, 2);
            addMorphism(Schema.product_label);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.price, 3, 2);
            addMorphism(Schema.product_price);
        });

        // Item
        addComposite(ADD_SET, () -> {
            addObjex(Schema.item, 1, 1);
            addMorphism(Schema.item_order);
            addMorphism(Schema.item_product);
            addIds(Schema.item);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.quantity, 1, 0);
            addMorphism(Schema.item_quantity);
        });

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

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.subject, 3, -1);
            addMorphism(Schema.data_subject);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.content, 3, 0);
            addMorphism(Schema.data_content);
        });

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
