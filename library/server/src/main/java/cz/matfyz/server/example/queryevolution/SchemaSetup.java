package cz.matfyz.server.example.queryevolution;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.example.common.SchemaBase;
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
        addProperty(Schema.customerId, Schema.customer_customerId, -3, -1);
        addIds(Schema.customer);

        addProperty(Schema.name, Schema.customer_name, -3, 0);
        addProperty(Schema.surname, Schema.customer_surname, -3, 1);

        addObjex(Schema.knows, -2, 1);
        addMorphism(Schema.knows_customerA);
        addMorphism(Schema.knows_customerB);
        addIds(Schema.knows);

        // Product
        addObjex(Schema.product, 2, 0);
        addProperty(Schema.productId, Schema.product_productId, 3, -1);
        addIds(Schema.product);

        addProperty(Schema.title, Schema.product_title, 3, 0);
        addProperty(Schema.productPrice, Schema.product_productPrice, 3, 1);

        // Order
        addObjex(Schema.order, 0, 0);
        addProperty(Schema.orderId, Schema.order_orderId, -1, -1);
        addIds(Schema.order);

        addProperty(Schema.street, Schema.order_street, -0.5, -2);
        addProperty(Schema.city, Schema.order_city, 0.5, -2);
        addProperty(Schema.postCode, Schema.order_postCode, 1, -1);
        addProperty(Schema.orderPrice, Schema.order_orderPrice, -1, 1);
        addProperty(Schema.quantity, Schema.order_quantity, 1, 1);

        addMorphism(Schema.order_customer);
        addMorphism(Schema.order_product);
    }

    private void secondVersion() {
        addComposite("group", () -> {
            editIds(Schema.order, ObjexIds.fromSignatures(Schema.order_orderId.signature()));
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
