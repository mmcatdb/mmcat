package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTests {

    private TestData data;
    private SchemaCategory schema;
    private Map<String, Mapping> mappings;

    @BeforeEach
    public void setupTestData() {
        data = new TestData();
        schema = data.createDefaultSchemaCategory();
        mappings = new TreeMap<>();

        addMapping(data.orderKey, "order", data.path_orderRoot());
        addMapping(data.contactKey, "contact", data.path_contactRoot());
        addMapping(data.orderedKey, "ordered", data.path_orderedRoot());
        addMapping(data.customerKey, "customer", data.path_customerRoot());
    }

    private void addMapping(Key key, String name, ComplexProperty path) {
        final var mapping = ICAlgorithmTestBase.createMapping(schema, schema.getObject(key), name, path);
        mappings.put(name, mapping);
    }

    private void testFunction(String dataFileName, String primaryMappingName, String... otherMappingNames) {
        new ICAlgorithmTestBase(dataFileName)
            .setPrimaryMapping(mappings.get(primaryMappingName))
            .addOtherMappings(List.of(otherMappingNames).stream().map(name -> mappings.get(name)).toArray(Mapping[]::new))
            .testAlgorithm();
    }

    @Test
    public void basicPrimaryKeyTest() {
        testFunction(
            "1BasicPrimaryKey.json",
            "order"
        );
    }

    @Test
    public void complexPrimaryKeyTest() {
        testFunction(
            "2ComplexPrimaryKey.json",
            "contact"
        );
    }

    @Test
    public void complexReferenceTest() {
        testFunction(
            "3BasicReference.json",
            "contact",
            "order"
        );
    }

    @Test
    public void moreReferencesTest() {
        testFunction(
            "4MoreReferences.json",
            "ordered",
            "order", "customer"
        );
    }

    // TODO complex reference

    @Test
    public void selfIdentifierTest() {
        schema = data.createDefaultV3SchemaCategory();
        mappings = new TreeMap<>();

        addMapping(data.orderKey, "order_v3", data.path_orderV3Root());

        testFunction(
            "12SelfIdentifier.json",
            "order_v3"
        );
    }

}