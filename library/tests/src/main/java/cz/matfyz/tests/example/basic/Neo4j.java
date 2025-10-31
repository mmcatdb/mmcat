package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String orderKind = "Order";
    public static final String itemKind = "ITEM";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("customer", Schema.order_name),
                b.simple("number", Schema.order_number)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("quantity", Schema.item_quantity),
                b.complex("_from.Order", Schema.item_order,
                    b.simple("customer", Schema.order_name)
                ),
                b.complex("_to.Product", Schema.item_product,
                    b.simple("id", Schema.product_id),
                    b.simple("label", Schema.product_label)
                )
            )
        );
    }

}
