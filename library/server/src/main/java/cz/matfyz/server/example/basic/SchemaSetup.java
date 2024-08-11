package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.tests.example.basic.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryWrapper wrapper, String lastUpdateVersion) {
        super(wrapper, lastUpdateVersion, Schema.newSchema());
    }

    static SchemaUpdateInit createNewUpdate(SchemaCategoryWrapper wrapper, String lastUpdateVersion) {
        return new SchemaSetup(wrapper, lastUpdateVersion).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        // Order
        addObject(Schema.order, 0, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.number, 0, -1);
            addMorphism(Schema.orderToNumber);
        });
        addIds(Schema.order);

        addObject(Schema.tag, -1, -1);
        addMorphism(Schema.tagToOrder);

        // Customer
        addObject(Schema.customer, -2, 0);
        addMorphism(Schema.orderToCustomer);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.name, -2, -1);
            addMorphism(Schema.customerToName);
        });
        addIds(Schema.customer);

        addObject(Schema.friend, -3, -0);
        addMorphism(Schema.friendToCustomerA);
        addMorphism(Schema.friendToCustomerB);
        addIds(Schema.friend);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.since, -3, -1);
            addMorphism(Schema.friendToSince);
        });

        // Address
        addObject(Schema.address, -2, 1);
        addMorphism(Schema.orderToAddress);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.street, -3, 1);
            addMorphism(Schema.addressToStreet);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.city, -3, 2);
            addMorphism(Schema.addressToCity);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.zip, -2, 2);
            addMorphism(Schema.addressToZip);
        });

        // Item - Product
        addObject(Schema.product, 2, 1);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.id, 1, 2);
            addMorphism(Schema.productToId);
        });
        addIds(Schema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.label, 2, 2);
            addMorphism(Schema.productToLabel);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.price, 3, 2);
            addMorphism(Schema.productToPrice);
        });

        // Item
        addComposite(ADD_SET, () -> {
            addObject(Schema.item, 1, 1);
            addMorphism(Schema.itemToOrder);
            addMorphism(Schema.itemToProduct);
            addIds(Schema.item);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.quantity, 1, 0);
            addMorphism(Schema.itemToQuantity);
        });

        // Contact
        addObject(Schema.value, 0, 2);

        addComposite(ADD_MAP, () -> {
            addObject(Schema.type, -1, 2);
            addObject(Schema.contact, 0, 1);
            addMorphism(Schema.contactToType);
            addMorphism(Schema.contactToOrder);
            addMorphism(Schema.contactToValue);
            addIds(Schema.contact);
        });

        // Note - Data
        addObject(Schema.data, 2, -1);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.subject, 3, -1);
            addMorphism(Schema.dataToSubject);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.content, 3, 0);
            addMorphism(Schema.dataToContent);
        });

        // Note
        addComposite(ADD_MAP, () -> {
            addObject(Schema.locale, 2, 0);
            addObject(Schema.note, 1, -1);
            addMorphism(Schema.noteToLocale);
            addMorphism(Schema.noteToOrder);
            addMorphism(Schema.noteToData);
            addIds(Schema.note);
        });
    }

}
