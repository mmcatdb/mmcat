package cz.matfyz.tests.example.basic;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String orderKind = "Order";
    public static final String productKind = "Product";
    public static final String itemKind = "ITEM";
    public static final String contactKind = "Contact";
    public static final String hasContactKind = "HAS_CONTACT";
    // public static final String noteKind = "Note";
    // public static final String noteRelKind = "NOTE_REL";

    public static TestMapping order(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.order,
            orderKind,
            b -> b.root(
                b.simple("customer", Schema.orderToName),
                b.simple("number", Schema.orderToNumber)
            )
        );
    }

    public static TestMapping product(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.product,
            productKind,
            b -> b.root(
                b.simple("id", Schema.productToId),
                b.simple("label", Schema.productToLabel)
            )
        );
    }

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.item,
            itemKind,
            b -> b.root(
                b.simple("quantity", Schema.itemToQuantity),
                b.complex("_from.Order", Schema.itemToOrder,
                    b.simple("number", Schema.orderToNumber)
                ),
                b.complex("_to.Product", Schema.itemToProduct,
                    b.simple("id", Schema.productToId)
                )
            )
        );
    }

    // Apparently specifying a relationship with _from and _to is sufficient for querying?

    public static TestMapping contact(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.contact,
            contactKind,
            b -> b.root(
                b.simple("value", Schema.contactToValue)
            )
        );
    }

    public static TestMapping hasContact(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.contact,
            hasContactKind,
            b -> b.root(
                b.simple("type", Schema.contactToType),

                b.complex("_from.Order", Schema.contactToOrder,
                    b.simple("number", Schema.orderToNumber)
                ),
                b.complex("_to.Contact", Signature.createEmpty(),
                    b.simple("value", Schema.contactToValue)
                )
            )
        );
    }

    // public static TestMapping note(SchemaCategory schema) {
    //     return new TestMapping(datasource, schema,
    //         Schema.data,
    //         noteKind,
    //         b -> b.root(
    //             b.simple("subject", Schema.dataToSubject),
    //             b.simple("content", Schema.dataToContent)
    //         )
    //     );
    // }

    // public static TestMapping hasNote(SchemaCategory schema) {
    //     return new TestMapping(datasource, schema,
    //         Schema.note,
    //         noteRelKind,
    //         b -> b.root(
    //             b.simple("locale", Schema.noteToLocale),
    //             b.complex("_from.Note", Schema.noteToData),
    //             b.complex("_to.Order", Schema.noteToOrder)
    //         )
    //     );
    // }

}
