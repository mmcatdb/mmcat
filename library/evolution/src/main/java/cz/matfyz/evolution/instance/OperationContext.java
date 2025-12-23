package cz.matfyz.evolution.instance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.MappingRow;

/**
 * Tracks the effects of a single operation execution.
 * 
 * Used for:
 * 1. Metrics - tracking what was affected (for logging/debugging)
 * 2. Reversibility - storing deleted data for potential undo
 * 3. Cost calculation - measuring the "expense" of an operation
 * 
 * Note: For dependency analysis and optimization, see {@link OperationDependency}
 * and {@link GraphBasedOptimizer}.
 */
public class OperationContext {

    // === Affected entities tracking ===
    
    private final Set<Key> affectedObjexKeys = new HashSet<>();
    private final Set<BaseSignature> affectedMorphismSignatures = new HashSet<>();
    
    private final List<DomainRow> deletedRows = new ArrayList<>();
    private final List<MappingRow> deletedMappings = new ArrayList<>();
    
    // === Metrics ===
    
    private int rowsDeleted = 0;
    private int mappingsDeleted = 0;
    private int objexesCreated = 0;
    private int objexesDeleted = 0;
    private int morphismsCreated = 0;
    private int morphismsDeleted = 0;
    
    // === Recording methods ===
    
    public void recordObjexCreation(Key key) {
        affectedObjexKeys.add(key);
        objexesCreated++;
    }
    
    public void recordObjexDeletion(Key key) {
        affectedObjexKeys.add(key);
        objexesDeleted++;
    }
    
    public void recordMorphismCreation(BaseSignature signature) {
        affectedMorphismSignatures.add(signature);
        morphismsCreated++;
    }
    
    public void recordMorphismDeletion(BaseSignature signature) {
        affectedMorphismSignatures.add(signature);
        morphismsDeleted++;
    }
    
    public void recordRowDeletion(DomainRow row) {
        deletedRows.add(row);
        rowsDeleted++;
    }
    
    public void recordMappingDeletion(MappingRow mapping) {
        deletedMappings.add(mapping);
        mappingsDeleted++;
    }
    
    public void recordObjexCopy(Key sourceKey, Key targetKey) {
        affectedObjexKeys.add(sourceKey);
        affectedObjexKeys.add(targetKey);
        // Copy is not counted as creation - it's tracked separately
    }
    
    public void recordMorphismCopy(BaseSignature sourceSignature, BaseSignature targetSignature) {
        affectedMorphismSignatures.add(sourceSignature);
        affectedMorphismSignatures.add(targetSignature);
        // Copy is not counted as creation - it's tracked separately
    }

    public void recordGroupOperation(Key parentKey, Key groupingKey, int movedMorphismsCount) {
        affectedObjexKeys.add(parentKey);
        affectedObjexKeys.add(groupingKey);
        // Group operation metrics are tracked via its sub-operations
        // (objex creation, morphism creation/deletion, etc.)
    }
    
    // === Getters for metrics ===
    
    public int getRowsDeleted() {
        return rowsDeleted;
    }
    
    public int getMappingsDeleted() {
        return mappingsDeleted;
    }
    
    public int getObjexesCreated() {
        return objexesCreated;
    }
    
    public int getObjexesDeleted() {
        return objexesDeleted;
    }
    
    public int getMorphismsCreated() {
        return morphismsCreated;
    }
    
    public int getMorphismsDeleted() {
        return morphismsDeleted;
    }
    
    public Set<Key> getAffectedObjexKeys() {
        return affectedObjexKeys;
    }
    
    public Set<BaseSignature> getAffectedMorphismSignatures() {
        return affectedMorphismSignatures;
    }
    
    /**
     * Calculate a cost metric for this operation.
     * Higher cost = more database work required.
     * 
     * Used for optimization comparison and logging.
     */
    public double calculateCost() {
        // Weights reflect relative expense of operations
        return rowsDeleted * 1.0 
             + mappingsDeleted * 0.5 
             + objexesCreated * 2.0
             + objexesDeleted * 10.0  // Deletes are more expensive (cleanup)
             + morphismsCreated * 1.0
             + morphismsDeleted * 5.0;
    }
    
    // === For reversibility (undo support) ===
    
    public List<DomainRow> getDeletedRows() {
        return deletedRows;
    }
    
    public List<MappingRow> getDeletedMappings() {
        return deletedMappings;
    }
    
    @Override
    public String toString() {
        return String.format(
            "OperationContext[created=%d/%d, deleted=%d/%d, rows=%d, mappings=%d, cost=%.2f]",
            objexesCreated, morphismsCreated,
            objexesDeleted, morphismsDeleted,
            rowsDeleted, mappingsDeleted, 
            calculateCost());
    }
}
