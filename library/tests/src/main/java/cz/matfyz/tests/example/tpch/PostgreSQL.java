package cz.matfyz.tests.example.tpch;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String ordersKind = "Orders";
    public static final String lineItemKind = "LineItem";

    public static TestMapping orders(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.orders,
            ordersKind,
            b -> b.root(
                b.simple("orderkey", Schema.orders_oOrderkey),
                b.simple("orderstatus", Schema.orders_oOrderstatus),
                b.simple("totalprice", Schema.orders_oTotalprice),
                b.simple("orderdate", Schema.orders_oOrderdate),
                b.simple("orderpriority", Schema.orders_oOrderpriority),
                b.simple("shippriority", Schema.orders_oShippriority),
                b.simple("clerk", Schema.orders_oClerk),
                b.simple("comment", Schema.orders_oComment),
                b.simple("custkey", Schema.orders_cCustkey)
            )
        );
    }

    public static TestMapping lineItem(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.lineItem,
            lineItemKind,
            b -> b.root(
                b.simple("linenumber", Schema.lineItem_lLinenumber),
                b.simple("quantity", Schema.lineItem_lQuantity),
                b.simple("extendedprice", Schema.lineItem_lExtendedprice),
                b.simple("discount", Schema.lineItem_lDiscount),
                b.simple("tax", Schema.lineItem_lTax),
                b.simple("returnflag", Schema.lineItem_lReturnflag),
                b.simple("linestatus", Schema.lineItem_lLinestatus),
                b.simple("shipdate", Schema.lineItem_lShipdate),
                b.simple("commitdate", Schema.lineItem_lCommitdate),
                b.simple("receiptdate", Schema.lineItem_lReceiptdate),
                b.simple("shipinstruct", Schema.lineItem_lShipinstruct),
                b.simple("shipmode", Schema.lineItem_lShipmode),
                b.simple("comment", Schema.lineItem_lComment),
                b.simple("orderkey", Schema.lineItem_oOrderkey),
                b.simple("partkey", Schema.lineItem_pPartkey),
                b.simple("suppkey", Schema.lineItem_sSuppkey)
            )
        );
    }

}
