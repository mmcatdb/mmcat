package cz.matfyz.tests.example.tpch;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper.Neo4jNames;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String supplierKind = "Supplier";
    public static final String partKind = "Part";
    public static final String partSuppKind = "PartSupp";
    public static final String suppliedByKind = "SUPPLIED_BY";
    public static final String isForPartKind = "IS_FOR_PART";

    public static TestMapping supplier(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.supplier,
            supplierKind,
            b -> b.root(
                b.simple("suppkey", Schema.supplier_sSuppkey),
                b.simple("name", Schema.supplier_sName),
                b.simple("address", Schema.supplier_sAddress),
                b.simple("phone", Schema.supplier_sPhone),
                b.simple("acctbal", Schema.supplier_sAcctbal),
                b.simple("comment", Schema.supplier_sComment),
                b.simple("nationKey", Schema.supplier_nNationkey)
            )
        );
    }

    public static TestMapping part(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.part,
            partKind,
            b -> b.root(
                b.simple("partkey", Schema.part_pPartkey),
                b.simple("name", Schema.part_pName),
                b.simple("mfgr", Schema.part_pMfgr),
                b.simple("brand", Schema.part_pBrand),
                b.simple("type", Schema.part_pType),
                b.simple("size", Schema.part_pSize),
                b.simple("container", Schema.part_pContainer),
                b.simple("comment", Schema.part_pComment),
                b.simple("retailprice", Schema.part_pRetailprice)
            )
        );
    }

    public static TestMapping partSupp(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.partSupp,
            partSuppKind,
            b -> b.root(
                b.simple("availqty", Schema.partSupp_psAvailqty),
                b.simple("supplycost", Schema.partSupp_psSupplycost),
                b.simple("comment", Schema.partSupp_psComment),
                b.simple("partkey", Schema.partSupp_pPartkey),
                b.simple("suppkey", Schema.partSupp_sSuppkey)
            )
        );
    }

    public static TestMapping suppliedBy(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.partSupp,
            suppliedByKind,
            b -> b.root(
                b.complex(Neo4jNames.from(partSuppKind), Signature.empty(),
                    b.simple("partkey", Schema.partSupp_pPartkey),
                    b.simple("suppkey", Schema.partSupp_sSuppkey)
                ),
                b.complex(Neo4jNames.to(supplierKind), Schema.partSupp_supplier,
                    b.simple("suppkey", Schema.supplier_sSuppkey)
                )
            )
        );
    }

    public static TestMapping isForPart(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.partSupp,
            isForPartKind,
            b -> b.root(
                b.complex(Neo4jNames.from(partSuppKind), Signature.empty(),
                    b.simple("partkey", Schema.partSupp_pPartkey),
                    b.simple("suppkey", Schema.partSupp_sSuppkey)
                ),
                b.complex(Neo4jNames.to(partKind), Schema.partSupp_part,
                    b.simple("partkey", Schema.part_pPartkey)
                )
            )
        );
    }

}
