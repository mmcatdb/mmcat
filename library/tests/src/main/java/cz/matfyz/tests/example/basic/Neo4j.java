package cz.matfyz.tests.example.basic;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
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
            () -> ComplexProperty.createRoot(
                new SimpleProperty("customer", Schema.orderToName),
                new SimpleProperty("number", Schema.orderToNumber)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            Schema.item,
            itemKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("quantity", Schema.itemToQuantity),
                ComplexProperty.create("_from.Order", Schema.itemToOrder,
                    new SimpleProperty("customer", Schema.orderToName)
                ),
                ComplexProperty.create("_to.Product", Schema.itemToProduct,
                    new SimpleProperty("id", Schema.productToId),
                    new SimpleProperty("label", Schema.productToLabel)
                )
            )
        );
    }

}
