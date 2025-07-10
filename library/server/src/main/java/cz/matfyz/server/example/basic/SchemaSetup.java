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
            addMorphism(Schema.orderToNumber);
        });
        addIds(Schema.order);

        addObjex(Schema.tag, -1, -1);
        addMorphism(Schema.tagToOrder);

        // Customer
        addObjex(Schema.customer, -2, 0);
        addMorphism(Schema.orderToCustomer);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.name, -2, -1);
            addMorphism(Schema.customerToName);
        });
        addIds(Schema.customer);

        addObjex(Schema.friend, -3, -0);
        addMorphism(Schema.friendToCustomerA);
        addMorphism(Schema.friendToCustomerB);
        addIds(Schema.friend);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.since, -3, -1);
            addMorphism(Schema.friendToSince);
        });

        // Address
        addObjex(Schema.address, -2, 1);
        addMorphism(Schema.orderToAddress);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.street, -3, 1);
            addMorphism(Schema.addressToStreet);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.city, -3, 2);
            addMorphism(Schema.addressToCity);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.zip, -2, 2);
            addMorphism(Schema.addressToZip);
        });

        // Item - Product
        addObjex(Schema.product, 2, 1);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.id, 1, 2);
            addMorphism(Schema.productToId);
        });
        addIds(Schema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.label, 2, 2);
            addMorphism(Schema.productToLabel);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.price, 3, 2);
            addMorphism(Schema.productToPrice);
        });

        // Item
        addComposite(ADD_SET, () -> {
            addObjex(Schema.item, 1, 1);
            addMorphism(Schema.itemToOrder);
            addMorphism(Schema.itemToProduct);
            addIds(Schema.item);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.quantity, 1, 0);
            addMorphism(Schema.itemToQuantity);
        });

        // Contact
        addObjex(Schema.value, 0, 2);

        addComposite(ADD_MAP, () -> {
            addObjex(Schema.type, -1, 2);
            addObjex(Schema.contact, 0, 1);
            addMorphism(Schema.contactToType);
            addMorphism(Schema.contactToOrder);
            addMorphism(Schema.contactToValue);
            addIds(Schema.contact);
        });

        // Note - Data
        addObjex(Schema.data, 2, -1);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.subject, 3, -1);
            addMorphism(Schema.dataToSubject);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.content, 3, 0);
            addMorphism(Schema.dataToContent);
        });

        // Note
        addComposite(ADD_MAP, () -> {
            addObjex(Schema.locale, 2, 0);
            addObjex(Schema.note, 1, -1);
            addMorphism(Schema.noteToLocale);
            addMorphism(Schema.noteToOrder);
            addMorphism(Schema.noteToData);
            addIds(Schema.note);
        });
    }

}
