package cz.matfyz.server.example.tpch;

import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.server.example.common.SchemaBase;
import cz.matfyz.tests.example.tpch.Schema;

class SchemaSetup extends SchemaBase {

    private SchemaSetup(SchemaCategoryEntity categoryEntity) {
        super(categoryEntity, Schema.newSchema());
    }

    static SchemaEvolutionInit createNewUpdate(SchemaCategoryEntity categoryEntity) {
        return new SchemaSetup(categoryEntity).innerCreateNewUpdate();
    }

    @Override protected Position createPosition(double x, double y) {
        return new Position(x, y);
    }

    @Override protected void createOperations() {
        // Region
        addObjex(Schema.region, 0, 375);
        addProperty(Schema.rRegionkey, Schema.region_rRegionkey, 125, 375);
        addIds(Schema.region);

        addProperty(Schema.rName, Schema.region_rName, 110, 442);
        addProperty(Schema.rComment, Schema.region_rComment, 98, 304);

        // Nation
        addObjex(Schema.nation, 0, 125);
        addProperty(Schema.nNationkey, Schema.nation_nNationkey, -92, 292);
        addIds(Schema.nation);

        addProperty(Schema.nName, Schema.nation_nName, -156, 254);
        addProperty(Schema.nComment, Schema.nation_nComment, -206, 191);
        addMorphism(Schema.nation_region);

        // Supplier
        addObjex(Schema.supplier, 500, 125);
        addProperty(Schema.sSuppkey, Schema.supplier_sSuppkey, 582, -11);
        addIds(Schema.supplier);

        addProperty(Schema.sName, Schema.supplier_sName, 663, 54);
        addProperty(Schema.sAddress, Schema.supplier_sAddress, 661, 169);
        addProperty(Schema.sPhone, Schema.supplier_sPhone, 614, 245);
        addProperty(Schema.sAcctbal, Schema.supplier_sAcctbal, 521, 274);
        addProperty(Schema.sComment, Schema.supplier_sComment, 410, 237);
        addMorphism(Schema.supplier_nation);

        // Part
        addObjex(Schema.part, 1000, -125);
        addProperty(Schema.pPartkey, Schema.part_pPartkey, 833, -69);
        addIds(Schema.part);

        addProperty(Schema.pName, Schema.part_pName, 873, 1);
        addProperty(Schema.pMfgr, Schema.part_pMfgr, 970, 43);
        addProperty(Schema.pBrand, Schema.part_pBrand, 1078, 29);
        addProperty(Schema.pType, Schema.part_pType, 1167, -61);
        addProperty(Schema.pSize, Schema.part_pSize, 1160, -191);
        addProperty(Schema.pContainer, Schema.part_pContainer, 1056, -268);
        addProperty(Schema.pComment, Schema.part_pComment, 942, -289);
        addProperty(Schema.pRetailprice, Schema.part_pRetailprice, 847, -230);

        // PartSupp
        addObjex(Schema.partSupp, 500, -125);
        addMorphism(Schema.partSupp_part);
        addMorphism(Schema.partSupp_supplier);
        addIds(Schema.partSupp);

        addProperty(Schema.psAvailqty, Schema.partSupp_psAvailqty, 647, -240);
        addProperty(Schema.psSupplycost, Schema.partSupp_psSupplycost, 509, -286);
        addProperty(Schema.psComment, Schema.partSupp_psComment, 363, -248);

        // Customer
        addObjex(Schema.customer, -500, 125);
        addProperty(Schema.cCustkey, Schema.customer_cCustkey, -348, 179);
        addIds(Schema.customer);

        addProperty(Schema.cName, Schema.customer_cName, -381, 254);
        addProperty(Schema.cAddress, Schema.customer_cAddress, -449, 305);
        addProperty(Schema.cPhone, Schema.customer_cPhone, -555, 300);
        addProperty(Schema.cAcctbal, Schema.customer_cAcctbal, -621, 253);
        addProperty(Schema.cMktsegment, Schema.customer_cMktsegment, -626, 167);
        addProperty(Schema.cComment, Schema.customer_cComment, -613, 79);
        addMorphism(Schema.customer_nation);

        // Orders
        addObjex(Schema.orders, -500, -125);
        addProperty(Schema.oOrderkey, Schema.orders_oOrderkey, -442, -3);
        addIds(Schema.orders);

        addProperty(Schema.oOrderstatus, Schema.orders_oOrderstatus, -382, -69);
        addProperty(Schema.oTotalprice, Schema.orders_oTotalprice, -390, -193);
        addProperty(Schema.oOrderdate, Schema.orders_oOrderdate, -452, -260);
        addProperty(Schema.oOrderpriority, Schema.orders_oOrderpriority, -550, -256);
        addProperty(Schema.oShippriority, Schema.orders_oShippriority, -612, -191);
        addProperty(Schema.oClerk, Schema.orders_oClerk, -619, -97);
        addProperty(Schema.oComment, Schema.orders_oComment, -556, -15);
        addMorphism(Schema.orders_customer);

        // LineItem
        addObjex(Schema.lineItem, 0, -125);
        addMorphism(Schema.lineItem_orders);
        addProperty(Schema.lLinenumber, Schema.lineItem_lLinenumber, -200, -67);
        addIds(Schema.lineItem);

        addProperty(Schema.lQuantity, Schema.lineItem_lQuantity, -150, -4);
        addProperty(Schema.lExtendedprice, Schema.lineItem_lExtendedprice, -74, 18);
        addProperty(Schema.lDiscount, Schema.lineItem_lDiscount, 2, 29);
        addProperty(Schema.lTax, Schema.lineItem_lTax, 88, -0);
        addProperty(Schema.lReturnflag, Schema.lineItem_lReturnflag, 157, -59);
        addProperty(Schema.lLinestatus, Schema.lineItem_lLinestatus, 195, -155);
        addProperty(Schema.lShipdate, Schema.lineItem_lShipdate, 178, -226);
        addProperty(Schema.lCommitdate, Schema.lineItem_lCommitdate, 101, -279);
        addProperty(Schema.lReceiptdate, Schema.lineItem_lReceiptdate, 17, -300);
        addProperty(Schema.lShipinstruct, Schema.lineItem_lShipinstruct, -71, -288);
        addProperty(Schema.lShipmode, Schema.lineItem_lShipmode, -178, -258);
        addProperty(Schema.lComment, Schema.lineItem_lComment, -217, -179);
        addMorphism(Schema.lineItem_partSupp);
    }

}
