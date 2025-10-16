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
    public static final String itemKind = "ITEM";
    public static final String noteKind = "Note";
    public static final String noteRelKind = "NOTE_REL";

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

    public static TestMapping item(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
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

    // Apparently specifying a relationship with _from and _to is sufficient for querying?

    public static TestMapping note(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.note,
            noteKind,
            b -> b.root(
                // b.simple("locale", Schema.noteToLocale),
                b.simple("subject", Schema.noteToData.signature().concatenate(Schema.dataToSubject.signature())),
                b.simple("content", Schema.noteToData.signature().concatenate(Schema.dataToContent.signature()))
            )
        );
    }

    public static TestMapping noteRel(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.note,
            noteRelKind,
            b -> b.root(
                // b.simple("locale", Schema.noteToLocale), // TODO: try if this works
                b.complex("_from.Note", Signature.createEmpty(),
                    b.simple("subject", Schema.noteToData.signature().concatenate(Schema.dataToSubject.signature())),
                    b.simple("content", Schema.noteToData.signature().concatenate(Schema.dataToContent.signature()))
                ),
                b.complex("_to.Order", Schema.noteToOrder,
                    b.simple("number", Schema.orderToNumber)
                )
            )
        );
    }

}
