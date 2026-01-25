package cz.matfyz.tests.evolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests that verify data preservation during evolution operations.
 *
 * These tests ensure that when schema evolution operations are applied,
 * existing unrelated data remains intact.
 */
class DataPreservationTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    // === CreateObjex preserves existing data ===

    @Test
    void createObjexPreservesOrderData() {
        final var schema = Schema.newSchema();

        // Create a new objex that is unrelated to orders
        final var createObjex = EvolutionOperations.createObjex(1000, "TempEntity");
        final var createMorphism = EvolutionOperations.createMorphism(1001, 1000, 1002, "tempProp");
        final var createPropertyObjex = EvolutionOperations.createObjex(1002, "tempProp");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                // Rich data: orders with products and items
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_1", "Laptop", "999.99");
                PostgreSQL.addProduct(builder, "p_2", "Mouse", "29.99");
                PostgreSQL.addItem(builder, 0, 0, "2");  // 2 laptops for order o_100
                PostgreSQL.addItem(builder, 1, 1, "5");  // 5 mice for order o_200
            })
            .operation(createPropertyObjex)  // Create the property objex first
            .operation(createObjex)          // Create parent objex
            .operation(createMorphism)       // Create morphism connecting them
            .expectedInstance(builder -> {
                // Same data should exist - unchanged
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_1", "Laptop", "999.99");
                PostgreSQL.addProduct(builder, "p_2", "Mouse", "29.99");
                PostgreSQL.addItem(builder, 0, 0, "2");
                PostgreSQL.addItem(builder, 1, 1, "5");
                // New objex/morphism structures exist but are empty
            })
            .run();
    }

    @Test
    void createObjexPreservesCompleteOrderData() {
        final var schema = Schema.newSchema();

        // Create a new entity that doesn't interfere with order structure
        final var createObjex = EvolutionOperations.createObjex(1000, "NewEntity");
        final var createProp = EvolutionOperations.createObjex(1001, "newProp");
        final var createMorphism = EvolutionOperations.createMorphism(1002, 1000, 1001, "newProp");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                // Complete order with customer, address, contacts
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-001", "John Doe", "123 Main St", "New York", "10001");
                EvolutionInstanceData.addContactToOrder(builder, 0, "email", "john@example.com");
                EvolutionInstanceData.addContactToOrder(builder, 0, "phone", "+1-555-0123");
            })
            .operation(createProp)
            .operation(createObjex)
            .operation(createMorphism)
            .expectedInstance(builder -> {
                // All order data should be preserved
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-001", "John Doe", "123 Main St", "New York", "10001");
                EvolutionInstanceData.addContactToOrder(builder, 0, "email", "john@example.com");
                EvolutionInstanceData.addContactToOrder(builder, 0, "phone", "+1-555-0123");
            })
            .run();
    }

    // === DeleteObjex preserves unrelated data ===

    @Test
    void deleteObjexPreservesUnrelatedData() {
        final var schema = Schema.newSchema();

        // Create then delete a temporary objex - existing data should survive
        final var createObjex = EvolutionOperations.createObjex(1000, "TempEntity");
        final var createProp = EvolutionOperations.createObjex(1001, "tempProp");
        final var createMorphism = EvolutionOperations.createMorphism(1002, 1000, 1001, "tempProp");
        final var deleteMorphism = EvolutionOperations.deleteMorphism(1002, 1000, 1001, "tempProp");
        final var deleteObjex = EvolutionOperations.deleteObjex(1000, "TempEntity");
        final var deleteProp = EvolutionOperations.deleteObjex(1001, "tempProp");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                // Rich data with multiple entity types
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_1", "Widget", "10.00");
                PostgreSQL.addItem(builder, 0, 0, "10");
            })
            .operation(createProp)
            .operation(createObjex)
            .operation(createMorphism)
            // Now delete everything we just created
            .operation(deleteMorphism)
            .operation(deleteObjex)
            .operation(deleteProp)
            .expectedInstance(builder -> {
                // Original data should be completely intact
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_1", "Widget", "10.00");
                PostgreSQL.addItem(builder, 0, 0, "10");
            })
            .run();
    }

    // === CreateMorphism preserves existing data ===

    @Test
    void createMorphismBetweenExistingObjexesPreservesData() {
        final var schema = Schema.newSchema();

        // Create a new morphism between two new objexes,
        // which shouldn't affect existing order/product data
        final var createObjex1 = EvolutionOperations.createObjex(1000, "Entity1");
        final var createObjex2 = EvolutionOperations.createObjex(1001, "Entity2");
        final var createMorphism = EvolutionOperations.createMorphism(1002, 1000, 1001, "link");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addProduct(builder, "p_1", "Book", "15.99");
                PostgreSQL.addProduct(builder, "p_2", "Pen", "1.99");
                PostgreSQL.addItem(builder, 0, 0, "3");
                PostgreSQL.addItem(builder, 0, 1, "12");
            })
            .operation(createObjex2)  // Create codomain first (non-entity initially)
            .operation(createObjex1)  // Then domain (will become entity with morphism)
            .operation(createMorphism)
            .expectedInstance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addProduct(builder, "p_1", "Book", "15.99");
                PostgreSQL.addProduct(builder, "p_2", "Pen", "1.99");
                PostgreSQL.addItem(builder, 0, 0, "3");
                PostgreSQL.addItem(builder, 0, 1, "12");
            })
            .run();
    }

    // === DeleteMorphism preserves data on unrelated entities ===

    @Test
    void deleteMorphismPreservesOtherMorphismData() {
        final var schema = Schema.newSchema();

        // Create two morphisms, delete one, the other's data should be preserved
        final var createObjex1 = EvolutionOperations.createObjex(1000, "Entity1");
        final var createObjex2 = EvolutionOperations.createObjex(1001, "Prop1");
        final var createObjex3 = EvolutionOperations.createObjex(1002, "Prop2");
        final var createMorphism1 = EvolutionOperations.createMorphism(1003, 1000, 1001, "prop1");
        final var createMorphism2 = EvolutionOperations.createMorphism(1004, 1000, 1002, "prop2");
        final var deleteMorphism1 = EvolutionOperations.deleteMorphism(1003, 1000, 1001, "prop1");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addProduct(builder, "p_1", "Phone", "599.99");
                PostgreSQL.addItem(builder, 0, 0, "1");
            })
            .operation(createObjex2)
            .operation(createObjex3)
            .operation(createObjex1)
            .operation(createMorphism1)
            .operation(createMorphism2)
            .operation(deleteMorphism1)  // Delete one morphism
            .expectedInstance(builder -> {
                // Original data preserved, morphism2 still exists (empty)
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addProduct(builder, "p_1", "Phone", "599.99");
                PostgreSQL.addItem(builder, 0, 0, "1");
            })
            .run();
    }

    // === Complex scenarios with multiple entity types ===

    @Test
    void multipleOperationsPreserveComplexDataGraph() {
        final var schema = Schema.newSchema();

        // Create multiple new entities and morphisms
        final var createParent = EvolutionOperations.createObjex(1000, "Parent");
        final var createChild1 = EvolutionOperations.createObjex(1001, "Child1");
        final var createChild2 = EvolutionOperations.createObjex(1002, "Child2");
        final var createMorphism1 = EvolutionOperations.createMorphism(1003, 1000, 1001, "child1");
        final var createMorphism2 = EvolutionOperations.createMorphism(1004, 1000, 1002, "child2");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                // Complex interconnected data
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-100", "Alice Smith", "100 Oak Ave", "Boston", "02101");
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-200", "Bob Jones", "200 Pine St", "Chicago", "60601");

                // Add items to both orders
                PostgreSQL.addProduct(builder, "SKU-001", "Keyboard", "79.99");
                PostgreSQL.addProduct(builder, "SKU-002", "Monitor", "299.99");

                EvolutionInstanceData.addItemToOrder(builder, 0, "SKU-003", "Headphones", "49.99", "2");
                EvolutionInstanceData.addItemToOrder(builder, 1, "SKU-004", "Webcam", "89.99", "1");

                // Add contacts
                EvolutionInstanceData.addContactToOrder(builder, 0, "email", "alice@corp.com");
                EvolutionInstanceData.addContactToOrder(builder, 1, "phone", "+1-555-9999");
            })
            .operation(createChild1)
            .operation(createChild2)
            .operation(createParent)
            .operation(createMorphism1)
            .operation(createMorphism2)
            .expectedInstance(builder -> {
                // All original data should be preserved
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-100", "Alice Smith", "100 Oak Ave", "Boston", "02101");
                EvolutionInstanceData.addCompleteOrder(builder, "ORD-200", "Bob Jones", "200 Pine St", "Chicago", "60601");

                PostgreSQL.addProduct(builder, "SKU-001", "Keyboard", "79.99");
                PostgreSQL.addProduct(builder, "SKU-002", "Monitor", "299.99");

                EvolutionInstanceData.addItemToOrder(builder, 0, "SKU-003", "Headphones", "49.99", "2");
                EvolutionInstanceData.addItemToOrder(builder, 1, "SKU-004", "Webcam", "89.99", "1");

                EvolutionInstanceData.addContactToOrder(builder, 0, "email", "alice@corp.com");
                EvolutionInstanceData.addContactToOrder(builder, 1, "phone", "+1-555-9999");
            })
            .run();
    }

    @Test
    void createAndDeleteCyclePreservesAllData() {
        final var schema = Schema.newSchema();

        // Full cycle: create entities, then delete them all
        final var createObjex1 = EvolutionOperations.createObjex(1000, "Temp1");
        final var createObjex2 = EvolutionOperations.createObjex(1001, "Temp2");
        final var createMorphism = EvolutionOperations.createMorphism(1002, 1000, 1001, "link");
        final var deleteMorphism = EvolutionOperations.deleteMorphism(1002, 1000, 1001, "link");
        final var deleteObjex1 = EvolutionOperations.deleteObjex(1000, "Temp1");
        final var deleteObjex2 = EvolutionOperations.deleteObjex(1001, "Temp2");

        new EvolutionTestBase()
            .schema(schema)
            .initialInstance(builder -> {
                PostgreSQL.addOrder(builder, "o_001");
                PostgreSQL.addOrder(builder, "o_002");
                PostgreSQL.addOrder(builder, "o_003");
                PostgreSQL.addProduct(builder, "p_A", "Product A", "100");
                PostgreSQL.addProduct(builder, "p_B", "Product B", "200");
                PostgreSQL.addItem(builder, 0, 0, "5");
                PostgreSQL.addItem(builder, 1, 1, "3");
                PostgreSQL.addItem(builder, 2, 0, "7");
            })
            // Create cycle
            .operation(createObjex2)
            .operation(createObjex1)
            .operation(createMorphism)
            // Delete cycle
            .operation(deleteMorphism)
            .operation(deleteObjex1)
            .operation(deleteObjex2)
            .expectedInstance(builder -> {
                // Schema returns to original state, all data preserved
                PostgreSQL.addOrder(builder, "o_001");
                PostgreSQL.addOrder(builder, "o_002");
                PostgreSQL.addOrder(builder, "o_003");
                PostgreSQL.addProduct(builder, "p_A", "Product A", "100");
                PostgreSQL.addProduct(builder, "p_B", "Product B", "200");
                PostgreSQL.addItem(builder, 0, 0, "5");
                PostgreSQL.addItem(builder, 1, 1, "3");
                PostgreSQL.addItem(builder, 2, 0, "7");
            })
            .run();
    }
}
