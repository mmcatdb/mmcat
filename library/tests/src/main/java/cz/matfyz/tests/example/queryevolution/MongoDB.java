package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final String ordersKind = "orders";
    public static final String orderKind = "order";

    public static TestMapping orders(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            ordersKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("_id", Schema.orderToOrderId),
                new SimpleProperty("street", Schema.orderToStreet),
                new SimpleProperty("city", Schema.orderToCity),
                new SimpleProperty("postCode", Schema.orderToPostCode),
                ComplexProperty.create("customer", Schema.orderedToOrder.dual().concatenate(Schema.orderedToCustomer.signature()),
                    new SimpleProperty("id", Schema.customerToCustomerId),
                    new SimpleProperty("name", Schema.customerToName),
                    new SimpleProperty("surname", Schema.customerToSurname),
                    ComplexProperty.create("knows", Schema.knowsToCustomerA.dual().concatenate(Schema.knowsToCustomerB.signature()),
                        new SimpleProperty("id", Schema.customerToCustomerId),
                        new SimpleProperty("name", Schema.customerToName),
                        new SimpleProperty("surname", Schema.customerToSurname)
                    )
                ),
                ComplexProperty.create("items", Schema.itemToOrder.dual(),
                new SimpleProperty("quantity", Schema.itemToQuantity),
                new SimpleProperty("price", Schema.itemToOrderPrice),
                    new SimpleProperty("pid", Schema.itemToProduct.signature().concatenate(Schema.productToProductId.signature())),
                    new SimpleProperty("title", Schema.itemToProduct.signature().concatenate(Schema.productToTitle.signature())),
                    new SimpleProperty("currentPrice", Schema.itemToProduct.signature().concatenate(Schema.productToProductPrice.signature()))
                )
            )
        );
    }

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("_id", Schema.orderToOrderId),
                new SimpleProperty("street", Schema.orderToStreet),
                new SimpleProperty("city", Schema.orderToCity),
                new SimpleProperty("postCode", Schema.orderToPostCode),
                ComplexProperty.create("customer", Schema.orderedToOrder.dual().concatenate(Schema.orderedToCustomer.signature()),
                    new SimpleProperty("id", Schema.customerToCustomerId),
                    new SimpleProperty("name", Schema.customerToName),
                    new SimpleProperty("surname", Schema.customerToSurname)
                ),
                ComplexProperty.create("items", Schema.itemToOrder.dual(),
                new SimpleProperty("quantity", Schema.itemToQuantity),
                new SimpleProperty("price", Schema.itemToOrderPrice),
                    new SimpleProperty("pid", Schema.itemToProduct.signature().concatenate(Schema.productToProductId.signature())),
                    new SimpleProperty("title", Schema.itemToProduct.signature().concatenate(Schema.productToTitle.signature()))
                )
            )
        );
    }

}
