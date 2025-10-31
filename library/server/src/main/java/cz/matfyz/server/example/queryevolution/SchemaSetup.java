package cz.matfyz.server.example.queryevolution;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.queryevolution.Schema;

class SchemaSetup extends SchemaBase {

    private final int version;

    private SchemaSetup(SchemaCategoryEntity categoryEntity, int version) {
        super(categoryEntity, Schema.newSchema(version));
        this.version = version;
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryEntity categoryEntity, int version) {
        return new SchemaSetup(categoryEntity, version).innerCreateNewUpdate();
    }

    @Override protected void createOperations() {
        if (version == 1)
            firstVersion();
        else
            secondVersion();
    }

    private void firstVersion() {
        // Customer
        addObjex(Schema.customer, -2, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.customerId, -3, -1);
            addMorphism(Schema.customer_customerId);
        });
        addIds(Schema.customer);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.name, -3, 0);
            addMorphism(Schema.customer_name);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.surname, -3, 1);
            addMorphism(Schema.customer_surname);
        });

        addObjex(Schema.knows, -2, 1);
        addMorphism(Schema.knows_customerA);
        addMorphism(Schema.knows_customerB);
        addIds(Schema.knows);

        // Product
        addObjex(Schema.product, 2, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.productId, 3, -1);
            addMorphism(Schema.product_productId);
        });
        addIds(Schema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.title, 3, 0);
            addMorphism(Schema.product_title);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.productPrice, 3, 1);
            addMorphism(Schema.product_productPrice);
        });

        // Order
        addObjex(Schema.order, 0, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.orderId, -1, -1);
            addMorphism(Schema.order_orderId);
        });
        addIds(Schema.order);

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.street, -0.5, -2);
            addMorphism(Schema.order_street);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.city, 0.5, -2);
            addMorphism(Schema.order_city);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.postCode, 1, -1);
            addMorphism(Schema.order_postCode);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.orderPrice, -1, 1);
            addMorphism(Schema.order_orderPrice);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObjex(Schema.quantity, 1, 1);
            addMorphism(Schema.order_quantity);
        });

        addMorphism(Schema.order_customer);
        addMorphism(Schema.order_product);
    }

    private void secondVersion() {
        addComposite("group", () -> {
            editIds(Schema.order, new ObjexIds(Schema.order_orderId.signature()));
            addObjex(Schema.item, 0, 0);
            addMorphism(Schema.item_order);
            updateMorphism(Schema.order_product, Schema.item, null);
            addIds(Schema.item);
            updateMorphism(Schema.order_orderPrice, Schema.item, null);
            updateMorphism(Schema.order_quantity, Schema.item, null);
        });

        moveObjex(Schema.order, 0, -1);
        moveObjex(Schema.orderId, -1, -2);
        moveObjex(Schema.street, -0.5, -3);
        moveObjex(Schema.city, 0.5, -3);
        moveObjex(Schema.postCode, 1, -2);

        addComposite("group", () -> {
            addObjex(Schema.ordered, -2, -1);
            addMorphism(Schema.ordered_order);
            updateMorphism(Schema.order_customer, Schema.ordered, null);
            addIds(Schema.ordered);
        });
    }

}
