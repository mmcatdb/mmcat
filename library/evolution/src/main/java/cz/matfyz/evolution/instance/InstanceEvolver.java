package cz.matfyz.evolution.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.core.instance.MappingRow;
import cz.matfyz.core.instance.SuperIdValues;
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CopyObjex;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.evolution.category.GroupProperties;
import cz.matfyz.evolution.category.JoinProperties;
import cz.matfyz.evolution.category.MoveProperty;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.category.SchemaEvolutionVisitor;
import cz.matfyz.evolution.category.SplitProperty;
import cz.matfyz.evolution.category.UngroupProperty;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.evolution.category.UpdateObjex;

/**
 * Propagates schema evolution operations to the instance category.
 * 
 * Design principles for future RL-based operation ordering optimization:
 * 1. Each operation is ATOMIC - does exactly one thing, no hidden cascades
 * 2. Operations track their EFFECTS via OperationContext for metrics/RL rewards
 * 3. Operations declare DEPENDENCIES for RL state representation
 * 4. Operations are REVERSIBLE - track deleted data for potential undo
 * 
 * The higher-level planner (SchemaEvolutionAlgorithm) is responsible for
 * generating dependent operations (e.g., when deleting an objex, it should
 * first generate DeleteMorphism operations for all connected morphisms).
 * This separation allows RL to optimize the ordering of atomic operations.
 */
public class InstanceEvolver extends InstanceCategory.Editor implements SchemaEvolutionVisitor<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceEvolver.class);
    
    private final InstanceCategory instance;
    
    /** Contexts for each executed operation - for RL metrics and reversibility. */
    private final List<OperationContext> executedContexts = new ArrayList<>();
    
    /** Current operation's context. */
    private OperationContext currentContext;

    public InstanceEvolver(InstanceCategory instance) {
        this.instance = instance;
    }
    
    /**
     * Get all operation contexts for RL training/evaluation.
     * Each context contains metrics that can be used as reward signals.
     */
    public List<OperationContext> getExecutedContexts() {
        return executedContexts;
    }
    
    /**
     * Calculate total cost of all operations - for RL reward.
     */
    public double getTotalCost() {
        return executedContexts.stream()
            .mapToDouble(OperationContext::calculateCost)
            .sum();
    }
    
    private void beginOperation() {
        currentContext = new OperationContext();
    }
    
    private void endOperation() {
        executedContexts.add(currentContext);
        currentContext = null;
    }

    /**
     * Copy mappings from source morphism to target morphism, mapping domain rows appropriately.
     */
    private void copyMappings(InstanceMorphism sourceMorphism, InstanceMorphism targetMorphism, 
                             InstanceObjex sourceObjex, InstanceObjex targetObjex) {
        // Create a map from source superIds to target domain rows
        final var sourceToTargetRows = new java.util.HashMap<SuperIdValues, DomainRow>();
        for (final var targetRow : targetObjex.allRows()) {
            // Find corresponding source row by superId
            final var sourceRow = java.util.stream.StreamSupport.stream(sourceObjex.allRows().spliterator(), false)
                .filter(r -> r.superId.equals(targetRow.superId))
                .findFirst();
            if (sourceRow.isPresent()) {
                sourceToTargetRows.put(sourceRow.get().superId, targetRow);
            }
        }
        
        // Copy each mapping from source to target
        for (final var sourceMapping : getAllMappings(sourceMorphism)) {
            final var sourceDomainRow = sourceMapping.dom();
            final var codomainRow = sourceMapping.cod();
            
            // Find the corresponding target domain row
            final var targetDomainRow = sourceToTargetRows.get(sourceDomainRow.superId);
            if (targetDomainRow != null) {
                // Create the mapping in the target morphism
                targetMorphism.createMapping(targetDomainRow, codomainRow);
            }
        }
    }

    /**
     * Get all outgoing morphisms from an objex.
     */
    private List<InstanceMorphism> getOutgoingMorphisms(Key objexKey) {
        final var result = new ArrayList<InstanceMorphism>();
        for (final var morphism : getMorphisms(instance).values()) {
            if (morphism.schema.dom().key().equals(objexKey)) {
                result.add(morphism);
            }
        }
        return result;
    }

    @Override public Void visit(Composite operation) {
        // Composite operations delegate to their children
        // The RL system can potentially reorder children
        for (SMO child : operation.children()) {
            child.accept(this);
        }
        return null;
    }

    @Override public Void visit(CreateObjex operation) {
        beginOperation();
        
        final var key = operation.schema().key();
        final var schemaObjex = instance.schema().getObjex(key);
        
        // If schemaObjex is null, it means this CreateObjex was followed by a DeleteObjex
        // in the same batch (Composite) - the schema changes already cancelled out.
        // We can safely skip instance creation since the schema objex doesn't exist.
        if (schemaObjex == null) {
            LOGGER.debug("CreateObjex: skipping - schemaObjex for key {} not found (likely cancelled)", key);
            endOperation();
            return null;
        }
        
        // Only create InstanceObjex for entity objexes (those with outgoing morphisms)
        // Non-entity objexes (properties/values) are handled differently via morphism mappings
        if (schemaObjex.isEntity()) {
            // Create empty instance objex
            // Note: Set.of() for dependentObjexes - this may need to be computed
            // from the schema morphisms in a more complete implementation
            final var instanceObjex = new InstanceObjex(schemaObjex, instance, Set.of());
            getObjexes(instance).put(key, instanceObjex);
        }
        
        currentContext.recordObjexCreation(key);
        
        endOperation();
        return null;
    }

    /**
     * Delete an objex from the instance category.
     * 
     * IMPORTANT: This is an ATOMIC operation - it only deletes the objex itself.
     * It does NOT automatically cascade to delete related morphisms or rows.
     * 
     * The higher-level planner MUST generate DeleteMorphism operations for all
     * morphisms connected to this objex BEFORE calling DeleteObjex.
     * 
     * This design allows the RL system to:
     * 1. Learn optimal ordering of delete operations
     * 2. Potentially skip unnecessary deletes (if create+delete cancel out)
     * 3. Batch similar operations together
     */
    @Override public Void visit(DeleteObjex operation) {
        beginOperation();
        
        final var key = operation.schema().key();
        
        final var instanceObjex = instance.getObjex(key);
        if (instanceObjex != null) {
            // Record all rows being deleted (for reversibility and metrics)
            for (DomainRow row : instanceObjex.allRows()) {
                currentContext.recordRowDeletion(row);
                
                // Clean up any remaining mapping references from/to this row
                // This handles edge cases where morphism wasn't deleted first
                cleanupRowMappings(row);
            }
            
            // Remove the objex from the instance
            getObjexes(instance).remove(key);
            currentContext.recordObjexDeletion(key);
        }
        
        endOperation();
        return null;
    }
    
    /**
     * Clean up all mappings connected to a row.
     * This is a safety measure - ideally morphisms are deleted first.
     */
    private void cleanupRowMappings(DomainRow row) {
        // Clean up mappings FROM this row
        for (var entry : new ArrayList<>(row.getAllMappingsFrom())) {
            BaseSignature signature = entry.getKey();
            MappingRow mapping = entry.getValue();
            
            InstanceMorphism morphism = getMorphisms(instance).get(signature);
            if (morphism != null) {
                removeMapping(morphism, mapping);
                currentContext.recordMappingDeletion(mapping);
            }
        }
        
        // Clean up mappings TO this row
        for (var entry : new ArrayList<>(row.getAllMappingsTo())) {
            BaseSignature signature = entry.getKey();
            Set<MappingRow> mappings = entry.getValue();
            
            InstanceMorphism morphism = getMorphisms(instance).get(signature);
            if (morphism != null) {
                for (MappingRow mapping : new ArrayList<>(mappings)) {
                    removeMapping(morphism, mapping);
                    currentContext.recordMappingDeletion(mapping);
                }
            }
        }
    }

    @Override public Void visit(UpdateObjex operation) {
        beginOperation();
        
        // UpdateObjex typically changes metadata/properties, not structure
        // The instance objex itself doesn't need modification for most updates
        // This is intentionally minimal - schema changes are handled by up()
        
        final var key = operation.newObjex().key();
        // UpdateObjex doesn't create/delete, just marks as affected
        
        endOperation();
        return null;
    }

    @Override public Void visit(CreateMorphism operation) {
        beginOperation();
        
        final var signature = operation.schema().signature();
        final var schemaMorphism = instance.schema().getMorphism(signature);
        
        // Ensure the domain objex exists in instance
        // (It might have just become an entity when this morphism was added to schema)
        final var domKey = schemaMorphism.dom().key();
        if (instance.getObjex(domKey) == null && schemaMorphism.dom().isEntity()) {
            final var instanceObjex = new InstanceObjex(schemaMorphism.dom(), instance, Set.of());
            getObjexes(instance).put(domKey, instanceObjex);
        }
        
        // Create empty instance morphism using Editor helper
        final var instanceMorphism = createMorphism(schemaMorphism);
        getMorphisms(instance).put(signature, instanceMorphism);
        
        currentContext.recordMorphismCreation(signature);
        
        endOperation();
        return null;
    }

    /**
     * Delete a morphism from the instance category.
     * 
     * This operation also handles the lifecycle of objexes:
     * - If the domain objex is no longer an entity after removing this morphism,
     *   the InstanceObjex is also removed.
     * 
     * Should typically be called BEFORE DeleteObjex for connected objexes.
     */
    @Override public Void visit(DeleteMorphism operation) {
        beginOperation();
        
        final var signature = operation.schema().signature();
        final var domKey = operation.schema().domKey();
        final var codKey = operation.schema().codKey();
        
        LOGGER.debug("DeleteMorphism: signature={}, domKey={}, codKey={}", signature, domKey, codKey);
        
        final var instanceMorphism = getMorphisms(instance).get(signature);
        LOGGER.debug("DeleteMorphism: instanceMorphism={}", instanceMorphism != null ? "found" : "null");
        
        if (instanceMorphism != null) {
            // Delete all mapping rows (and clean up references in domain rows)
            for (MappingRow mapping : new ArrayList<>(getAllMappings(instanceMorphism))) {
                removeMapping(instanceMorphism, mapping);
                currentContext.recordMappingDeletion(mapping);
            }
            
            // Remove the morphism from the instance
            getMorphisms(instance).remove(signature);
            currentContext.recordMorphismDeletion(signature);
        }
        
        // If domain objex is no longer an entity (no more outgoing morphisms), remove its InstanceObjex
        final var domSchemaObjex = instance.schema().getObjex(domKey);
        LOGGER.debug("DeleteMorphism: domSchemaObjex={}, isEntity={}", 
            domSchemaObjex != null ? domSchemaObjex.key() : "null",
            domSchemaObjex != null ? domSchemaObjex.isEntity() : "N/A");
        
        if (domSchemaObjex != null && !domSchemaObjex.isEntity()) {
            final var domInstanceObjex = instance.getObjex(domKey);
            LOGGER.debug("DeleteMorphism: will remove domInstanceObjex={}", domInstanceObjex != null ? "found" : "null");
            if (domInstanceObjex != null) {
                // Clean up any remaining rows in this objex
                final List<DomainRow> domRows = new ArrayList<>();
                domInstanceObjex.allRows().forEach(domRows::add);
                for (DomainRow row : domRows) {
                    cleanupRowMappings(row);
                    currentContext.recordRowDeletion(row);
                }
                getObjexes(instance).remove(domKey);
            }
        }
        
        // Similarly check codomain
        final var codSchemaObjex = instance.schema().getObjex(codKey);
        LOGGER.debug("DeleteMorphism: codSchemaObjex={}, isEntity={}", 
            codSchemaObjex != null ? codSchemaObjex.key() : "null",
            codSchemaObjex != null ? codSchemaObjex.isEntity() : "N/A");
            
        if (codSchemaObjex != null && !codSchemaObjex.isEntity()) {
            final var codInstanceObjex = instance.getObjex(codKey);
            LOGGER.debug("DeleteMorphism: will remove codInstanceObjex={}", codInstanceObjex != null ? "found" : "null");
            if (codInstanceObjex != null) {
                // Clean up any remaining rows in this objex
                final List<DomainRow> codRows = new ArrayList<>();
                codInstanceObjex.allRows().forEach(codRows::add);
                for (DomainRow row : codRows) {
                    cleanupRowMappings(row);
                    currentContext.recordRowDeletion(row);
                }
                getObjexes(instance).remove(codKey);
            }
        }
        
        endOperation();
        return null;
    }

    @Override public Void visit(UpdateMorphism operation) {
        beginOperation();
        
        // UpdateMorphism typically changes metadata, not the mapping data itself
        // The instance morphism structure remains the same
        
        final var signature = operation.newMorphism().signature();
        // UpdateMorphism doesn't create/delete, just marks as affected
        
        endOperation();
        return null;
    }

    @Override public Void visit(CopyObjex operation) {
        // CopyObjex is special - it wraps multiple child operations AND copies data
        // The child operations will each manage their own context
        // We create our context after children complete
        
        // First, process child operations (CreateObjex, CreateMorphism, etc.)
        // These create the target objex and its morphisms in the instance
        for (SMO child : operation.children()) {
            child.accept(this);
        }
        
        // Now create context for the copy operation itself
        beginOperation();
        
        // Copy rows from source to target
        final var sourceKey = operation.source().key();
        final var targetKey = operation.target().key();
        
        final InstanceObjex sourceObjex = instance.getObjex(sourceKey);
        final InstanceObjex targetObjex = instance.getObjex(targetKey);
        
        if (sourceObjex != null && targetObjex != null) {
            targetObjex.copyRowsFrom(sourceObjex);
            currentContext.recordObjexCopy(sourceKey, targetKey);
            
            // Copy morphism mappings for redirected morphisms
            // For each outgoing morphism from target, find corresponding source morphism with same codomain
            for (final var targetMorphism : getOutgoingMorphisms(targetKey)) {
                final var codomainKey = targetMorphism.schema.cod().key();
                
                // Find source morphism with same codomain
                final var sourceMorphism = getOutgoingMorphisms(sourceKey).stream()
                    .filter(m -> m.schema.cod().key().equals(codomainKey))
                    .findFirst();
                
                if (sourceMorphism.isPresent()) {
                    // Copy mappings from source to target morphism
                    copyMappings(sourceMorphism.get(), targetMorphism, sourceObjex, targetObjex);
                    currentContext.recordMorphismCopy(sourceMorphism.get().identifier(), targetMorphism.identifier());
                }
            }
        }
        
        endOperation();
        return null;
    }

    /**
     * GROUP operation: Groups multiple properties under a new intermediate object.
     *
     * Example transformation:
     * Before: Customer.firstName, Customer.lastName
     * After: Customer.name.firstName, Customer.name.lastName
     *
     * Instance changes:
     * 1. Create InstanceObjex for the grouping object
     * 2. Create InstanceMorphism for parent → grouping object
     * 3. For each parent row:
     *    - Create a corresponding row in the grouping objex
     *    - Create mapping from parent row → grouping row
     *    - Migrate mappings from old morphisms to new morphisms
     */
    @Override public Void visit(GroupProperties operation) {
        beginOperation();

        final var parentKey = operation.parentKey();
        final var groupingKey = operation.groupingObjexKey();
        final var parentToGroupSig = operation.parentToGroupSignature();

        LOGGER.debug("GroupProperties: parentKey={}, groupingKey={}, parentToGroupSig={}",
            parentKey, groupingKey, parentToGroupSig);

        // Get schema objects (these exist because up() was called first)
        final var groupingSchemaObjex = instance.schema().getObjex(groupingKey);
        final var parentToGroupSchemaMorphism = instance.schema().getMorphism(parentToGroupSig);

        if (groupingSchemaObjex == null) {
            LOGGER.warn("GroupProperties: grouping objex not found in schema (key={})", groupingKey);
            endOperation();
            return null;
        }

        // Create InstanceObjex for the grouping object if it doesn't exist
        InstanceObjex groupingObjex = instance.getObjex(groupingKey);
        if (groupingObjex == null && groupingSchemaObjex.isEntity()) {
            groupingObjex = new InstanceObjex(groupingSchemaObjex, instance, Set.of());
            getObjexes(instance).put(groupingKey, groupingObjex);
            currentContext.recordObjexCreation(groupingKey);
        }

        // Create InstanceMorphism for parent → grouping if it doesn't exist
        InstanceMorphism parentToGroupMorphism = getMorphisms(instance).get(parentToGroupSig);
        if (parentToGroupMorphism == null) {
            parentToGroupMorphism = createMorphism(parentToGroupSchemaMorphism);
            getMorphisms(instance).put(parentToGroupSig, parentToGroupMorphism);
            currentContext.recordMorphismCreation(parentToGroupSig);
        }

        // Create new InstanceMorphisms for the moved morphisms (grouping → property)
        for (final var moved : operation.movedMorphisms()) {
            final var newSig = moved.newMorphism().signature();
            if (getMorphisms(instance).get(newSig) == null) {
                final var newSchemaMorphism = instance.schema().getMorphism(newSig);
                final var newMorphism = createMorphism(newSchemaMorphism);
                getMorphisms(instance).put(newSig, newMorphism);
                currentContext.recordMorphismCreation(newSig);
            }
        }

        // Get parent InstanceObjex
        final InstanceObjex parentObjex = instance.getObjex(parentKey);
        if (parentObjex == null || groupingObjex == null) {
            LOGGER.debug("GroupProperties: parent or grouping objex not found in instance, skipping data migration");
            endOperation();
            return null;
        }

        // Map from parent row to corresponding grouping row
        final var parentToGroupRowMap = new java.util.HashMap<DomainRow, DomainRow>();

        // For each row in the parent objex, create a corresponding row in the grouping objex
        for (final DomainRow parentRow : parentObjex.allRows()) {
            // Create a new row in the grouping objex
            // The superId for the grouping row should derive from the parent row
            // For simplicity, we use an empty superId (the row is uniquely identified by its relationship to the parent)
            final var groupingRow = groupingObjex.createRow(SuperIdValues.empty());
            parentToGroupRowMap.put(parentRow, groupingRow);

            // Create mapping from parent row → grouping row
            parentToGroupMorphism.createMapping(parentRow, groupingRow);

            LOGGER.debug("GroupProperties: created grouping row for parent row {}", parentRow.superId);
        }

        // Migrate data for each moved morphism
        for (final var moved : operation.movedMorphisms()) {
            final var oldSig = moved.oldMorphism().signature();
            final var newSig = moved.newMorphism().signature();
            final var propertyKey = moved.oldMorphism().codKey();

            final var oldMorphism = getMorphisms(instance).get(oldSig);
            final var newMorphism = getMorphisms(instance).get(newSig);

            if (oldMorphism == null || newMorphism == null) {
                LOGGER.warn("GroupProperties: morphism not found, oldSig={}, newSig={}", oldSig, newSig);
                continue;
            }

            // Check if the property is an entity or a simple value
            final var propertySchemaObjex = instance.schema().getObjex(propertyKey);
            final boolean isPropertyEntity = propertySchemaObjex != null && propertySchemaObjex.isEntity();

            if (isPropertyEntity) {
                // Property is an entity - migrate MappingRows
                final var mappingsToMigrate = new ArrayList<MappingRow>(getAllMappings(oldMorphism));

                for (final MappingRow oldMapping : mappingsToMigrate) {
                    final DomainRow parentRow = oldMapping.dom();
                    final DomainRow propertyRow = oldMapping.cod();

                    final DomainRow groupingRow = parentToGroupRowMap.get(parentRow);
                    if (groupingRow == null) {
                        LOGGER.warn("GroupProperties: no grouping row found for parent row {}", parentRow.superId);
                        continue;
                    }

                    // Remove old mapping (parent → property)
                    removeMapping(oldMorphism, oldMapping);
                    currentContext.recordMappingDeletion(oldMapping);

                    // Create new mapping (grouping → property)
                    newMorphism.createMapping(groupingRow, propertyRow);
                }
            } else {
                // Property is a non-entity (simple value) - migrate property values
                // In MM-cat, non-entity property values are stored in the DomainRow's propertyValues map
                for (final DomainRow parentRow : parentObjex.allRows()) {
                    final DomainRow groupingRow = parentToGroupRowMap.get(parentRow);
                    if (groupingRow == null) {
                        LOGGER.warn("GroupProperties: no grouping row found for parent row {}", parentRow.superId);
                        continue;
                    }

                    // Get the property value from the parent row
                    // Property values can be in the superId or in propertyValues
                    final String value = parentRow.tryGetScalarValue(oldSig);
                    if (value != null) {
                        // Add the value as a property in the grouping row
                        groupingRow.addPropertyValue(newSig, value);
                    }
                }
            }

            // Remove the old morphism from the instance (it's deleted from schema)
            getMorphisms(instance).remove(oldSig);
            currentContext.recordMorphismDeletion(oldSig);
        }

        currentContext.recordGroupOperation(parentKey, groupingKey, operation.movedMorphisms().size());

        endOperation();
        return null;
    }

    /**
     * UNGROUP operation: Moves properties from a grouping object back to the parent.
     *
     * Example transformation:
     * Before: Customer.name.firstName, Customer.name.lastName
     * After: Customer.firstName, Customer.lastName
     *
     * Instance changes:
     * 1. For each parent row and its grouping row:
     *    - Migrate property values/mappings from grouping row back to parent row
     * 2. Delete the grouping rows and parent-to-grouping mappings
     * 3. Delete the grouping InstanceObjex and related morphisms
     */
    @Override public Void visit(UngroupProperty operation) {
        beginOperation();

        final var parentKey = operation.parentKey();
        final var groupingKey = operation.groupingObjexKey();
        final var parentToGroupSig = operation.parentToGroupSignature();

        LOGGER.debug("UngroupProperty: parentKey={}, groupingKey={}, parentToGroupSig={}",
            parentKey, groupingKey, parentToGroupSig);

        // Get instance objects
        final InstanceObjex parentObjex = instance.getObjex(parentKey);
        final InstanceObjex groupingObjex = instance.getObjex(groupingKey);
        final InstanceMorphism parentToGroupMorphism = getMorphisms(instance).get(parentToGroupSig);

        if (parentObjex == null || groupingObjex == null) {
            LOGGER.debug("UngroupProperty: parent or grouping objex not found, skipping data migration");
            endOperation();
            return null;
        }

        // Build reverse mapping: grouping row -> parent row
        final var groupToParentRowMap = new java.util.HashMap<DomainRow, DomainRow>();
        if (parentToGroupMorphism != null) {
            for (final MappingRow mapping : getAllMappings(parentToGroupMorphism)) {
                groupToParentRowMap.put(mapping.cod(), mapping.dom());
            }
        }

        // Create new InstanceMorphisms for the moved morphisms (parent -> property)
        for (final var moved : operation.movedMorphisms()) {
            final var newSig = moved.newMorphism().signature();
            if (getMorphisms(instance).get(newSig) == null) {
                final var newSchemaMorphism = instance.schema().getMorphism(newSig);
                if (newSchemaMorphism != null) {
                    final var newMorphism = createMorphism(newSchemaMorphism);
                    getMorphisms(instance).put(newSig, newMorphism);
                    currentContext.recordMorphismCreation(newSig);
                }
            }
        }

        // Migrate data for each moved morphism
        for (final var moved : operation.movedMorphisms()) {
            final var oldSig = moved.oldMorphism().signature();
            final var newSig = moved.newMorphism().signature();
            final var propertyKey = moved.oldMorphism().codKey();

            final var oldMorphism = getMorphisms(instance).get(oldSig);
            final var newMorphism = getMorphisms(instance).get(newSig);

            if (newMorphism == null) {
                LOGGER.warn("UngroupProperty: new morphism not found, newSig={}", newSig);
                continue;
            }

            // Check if the property is an entity or a simple value
            final var propertySchemaObjex = instance.schema().getObjex(propertyKey);
            final boolean isPropertyEntity = propertySchemaObjex != null && propertySchemaObjex.isEntity();

            if (isPropertyEntity && oldMorphism != null) {
                // Property is an entity - migrate MappingRows back to parent
                final var mappingsToMigrate = new ArrayList<MappingRow>(getAllMappings(oldMorphism));

                for (final MappingRow oldMapping : mappingsToMigrate) {
                    final DomainRow groupingRow = oldMapping.dom();
                    final DomainRow propertyRow = oldMapping.cod();

                    final DomainRow parentRow = groupToParentRowMap.get(groupingRow);
                    if (parentRow == null) {
                        LOGGER.warn("UngroupProperty: no parent row found for grouping row {}", groupingRow.superId);
                        continue;
                    }

                    // Remove old mapping (grouping -> property)
                    removeMapping(oldMorphism, oldMapping);
                    currentContext.recordMappingDeletion(oldMapping);

                    // Create new mapping (parent -> property)
                    newMorphism.createMapping(parentRow, propertyRow);
                }
            } else {
                // Property is a non-entity (simple value) - migrate property values back to parent
                for (final DomainRow groupingRow : groupingObjex.allRows()) {
                    final DomainRow parentRow = groupToParentRowMap.get(groupingRow);
                    if (parentRow == null) {
                        LOGGER.warn("UngroupProperty: no parent row found for grouping row {}", groupingRow.superId);
                        continue;
                    }

                    // Get the property value from the grouping row
                    final String value = groupingRow.tryGetScalarValue(oldSig);
                    if (value != null) {
                        // Add the value as a property in the parent row
                        parentRow.addPropertyValue(newSig, value);
                    }
                }
            }

            // Remove the old morphism from the instance (it's deleted from schema)
            if (oldMorphism != null) {
                getMorphisms(instance).remove(oldSig);
                currentContext.recordMorphismDeletion(oldSig);
            }
        }

        // Delete parent-to-grouping mappings and morphism
        if (parentToGroupMorphism != null) {
            for (final MappingRow mapping : new ArrayList<>(getAllMappings(parentToGroupMorphism))) {
                removeMapping(parentToGroupMorphism, mapping);
                currentContext.recordMappingDeletion(mapping);
            }
            getMorphisms(instance).remove(parentToGroupSig);
            currentContext.recordMorphismDeletion(parentToGroupSig);
        }

        // Delete the grouping objex
        final List<DomainRow> groupingRows = new ArrayList<>();
        groupingObjex.allRows().forEach(groupingRows::add);
        for (final DomainRow row : groupingRows) {
            currentContext.recordRowDeletion(row);
        }
        getObjexes(instance).remove(groupingKey);
        currentContext.recordObjexDeletion(groupingKey);

        endOperation();
        return null;
    }

    /**
     * MOVE operation: Copy property values/mappings to new location and delete originals.
     *
     * Instance changes:
     * 1. Create new morphisms at target location (if not already existing)
     * 2. Copy property values/mappings from source to target
     * 3. Delete source morphisms and their mappings
     */
    @Override public Void visit(MoveProperty operation) {
        beginOperation();

        final var sourceKey = operation.sourceKey();
        final var targetKey = operation.targetKey();

        LOGGER.debug("MoveProperty: sourceKey={}, targetKey={}", sourceKey, targetKey);

        // Get source and target objexes
        final InstanceObjex sourceObjex = instance.getObjex(sourceKey);
        final InstanceObjex targetObjex = instance.getObjex(targetKey);

        if (sourceObjex == null) {
            LOGGER.debug("MoveProperty: source objex not found, skipping data migration");
            endOperation();
            return null;
        }

        // Process each moved morphism
        for (final var moved : operation.movedMorphisms()) {
            final var sourceSig = moved.sourceMorphism().signature();
            final var targetSig = moved.targetMorphism().signature();
            final var propertyKey = moved.sourceMorphism().codKey();

            final var sourceMorphism = getMorphisms(instance).get(sourceSig);

            // Create target morphism if needed
            InstanceMorphism targetMorphism = getMorphisms(instance).get(targetSig);
            if (targetMorphism == null) {
                final var targetSchemaMorphism = instance.schema().getMorphism(targetSig);
                if (targetSchemaMorphism != null) {
                    targetMorphism = createMorphism(targetSchemaMorphism);
                    getMorphisms(instance).put(targetSig, targetMorphism);
                    currentContext.recordMorphismCreation(targetSig);
                }
            }

            if (targetMorphism == null) {
                LOGGER.warn("MoveProperty: target morphism not found in schema, targetSig={}", targetSig);
                continue;
            }

            // Check if the property is an entity or a simple value
            final var propertySchemaObjex = instance.schema().getObjex(propertyKey);
            final boolean isPropertyEntity = propertySchemaObjex != null && propertySchemaObjex.isEntity();

            if (isPropertyEntity && sourceMorphism != null) {
                // Property is an entity - copy MappingRows to target, then delete from source
                final var mappingsToCopy = new ArrayList<MappingRow>(getAllMappings(sourceMorphism));

                for (final MappingRow sourceMapping : mappingsToCopy) {
                    final DomainRow sourceRow = sourceMapping.dom();
                    final DomainRow propertyRow = sourceMapping.cod();

                    // Find corresponding target row (if target objex is same as source)
                    // or use the target objex directly
                    DomainRow targetRow = null;
                    if (targetObjex != null && targetKey.equals(sourceKey)) {
                        // Same objex - use the same row
                        targetRow = sourceRow;
                    } else if (targetObjex != null) {
                        // Different objex - need to find corresponding row
                        // For now, we'll create mapping from target rows to property rows
                        // This assumes there's a 1:1 correspondence established elsewhere
                        targetRow = findCorrespondingRow(targetObjex, sourceRow);
                    }

                    if (targetRow != null) {
                        // Create new mapping at target
                        targetMorphism.createMapping(targetRow, propertyRow);
                    }

                    // Remove source mapping
                    removeMapping(sourceMorphism, sourceMapping);
                    currentContext.recordMappingDeletion(sourceMapping);
                }
            } else {
                // Property is a non-entity (simple value) - copy property values
                for (final DomainRow sourceRow : sourceObjex.allRows()) {
                    // Get the property value from the source row
                    final String value = sourceRow.tryGetScalarValue(sourceSig);
                    if (value != null) {
                        // Find corresponding target row
                        DomainRow targetRow = null;
                        if (targetObjex != null && targetKey.equals(sourceKey)) {
                            targetRow = sourceRow;
                        } else if (targetObjex != null) {
                            targetRow = findCorrespondingRow(targetObjex, sourceRow);
                        }

                        if (targetRow != null) {
                            // Add the value as a property in the target row
                            targetRow.addPropertyValue(targetSig, value);
                        }
                    }
                }
            }

            // Remove the source morphism from the instance
            if (sourceMorphism != null) {
                getMorphisms(instance).remove(sourceSig);
                currentContext.recordMorphismDeletion(sourceSig);
            }
        }

        endOperation();
        return null;
    }

    /**
     * Find a row in targetObjex that corresponds to sourceRow.
     * This is a simple implementation that assumes rows can be matched by superId.
     */
    private DomainRow findCorrespondingRow(InstanceObjex targetObjex, DomainRow sourceRow) {
        for (final DomainRow targetRow : targetObjex.allRows()) {
            if (targetRow.superId.equals(sourceRow.superId)) {
                return targetRow;
            }
        }
        return null;
    }

    /**
     * JOIN operation: Combine multiple property values into one using a delimiter.
     *
     * Instance changes:
     * 1. For each parent row, collect values from source properties
     * 2. Concatenate values with delimiter
     * 3. Store result in target property
     * 4. Optionally delete source property values
     */
    @Override public Void visit(JoinProperties operation) {
        beginOperation();

        final var parentKey = operation.parentKey();
        final var delimiter = operation.delimiter();

        LOGGER.debug("JoinProperties: parentKey={}, delimiter='{}', sourceCount={}",
            parentKey, delimiter, operation.sourceProperties().size());

        // Get parent objex
        final InstanceObjex parentObjex = instance.getObjex(parentKey);
        if (parentObjex == null) {
            LOGGER.debug("JoinProperties: parent objex not found, skipping");
            endOperation();
            return null;
        }

        // Get target morphism signature
        final var targetSig = operation.targetProperty().morphism().signature();

        // Create target morphism if needed
        InstanceMorphism targetMorphism = getMorphisms(instance).get(targetSig);
        if (targetMorphism == null) {
            final var targetSchemaMorphism = instance.schema().getMorphism(targetSig);
            if (targetSchemaMorphism != null) {
                targetMorphism = createMorphism(targetSchemaMorphism);
                getMorphisms(instance).put(targetSig, targetMorphism);
                currentContext.recordMorphismCreation(targetSig);
            }
        }

        // Collect source signatures in order
        final var sourceSignatures = operation.sourceProperties().stream()
            .map(sp -> sp.morphism().signature())
            .toList();

        // For each parent row, join the source values
        for (final DomainRow parentRow : parentObjex.allRows()) {
            final var values = new ArrayList<String>();

            for (final var sourceSig : sourceSignatures) {
                final String value = parentRow.tryGetScalarValue(sourceSig);
                if (value != null) {
                    values.add(value);
                }
            }

            // Join values with delimiter
            if (!values.isEmpty()) {
                final String joinedValue = String.join(delimiter, values);
                parentRow.addPropertyValue(targetSig, joinedValue);
                LOGGER.debug("JoinProperties: joined '{}' for row {}", joinedValue, parentRow.superId);
            }
        }

        // Optionally delete source properties
        if (operation.deleteSourceProperties()) {
            for (final var source : operation.sourceProperties()) {
                final var sourceSig = source.morphism().signature();
                final var sourceMorphism = getMorphisms(instance).get(sourceSig);
                if (sourceMorphism != null) {
                    getMorphisms(instance).remove(sourceSig);
                    currentContext.recordMorphismDeletion(sourceSig);
                }
            }
        }

        endOperation();
        return null;
    }

    /**
     * SPLIT operation: Split one property value into multiple using a delimiter.
     *
     * Instance changes:
     * 1. For each parent row, get the source property value
     * 2. Split value by delimiter
     * 3. Store each part in corresponding target property (by index)
     * 4. Optionally delete source property value
     */
    @Override public Void visit(SplitProperty operation) {
        beginOperation();

        final var parentKey = operation.parentKey();
        final var delimiter = operation.delimiter();

        LOGGER.debug("SplitProperty: parentKey={}, delimiter='{}', targetCount={}",
            parentKey, delimiter, operation.targetProperties().size());

        // Get parent objex
        final InstanceObjex parentObjex = instance.getObjex(parentKey);
        if (parentObjex == null) {
            LOGGER.debug("SplitProperty: parent objex not found, skipping");
            endOperation();
            return null;
        }

        // Get source signature
        final var sourceSig = operation.sourceProperty().morphism().signature();

        // Create target morphisms if needed
        final var targetSignatures = new ArrayList<BaseSignature>();
        for (final var target : operation.targetProperties()) {
            final var targetSig = target.morphism().signature();
            targetSignatures.add(targetSig);

            if (getMorphisms(instance).get(targetSig) == null) {
                final var targetSchemaMorphism = instance.schema().getMorphism(targetSig);
                if (targetSchemaMorphism != null) {
                    final var targetMorphism = createMorphism(targetSchemaMorphism);
                    getMorphisms(instance).put(targetSig, targetMorphism);
                    currentContext.recordMorphismCreation(targetSig);
                }
            }
        }

        // For each parent row, split the source value
        for (final DomainRow parentRow : parentObjex.allRows()) {
            final String sourceValue = parentRow.tryGetScalarValue(sourceSig);

            if (sourceValue != null) {
                // Split the value - use -1 limit to keep trailing empty strings
                final String[] parts = sourceValue.split(java.util.regex.Pattern.quote(delimiter), -1);

                // Assign each part to corresponding target property
                for (int i = 0; i < Math.min(parts.length, targetSignatures.size()); i++) {
                    final var targetSig = targetSignatures.get(i);
                    final String part = parts[i].trim();
                    if (!part.isEmpty()) {
                        parentRow.addPropertyValue(targetSig, part);
                    }
                }

                LOGGER.debug("SplitProperty: split '{}' into {} parts for row {}",
                    sourceValue, parts.length, parentRow.superId);
            }
        }

        // Optionally delete source property
        if (operation.deleteSourceProperty()) {
            final var sourceMorphism = getMorphisms(instance).get(sourceSig);
            if (sourceMorphism != null) {
                getMorphisms(instance).remove(sourceSig);
                currentContext.recordMorphismDeletion(sourceSig);
            }
        }

        endOperation();
        return null;
    }

}
