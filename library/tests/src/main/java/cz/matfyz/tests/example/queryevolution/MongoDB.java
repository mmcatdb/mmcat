package cz.matfyz.tests.example.queryevolution;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String ordersKind = "orders";
    public static final String orderKind = "order";

    public static TestMapping orders(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            ordersKind,
            b -> b.root(
                b.simple("_id", Schema.order_orderId),
                b.simple("street", Schema.order_street),
                b.simple("city", Schema.order_city),
                b.simple("postCode", Schema.order_postCode),
                b.complex("customer", Schema.order_customer2,
                    b.simple("id", Schema.customer_customerId),
                    b.simple("name", Schema.customer_name),
                    b.simple("surname", Schema.customer_surname),
                    b.complex("knows", Schema.customerA_customerB,
                        b.simple("id", Schema.customer_customerId),
                        b.simple("name", Schema.customer_name),
                        b.simple("surname", Schema.customer_surname)
                    )
                ),
                b.complex("items", Schema.item_order.dual(),
                b.simple("quantity", Schema.item_quantity),
                b.simple("price", Schema.item_orderPrice),
                    b.simple("pid", Schema.item_productId),
                    b.simple("title", Schema.item_title),
                    b.simple("currentPrice", Schema.item_productPrice)
                )
            )
        );
    }

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("_id", Schema.order_orderId),
                b.simple("street", Schema.order_street),
                b.simple("city", Schema.order_city),
                b.simple("postCode", Schema.order_postCode),
                b.complex("customer", Schema.order_customer2,
                    b.simple("id", Schema.customer_customerId),
                    b.simple("name", Schema.customer_name),
                    b.simple("surname", Schema.customer_surname)
                ),
                b.complex("items", Schema.item_order.dual(),
                b.simple("quantity", Schema.item_quantity),
                b.simple("price", Schema.item_orderPrice),
                    b.simple("pid", Schema.product_productId),
                    b.simple("title", Schema.product_title)
                )
            )
        );
    }

}
