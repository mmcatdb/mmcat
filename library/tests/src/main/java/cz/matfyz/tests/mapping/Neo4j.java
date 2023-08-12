package cz.matfyz.tests.mapping;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.schema.TestSchema;

public abstract class Neo4j {

    public static final String orderKind = "Order";
    public static final String itemKind = "ITEM";
    
    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("customer", TestSchema.orderToName),
                new SimpleProperty("number", TestSchema.orderToNumber)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            TestSchema.item,
            itemKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("quantity", TestSchema.itemToQuantity),
                ComplexProperty.create("_from.Order", TestSchema.itemToOrder,
                    new SimpleProperty("customer", TestSchema.orderToName)
                ),
                ComplexProperty.create("_to.Product", TestSchema.itemToProduct,
                    new SimpleProperty("id", TestSchema.productToId),
                    new SimpleProperty("label", TestSchema.productToLabel)
                )
            )
        );
    }

}
