package cz.matfyz.server.example.queryevolution;

import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.queryevolution.Schema;

class SchemaSetup extends SchemaBase {

    private final int version;

    private SchemaSetup(SchemaCategoryWrapper wrapper, int version) {
        super(wrapper, Schema.newSchema(version));
        this.version = version;
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryWrapper wrapper, int version) {
        return new SchemaSetup(wrapper, version).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        if (version == 1)
            firstVersion();
        else
            secondVersion();
    }

    private void firstVersion() {
        // Customer
        addObject(Schema.customer, -2, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.customerId, -3, -1);
            addMorphism(Schema.customerToCustomerId);
        });
        addIds(Schema.customer);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.name, -3, 0);
            addMorphism(Schema.customerToName);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.surname, -3, 1);
            addMorphism(Schema.customerToSurname);
        });

        addObject(Schema.knows, -2, 1);
        addMorphism(Schema.knowsToCustomerA);
        addMorphism(Schema.knowsToCustomerB);
        addIds(Schema.knows);

        // Product
        addObject(Schema.product, 2, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.productId, 3, -1);
            addMorphism(Schema.productToProductId);
        });
        addIds(Schema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.title, 3, 0);
            addMorphism(Schema.productToTitle);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.productPrice, 3, 1);
            addMorphism(Schema.productToProductPrice);
        });

        // Order
        addObject(Schema.order, 0, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.orderId, -1, -1);
            addMorphism(Schema.orderToOrderId);
        });
        addIds(Schema.order);

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.street, -0.5, -2);
            addMorphism(Schema.orderToStreet);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.city, 0.5, -2);
            addMorphism(Schema.orderToCity);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.postCode, 1, -1);
            addMorphism(Schema.orderToPostCode);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.orderPrice, -1, 1);
            addMorphism(Schema.orderToOrderPrice);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(Schema.quantity, 1, 1);
            addMorphism(Schema.orderToQuantity);
        });

        addMorphism(Schema.orderToCustomer);
        addMorphism(Schema.orderToProduct);
    }

    private void secondVersion() {
        addComposite("group", () -> {
            editIds(Schema.order, new ObjectIds(Schema.orderToOrderId.signature()));
            addObject(Schema.item, 0, 0);
            addMorphism(Schema.itemToOrder);
            updateMorphism(Schema.orderToProduct, Schema.item, null);
            addIds(Schema.item);
            updateMorphism(Schema.orderToOrderPrice, Schema.item, null);
            updateMorphism(Schema.orderToQuantity, Schema.item, null);
        });

        moveObject(Schema.order, 0, -1);
        moveObject(Schema.orderId, -1, -2);
        moveObject(Schema.street, -0.5, -3);
        moveObject(Schema.city, 0.5, -3);
        moveObject(Schema.postCode, 1, -2);

        addComposite("group", () -> {
            addObject(Schema.ordered, -2, -1);
            addMorphism(Schema.orderedToOrder);
            updateMorphism(Schema.orderToCustomer, Schema.ordered, null);
            addIds(Schema.ordered);
        });
    }

}
