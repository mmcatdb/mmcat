package cz.matfyz.tests.example.basic;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final String orderKind = "Order";
    public static final String itemKind = "ITEM";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("customer", Schema.orderToName),
                b.simple("number", Schema.orderToNumber)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("quantity", Schema.itemToQuantity),
                b.complex("_from.Order", Schema.itemToOrder,
                    b.simple("customer", Schema.orderToName)
                ),
                b.complex("_to.Product", Schema.itemToProduct,
                    b.simple("id", Schema.productToId),
                    b.simple("label", Schema.productToLabel)
                )
            )
        );
    }

}
