package cz.matfyz.tests.example.queryevolution;

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
            b -> b.root(
                b.simple("_id", Schema.orderToOrderId),
                b.simple("street", Schema.orderToStreet),
                b.simple("city", Schema.orderToCity),
                b.simple("postCode", Schema.orderToPostCode),
                b.complex("customer", Schema.orderToCustomer2,
                    b.simple("id", Schema.customerToCustomerId),
                    b.simple("name", Schema.customerToName),
                    b.simple("surname", Schema.customerToSurname),
                    b.complex("knows", Schema.customerAToCustomerB,
                        b.simple("id", Schema.customerToCustomerId),
                        b.simple("name", Schema.customerToName),
                        b.simple("surname", Schema.customerToSurname)
                    )
                ),
                b.complex("items", Schema.itemToOrder.dual(),
                b.simple("quantity", Schema.itemToQuantity),
                b.simple("price", Schema.itemToOrderPrice),
                    b.simple("pid", Schema.itemToProductId),
                    b.simple("title", Schema.itemToTitle),
                    b.simple("currentPrice", Schema.itemToProductPrice)
                )
            )
        );
    }

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("_id", Schema.orderToOrderId),
                b.simple("street", Schema.orderToStreet),
                b.simple("city", Schema.orderToCity),
                b.simple("postCode", Schema.orderToPostCode),
                b.complex("customer", Schema.orderToCustomer2,
                    b.simple("id", Schema.customerToCustomerId),
                    b.simple("name", Schema.customerToName),
                    b.simple("surname", Schema.customerToSurname)
                ),
                b.complex("items", Schema.itemToOrder.dual(),
                b.simple("quantity", Schema.itemToQuantity),
                b.simple("price", Schema.itemToOrderPrice),
                    b.simple("pid", Schema.productToProductId),
                    b.simple("title", Schema.productToTitle)
                )
            )
        );
    }

}
