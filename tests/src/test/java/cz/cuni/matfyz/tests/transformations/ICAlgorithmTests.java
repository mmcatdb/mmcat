package cz.cuni.matfyz.tests.transformations;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.tests.mapping.PostgreSQL;
import cz.cuni.matfyz.tests.schema.TestSchema;

import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTests {

    private static final SchemaCategory schema = TestSchema.newSchemaCategory();

    @Test
    public void basicPrimaryKeyTest() {
        new ICAlgorithmTestBase()
            .primaryMapping(PostgreSQL.order(schema))
            .expected("""
                [
                    "appendIdentifier(order, [ number ])",
                    "createICStatement()"
                ]
            """)
            .run();
    }

    @Test
    public void complexPrimaryKeyTest() {
        new ICAlgorithmTestBase()
            .primaryMapping(PostgreSQL.item(schema))
            .expected("""
                [
                    "appendIdentifier(order_item, [ order_number, product_id ])",
                    "createICStatement()"
                ]
            """)
            .run();
    }

    @Test
    public void basicReferenceTest() {
        new ICAlgorithmTestBase()
            .primaryMapping(PostgreSQL.item(schema))
            .otherMappings(PostgreSQL.order(schema))
            .expected("""
                [
                    "appendIdentifier(order_item, [ order_number, product_id ])",
                    "appendReference(order_item, order, [ (order_number, number) ])",
                    "createICStatement()"
                ]
            """)
            .run();
    }

    @Test
    public void moreReferencesTest() {
        new ICAlgorithmTestBase()
            .primaryMapping(PostgreSQL.item(schema))
            .otherMappings(PostgreSQL.order(schema), PostgreSQL.product(schema))
            .expected("""
                [
                    "appendIdentifier(order_item, [ order_number, product_id ])",
                    "appendReference(order_item, product, [ (product_id, id) ])",
                    "appendReference(order_item, order, [ (order_number, number) ])",
                    "createICStatement()"
                ]
            """)
            .run();
    }

    // TODO complex reference
    // TODO se MTC

    // @Test
    // public void selfIdentifierTest() {
    //     schema = data.createDefaultV3SchemaCategory();
    //     mappings = new TreeMap<>();

    //     addMapping(data.orderKey, "order_v3", data.path_orderV3Root());

    //     testFunction(
    //         "order_v3"
    //     );
    // }

    // [
    //     "appendIdentifier(order_v3, [ id ])",
    //     "createICStatement()"
    // ]

}