package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.instance.GraphBasedOptimizer;
import cz.matfyz.evolution.instance.NoOpOptimizer;
import cz.matfyz.evolution.instance.OperationOptimizer.OptimizationComparison;
import cz.matfyz.evolution.instance.OperationOptimizer.OptimizationResult;

/**
 * Tests for the optimization comparison functionality.
 * These tests verify that we can measure the difference between
 * running with and without the GraphBasedOptimizer.
 * 
 * Key thesis metrics tested:
 * - operationsSaved(): How many operations were eliminated
 * - reductionRatio(): Percentage reduction in operations
 * - summary(): Human-readable comparison for thesis text
 */
class OptimizationComparisonTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void noOperations_noReduction() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(List.of());
        
        assertEquals(0, comparison.baseline().originalCount());
        assertEquals(0, comparison.optimized().optimizedCount());
        assertEquals(0, comparison.operationsSaved());
        assertEquals(0.0, comparison.reductionRatio(), 0.001);
    }

    @Test
    void singleOperation_noReduction() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Create a single operation
        final var createObjex = createObjexOperation(1000, "Entity1");
        List<SMO> operations = List.of(createObjex);
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(operations);
        
        assertEquals(1, comparison.baseline().originalCount());
        assertEquals(1, comparison.optimized().optimizedCount());
        assertEquals(0, comparison.operationsSaved());
        assertEquals(0.0, comparison.reductionRatio(), 0.001);
    }

    @Test
    void createDeletePair_fullCancellation() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Create an objex and then delete it - should be cancelled
        final var createObjex = createObjexOperation(1000, "TempEntity");
        final var deleteObjex = deleteObjexOperation(1000, "TempEntity");
        
        List<SMO> operations = List.of(createObjex, deleteObjex);
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(operations);
        
        // Baseline: 2 operations
        assertEquals(2, comparison.baseline().originalCount());
        // Optimized: 0 operations (both cancelled)
        assertEquals(0, comparison.optimized().optimizedCount());
        // Saved: 2 operations
        assertEquals(2, comparison.operationsSaved());
        // Reduction: 100%
        assertEquals(1.0, comparison.reductionRatio(), 0.001);
        assertEquals(2, comparison.optimized().cancelledCount());
    }

    @Test
    void multipleCancellations_metricsSummarized() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Create 3 objexes, delete 2 of them
        final var create1 = createObjexOperation(1000, "TempEntity1");
        final var create2 = createObjexOperation(1001, "TempEntity2");
        final var create3 = createObjexOperation(1002, "PermanentEntity");
        final var delete1 = deleteObjexOperation(1000, "TempEntity1");
        final var delete2 = deleteObjexOperation(1001, "TempEntity2");
        
        // Order: create1, create2, create3, delete1, delete2
        List<SMO> operations = List.of(create1, create2, create3, delete1, delete2);
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(operations);
        
        // Baseline: 5 operations
        assertEquals(5, comparison.baseline().originalCount());
        // Optimized: 1 operation (create3 remains, 4 cancelled)
        assertEquals(1, comparison.optimized().optimizedCount());
        // Saved: 4 operations
        assertEquals(4, comparison.operationsSaved());
        // Reduction: 80%
        assertEquals(0.8, comparison.reductionRatio(), 0.001);
        // 4 operations cancelled (2 pairs)
        assertEquals(4, comparison.optimized().cancelledCount());
    }

    @Test
    void morphismCancellation_alsoDetected() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Create a morphism and then delete it
        final var createMorphism = createMorphismOperation(Signature.createBase(500), new Key(1), new Key(2));
        final var deleteMorphism = deleteMorphismOperation(Signature.createBase(500));
        
        List<SMO> operations = List.of(createMorphism, deleteMorphism);
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(operations);
        
        assertEquals(2, comparison.baseline().originalCount());
        assertEquals(0, comparison.optimized().optimizedCount());
        assertEquals(2, comparison.operationsSaved());
        assertEquals(2, comparison.optimized().cancelledCount());
    }

    @Test
    void summary_containsAllMetrics() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Create cancellation scenario
        final var create = createObjexOperation(1000, "TempEntity");
        final var delete = deleteObjexOperation(1000, "TempEntity");
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(List.of(create, delete));
        String summary = comparison.summary();
        
        // Summary should contain key thesis metrics
        assertTrue(summary.contains("WITHOUT optimizer: 2 operations"), 
            "Should show baseline count: " + summary);
        assertTrue(summary.contains("WITH optimizer: 0 operations"), 
            "Should show optimized count: " + summary);
        assertTrue(summary.contains("saved 2 ops"), 
            "Should show operations saved: " + summary);
        assertTrue(summary.contains("100.0% reduction"), 
            "Should show reduction percentage: " + summary);
        assertTrue(summary.contains("2 cancellations"), 
            "Should show cancellation count: " + summary);
    }

    @Test
    void noOpOptimizer_zeroReduction() {
        NoOpOptimizer noOp = new NoOpOptimizer();
        
        // Even with a cancellation pair, NoOp should not optimize
        final var create = createObjexOperation(1000, "TempEntity");
        final var delete = deleteObjexOperation(1000, "TempEntity");
        
        OptimizationResult result = noOp.optimize(List.of(create, delete));
        
        assertEquals(2, result.originalCount());
        assertEquals(2, result.optimizedCount());
        assertEquals(0, result.cancelledCount());
        assertEquals(0.0, result.reductionRatio(), 0.001);
    }

    @Test
    void graphBasedOptimizer_detectsCancellation() {
        GraphBasedOptimizer graphBased = new GraphBasedOptimizer();
        
        // Same scenario - GraphBased should detect cancellation
        final var create = createObjexOperation(1000, "TempEntity");
        final var delete = deleteObjexOperation(1000, "TempEntity");
        
        OptimizationResult result = graphBased.optimize(List.of(create, delete));
        
        assertEquals(2, result.originalCount());
        assertEquals(0, result.optimizedCount());
        assertEquals(2, result.cancelledCount());
        assertEquals(1.0, result.reductionRatio(), 0.001);
    }

    @Test
    void compositeOperation_flattened() {
        GraphBasedOptimizer optimizer = new GraphBasedOptimizer();
        
        // Composite with create + delete inside
        final var create = createObjexOperation(1000, "TempEntity");
        final var delete = deleteObjexOperation(1000, "TempEntity");
        final var composite = new Composite("testComposite", List.of(create, delete));
        
        OptimizationComparison comparison = optimizer.compareWithBaseline(List.of(composite));
        
        // Composite is flattened: 1 composite -> 2 operations -> both cancelled
        assertEquals(1, comparison.baseline().originalCount()); // 1 composite
        assertEquals(0, comparison.optimized().optimizedCount()); // 0 after cancellation
        assertEquals(2, comparison.optimized().cancelledCount()); // 2 operations cancelled
    }

    // Helper methods to create test operations

    private CreateObjex createObjexOperation(int keyValue, String label) {
        final var key = new Key(keyValue);
        final var serializedObjex = new SerializedObjex(key, ObjexIds.fromIds(SignatureId.empty()));
        final var serializedMetadata = new SerializedMetadataObjex(key, label, null);
        return new CreateObjex(serializedObjex, serializedMetadata);
    }

    private DeleteObjex deleteObjexOperation(int keyValue, String label) {
        final var key = new Key(keyValue);
        final var serializedObjex = new SerializedObjex(key, ObjexIds.fromIds(SignatureId.empty()));
        final var serializedMetadata = new SerializedMetadataObjex(key, label, null);
        return new DeleteObjex(serializedObjex, serializedMetadata);
    }

    private CreateMorphism createMorphismOperation(BaseSignature signature, Key domKey, Key codKey) {
        final var serializedMorphism = new SerializedMorphism(signature, domKey, codKey, null, null);
        final var serializedMetadata = new SerializedMetadataMorphism(signature, null);
        return new CreateMorphism(serializedMorphism, serializedMetadata);
    }

    private DeleteMorphism deleteMorphismOperation(BaseSignature signature) {
        final var serializedMorphism = new SerializedMorphism(signature, new Key(1), new Key(2), null, null);
        final var serializedMetadata = new SerializedMetadataMorphism(signature, null);
        return new DeleteMorphism(serializedMorphism, serializedMetadata);
    }
}
