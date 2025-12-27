package cz.matfyz.evolution.instance;

import java.util.HashSet;
import java.util.Set;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.evolution.category.SMO;

/**
 * Represents dependencies between SMO operations for graph-based optimization.
 * 
 * Used to build a Directed Acyclic Graph (DAG) of operations where:
 * - Nodes = SMO operations
 * - Edges = dependencies (operation A must execute before operation B)
 * 
 * The graph enables:
 * 1. Topological sorting for valid execution order
 * 2. Detection of cancellation pairs (Create + Delete of same entity)
 * 3. Parallel execution of independent operations
 */
public class OperationDependency {

    private final SMO operation;
    
    /** Keys of objexes that must exist BEFORE this operation can execute. */
    private final Set<Key> requiresObjexes = new HashSet<>();
    
    /** Signatures of morphisms that must exist BEFORE this operation can execute. */
    private final Set<BaseSignature> requiresMorphisms = new HashSet<>();
    
    /** Keys of objexes that this operation CREATES (available after execution). */
    private final Set<Key> createsObjexes = new HashSet<>();
    
    /** Signatures of morphisms that this operation CREATES (available after execution). */
    private final Set<BaseSignature> createsMorphisms = new HashSet<>();
    
    /** Keys of objexes that this operation DELETES (unavailable after execution). */
    private final Set<Key> deletesObjexes = new HashSet<>();
    
    /** Signatures of morphisms that this operation DELETES (unavailable after execution). */
    private final Set<BaseSignature> deletesMorphisms = new HashSet<>();

    public OperationDependency(SMO operation) {
        this.operation = operation;
    }

    // === Builder methods ===

    public OperationDependency requiresObjex(Key key) {
        requiresObjexes.add(key);
        return this;
    }

    public OperationDependency requiresMorphism(BaseSignature signature) {
        requiresMorphisms.add(signature);
        return this;
    }

    public OperationDependency createsObjex(Key key) {
        createsObjexes.add(key);
        return this;
    }

    public OperationDependency createsMorphism(BaseSignature signature) {
        createsMorphisms.add(signature);
        return this;
    }

    public OperationDependency deletesObjex(Key key) {
        deletesObjexes.add(key);
        return this;
    }

    public OperationDependency deletesMorphism(BaseSignature signature) {
        deletesMorphisms.add(signature);
        return this;
    }

    // === Getters ===

    public SMO getOperation() {
        return operation;
    }

    public Set<Key> getRequiresObjexes() {
        return requiresObjexes;
    }

    public Set<BaseSignature> getRequiresMorphisms() {
        return requiresMorphisms;
    }

    public Set<Key> getCreatesObjexes() {
        return createsObjexes;
    }

    public Set<BaseSignature> getCreatesMorphisms() {
        return createsMorphisms;
    }

    public Set<Key> getDeletesObjexes() {
        return deletesObjexes;
    }

    public Set<BaseSignature> getDeletesMorphisms() {
        return deletesMorphisms;
    }

    // === Dependency analysis ===

    /**
     * Check if this operation MUST execute AFTER the other operation.
     * Returns true if this operation requires something that other creates.
     */
    public boolean dependsOn(OperationDependency other) {
        // This requires an objex that other creates
        for (Key key : requiresObjexes) {
            if (other.createsObjexes.contains(key)) {
                return true;
            }
        }
        // This requires a morphism that other creates
        for (BaseSignature sig : requiresMorphisms) {
            if (other.createsMorphisms.contains(sig)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this operation CONFLICTS with other (cannot be reordered).
     * Returns true if one deletes what the other requires.
     */
    public boolean conflictsWith(OperationDependency other) {
        // This deletes something other requires
        for (Key key : deletesObjexes) {
            if (other.requiresObjexes.contains(key)) {
                return true;
            }
        }
        for (BaseSignature sig : deletesMorphisms) {
            if (other.requiresMorphisms.contains(sig)) {
                return true;
            }
        }
        // Other deletes something this requires
        for (Key key : other.deletesObjexes) {
            if (requiresObjexes.contains(key)) {
                return true;
            }
        }
        for (BaseSignature sig : other.deletesMorphisms) {
            if (requiresMorphisms.contains(sig)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this operation CANCELS OUT with other.
     * E.g., CreateObjex(A) + DeleteObjex(A) = no-op if no data was added.
     */
    public boolean cancelsOutWith(OperationDependency other) {
        // This creates objex X and other deletes objex X
        Set<Key> createdThenDeleted = new HashSet<>(createsObjexes);
        createdThenDeleted.retainAll(other.deletesObjexes);
        if (!createdThenDeleted.isEmpty()) {
            return true;
        }
        
        // This creates morphism Y and other deletes morphism Y
        Set<BaseSignature> morphismCreatedThenDeleted = new HashSet<>(createsMorphisms);
        morphismCreatedThenDeleted.retainAll(other.deletesMorphisms);
        if (!morphismCreatedThenDeleted.isEmpty()) {
            return true;
        }
        
        return false;
    }

    @Override
    public String toString() {
        return String.format("OperationDependency[op=%s, requires=%s/%s, creates=%s/%s, deletes=%s/%s]",
            operation.getClass().getSimpleName(),
            requiresObjexes, requiresMorphisms,
            createsObjexes, createsMorphisms,
            deletesObjexes, deletesMorphisms);
    }
}
