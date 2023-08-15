package cz.matfyz.tests.mapping;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.schema.BasicSchema;

public abstract class Neo4j {

    public static final String orderKind = "Order";
    public static final String itemKind = "ITEM";
    
    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.order,
            orderKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("customer", BasicSchema.orderToName),
                new SimpleProperty("number", BasicSchema.orderToNumber)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(schema,
            BasicSchema.item,
            itemKind,
            () -> ComplexProperty.createRoot(
                new SimpleProperty("quantity", BasicSchema.itemToQuantity),
                ComplexProperty.create("_from.Order", BasicSchema.itemToOrder,
                    new SimpleProperty("customer", BasicSchema.orderToName)
                ),
                ComplexProperty.create("_to.Product", BasicSchema.itemToProduct,
                    new SimpleProperty("id", BasicSchema.productToId),
                    new SimpleProperty("label", BasicSchema.productToLabel)
                )
            )
        );
    }

}
