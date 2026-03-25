package cz.matfyz.tests.example.tpch;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;

public abstract class Schema {

    public static final String schemaLabel = "TPC-H Schema";

    private static final SchemaBuilder b = new SchemaBuilder();

    // Why is this so ugly? Ask the authors of the TPC-H benchmark why they are so ugly.

    // Keys

    public static final BuilderObjex
        region =            b.objex("Region", 10),
        rRegionkey =        b.objex("regionkey", 11),
        rName =             b.objex("name", 12),
        rComment =          b.objex("comment", 13),

        nation =            b.objex("Nation", 20),
        nNationkey =        b.objex("nationkey", 21),
        nName =             b.objex("name", 22),
        nComment =          b.objex("comment", 23),
        // nRegionkey

        supplier =          b.objex("Supplier", 30),
        sSuppkey =          b.objex("suppkey", 31),
        sName =             b.objex("name", 32),
        sAddress =          b.objex("address", 33),
        sPhone =            b.objex("phone", 34),
        sAcctbal =          b.objex("acctbal", 35),
        sComment =          b.objex("comment", 36),
        // sNationkey

        part =              b.objex("Part", 40),
        pPartkey =          b.objex("partkey", 41),
        pName =             b.objex("name", 42),
        pMfgr =             b.objex("mfgr", 43),
        pBrand =            b.objex("brand", 44),
        pType =             b.objex("type", 45),
        pSize =             b.objex("size", 46),
        pContainer =        b.objex("container", 47),
        pComment =          b.objex("comment", 48),
        pRetailprice =      b.objex("retailprice", 49),

        partSupp =          b.objex("PartSupp", 50),
        psAvailqty =        b.objex("availqty", 51),
        psSupplycost =      b.objex("supplycost", 52),
        psComment =         b.objex("comment", 53),
        // psPartkey
        // psSuppkey

        customer =          b.objex("Customer", 60),
        cCustkey =          b.objex("custkey", 61),
        cName =             b.objex("name", 62),
        cAddress =          b.objex("address", 63),
        cPhone =            b.objex("phone", 64),
        cAcctbal =          b.objex("acctbal", 65),
        cMktsegment =       b.objex("mktsegment", 66),
        cComment =          b.objex("comment", 67),
        // cNationkey

        orders =            b.objex("Orders", 70),
        oOrderkey =         b.objex("orderkey", 71),
        oOrderstatus =      b.objex("orderstatus", 72),
        oTotalprice =       b.objex("totalprice", 73),
        oOrderdate =        b.objex("orderdate", 74),
        oOrderpriority =    b.objex("orderpriority", 75),
        oShippriority =     b.objex("shippriority", 76),
        oClerk =            b.objex("clerk", 77),
        oComment =          b.objex("comment", 78),
        // oCustkey

        lineItem =          b.objex("LineItem", 80),
        lLinenumber =       b.objex("linenumber", 81),
        lQuantity =         b.objex("quantity", 82),
        lExtendedprice =    b.objex("extendedprice", 83),
        lDiscount =         b.objex("discount", 84),
        lTax =              b.objex("tax", 85),
        lReturnflag =       b.objex("returnflag", 86),
        lLinestatus =       b.objex("linestatus", 87),
        lShipdate =         b.objex("shipdate", 88),
        lCommitdate =       b.objex("commitdate", 89),
        lReceiptdate =      b.objex("receiptdate", 90),
        lShipinstruct =     b.objex("shipinstruct", 91),
        lShipmode =         b.objex("shipmode", 92),
        lComment =          b.objex("comment", 93);
        // lOrderkey
        // lPartkey
        // lSuppkey

    // Signatures

    public static final BuilderMorphism
        region_rRegionkey =         b.morphism(region, rRegionkey, 10),
        region_rName =              b.morphism(region, rName, 11),
        region_rComment =           b.morphism(region, rComment, 12),

        nation_nNationkey =         b.morphism(nation, nNationkey, 20),
        nation_nName =              b.morphism(nation, nName, 21),
        nation_nComment =           b.morphism(nation, nComment, 22),
        nation_region =             b.morphism(nation, region, 23),

        supplier_sSuppkey =         b.morphism(supplier, sSuppkey, 30),
        supplier_sName =            b.morphism(supplier, sName, 31),
        supplier_sAddress =         b.morphism(supplier, sAddress, 32),
        supplier_sPhone =           b.morphism(supplier, sPhone, 33),
        supplier_sAcctbal =         b.morphism(supplier, sAcctbal, 34),
        supplier_sComment =         b.morphism(supplier, sComment, 35),
        supplier_nation =           b.morphism(supplier, nation, 36),

        part_pPartkey =             b.morphism(part, pPartkey, 40),
        part_pName =                b.morphism(part, pName, 41),
        part_pMfgr =                b.morphism(part, pMfgr, 42),
        part_pBrand =               b.morphism(part, pBrand, 43),
        part_pType =                b.morphism(part, pType, 44),
        part_pSize =                b.morphism(part, pSize, 45),
        part_pContainer =           b.morphism(part, pContainer, 46),
        part_pComment =             b.morphism(part, pComment, 47),
        part_pRetailprice =         b.morphism(part, pRetailprice, 48),

        partSupp_psAvailqty =       b.morphism(partSupp, psAvailqty, 50),
        partSupp_psSupplycost =     b.morphism(partSupp, psSupplycost, 51),
        partSupp_psComment =        b.morphism(partSupp, psComment, 52),
        partSupp_part =             b.morphism(partSupp, part, 53),
        partSupp_supplier =         b.morphism(partSupp, supplier, 54),

        customer_cCustkey =         b.morphism(customer, cCustkey, 60),
        customer_cName =            b.morphism(customer, cName, 61),
        customer_cAddress =         b.morphism(customer, cAddress, 62),
        customer_cPhone =           b.morphism(customer, cPhone, 63),
        customer_cAcctbal =         b.morphism(customer, cAcctbal, 64),
        customer_cMktsegment =      b.morphism(customer, cMktsegment, 65),
        customer_cComment =         b.morphism(customer, cComment, 66),
        customer_nation =           b.morphism(customer, nation, 67),

        orders_oOrderkey =          b.morphism(orders, oOrderkey, 70),
        orders_oOrderstatus =       b.morphism(orders, oOrderstatus, 71),
        orders_oTotalprice =        b.morphism(orders, oTotalprice, 72),
        orders_oOrderdate =         b.morphism(orders, oOrderdate, 73),
        orders_oOrderpriority =     b.morphism(orders, oOrderpriority, 74),
        orders_oShippriority =      b.morphism(orders, oShippriority, 75),
        orders_oClerk =             b.morphism(orders, oClerk, 76),
        orders_oComment =           b.morphism(orders, oComment, 77),
        orders_customer =           b.morphism(orders, customer, 78),

        lineItem_lLinenumber =      b.morphism(lineItem, lLinenumber, 80),
        lineItem_lQuantity =        b.morphism(lineItem, lQuantity, 81),
        lineItem_lExtendedprice =   b.morphism(lineItem, lExtendedprice, 82),
        lineItem_lDiscount =        b.morphism(lineItem, lDiscount, 83),
        lineItem_lTax =             b.morphism(lineItem, lTax, 84),
        lineItem_lReturnflag =      b.morphism(lineItem, lReturnflag, 85),
        lineItem_lLinestatus =      b.morphism(lineItem, lLinestatus, 86),
        lineItem_lShipdate =        b.morphism(lineItem, lShipdate, 87),
        lineItem_lCommitdate =      b.morphism(lineItem, lCommitdate, 88),
        lineItem_lReceiptdate =     b.morphism(lineItem, lReceiptdate, 89),
        lineItem_lShipinstruct =    b.morphism(lineItem, lShipinstruct, 90),
        lineItem_lShipmode =        b.morphism(lineItem, lShipmode, 91),
        lineItem_lComment =         b.morphism(lineItem, lComment, 92),
        lineItem_orders =           b.morphism(lineItem, orders, 93),
        lineItem_partSupp =         b.morphism(lineItem, partSupp, 94);

    public static final Signature
        nation_rRegionKey =     b.concatenate(nation_region, region_rRegionkey),

        supplier_nNationkey =   b.concatenate(supplier_nation, nation_nNationkey),

        partSupp_pPartkey =     b.concatenate(partSupp_part, part_pPartkey),
        partSupp_sSuppkey =     b.concatenate(partSupp_supplier, supplier_sSuppkey),

        customer_nNationkey =   b.concatenate(customer_nation, nation_nNationkey),

        orders_cCustkey =       b.concatenate(orders_customer, customer_cCustkey),

        lineItem_oOrderkey =    b.concatenate(lineItem_orders, orders_oOrderkey),
        lineItem_pPartkey =     b.concatenate(lineItem_partSupp, partSupp_pPartkey),
        lineItem_sSuppkey =     b.concatenate(lineItem_partSupp, partSupp_sSuppkey);

    static {
        b
            .ids(region, region_rRegionkey)
            .ids(nation, nation_nNationkey)
            .ids(supplier, supplier_sSuppkey)
            .ids(part, part_pPartkey)
            .ids(partSupp, partSupp_pPartkey, partSupp_sSuppkey)
            .ids(customer, customer_cCustkey)
            .ids(orders, orders_oOrderkey)
            .ids(lineItem, lineItem_oOrderkey, lineItem_lLinenumber);
    }

    // #endregion

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return b.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return b.buildMetadata(schema);
    }

    private Schema() {}

}
