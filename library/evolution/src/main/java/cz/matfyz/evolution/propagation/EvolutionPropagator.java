package cz.matfyz.evolution.propagation;

import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper;
import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper.PropertyInfo;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CopyObjex;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.evolution.category.GroupProperties;
import cz.matfyz.evolution.category.JoinProperties;
import cz.matfyz.evolution.category.MoveProperty;
import cz.matfyz.evolution.category.SchemaEvolutionVisitor;
import cz.matfyz.evolution.category.SplitProperty;
import cz.matfyz.evolution.category.UngroupProperty;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.evolution.category.UpdateObjex;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates schema evolution operations (SMOs) into database-specific migration statements.
 * Implements the visitor pattern over SMOs and delegates to an AbstractEvolutionWrapper
 * for database-specific statement generation.
 *
 * The propagator resolves SMO schema-level references (Keys, Signatures) to database-level
 * names (kind names, property names) using the mapping access paths and metadata labels.
 */
public class EvolutionPropagator implements SchemaEvolutionVisitor<List<AbstractStatement>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionPropagator.class);

    private final AbstractEvolutionWrapper wrapper;
    private final List<Mapping> mappings;
    private final MetadataCategory metadata;

    public EvolutionPropagator(AbstractEvolutionWrapper wrapper, List<Mapping> mappings, MetadataCategory metadata) {
        this.wrapper = wrapper;
        this.mappings = mappings;
        this.metadata = metadata;
    }

    // --- Visitor methods ---

    @Override
    public List<AbstractStatement> visit(CreateObjex operation) {
        // A new objex is created. If it corresponds to a root of a mapping (i.e., a new table/collection),
        // we generate a CREATE statement. Otherwise it's an internal schema change (no DB effect).
        final Key key = operation.schema().key();
        final Mapping mapping = findMappingByRootKey(key);
        if (mapping == null) {
            LOGGER.debug("CreateObjex for key {} does not map to any database kind — skipping", key);
            return List.of();
        }

        // Collect properties from the access path
        final var properties = new ArrayList<PropertyInfo>();
        for (final AccessPath subpath : mapping.accessPath().subpaths()) {
            if (subpath.name() instanceof StringName stringName) {
                properties.add(new PropertyInfo(stringName.value, subpath.isRequired(), false));
            }
        }

        return wrapper.evolveCreateObjex(mapping.kindName(), properties);
    }

    @Override
    public List<AbstractStatement> visit(DeleteObjex operation) {
        final Key key = operation.schema().key();
        final Mapping mapping = findMappingByRootKey(key);
        if (mapping == null) {
            LOGGER.debug("DeleteObjex for key {} does not map to any database kind — skipping", key);
            return List.of();
        }

        return wrapper.evolveDeleteObjex(mapping.kindName());
    }

    @Override
    public List<AbstractStatement> visit(CreateMorphism operation) {
        // A new morphism is created. This may add a new property/column.
        final Signature signature = operation.schema().signature();
        final MappingResolution resolution = resolveSignatureInMappings(signature);
        if (resolution == null) {
            LOGGER.debug("CreateMorphism for signature {} not resolved — skipping", signature);
            return List.of();
        }

        // New property name comes from metadata label
        final var metadataMorphism = metadata.getMorphism(operation.schema().signature());
        final String propertyName = metadataMorphism != null ? metadataMorphism.label : resolution.propertyName;

        return wrapper.evolveAddProperty(resolution.kindName, propertyName, false);
    }

    @Override
    public List<AbstractStatement> visit(DeleteMorphism operation) {
        final Signature signature = operation.schema().signature();
        final MappingResolution resolution = resolveSignatureInMappings(signature);
        if (resolution == null) {
            LOGGER.debug("DeleteMorphism for signature {} not resolved — skipping", signature);
            return List.of();
        }

        return wrapper.evolveRemoveProperty(resolution.kindName, resolution.propertyName);
    }

    @Override
    public List<AbstractStatement> visit(GroupProperties operation) {
        // GROUP: nest properties under a new sub-structure
        // Find the mapping for the parent objex
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null) {
            LOGGER.debug("GroupProperties: parent key {} not mapped — skipping", operation.parentKey());
            return List.of();
        }

        // The group name comes from the metadata label of the new grouping objex
        final var groupMetadata = metadata.getObjex(operation.groupingObjexKey());
        final String groupName = groupMetadata != null ? groupMetadata.label : "group";

        // Resolve the old morphism signatures to database property names
        final var propertyNames = new ArrayList<String>();
        for (final var moved : operation.movedMorphisms()) {
            final String propName = resolvePropertyName(mapping, moved.oldMorphism().signature());
            if (propName != null) {
                propertyNames.add(propName);
            }
        }

        if (propertyNames.isEmpty()) {
            LOGGER.debug("GroupProperties: no properties resolved — skipping");
            return List.of();
        }

        return wrapper.evolveGroupProperties(mapping.kindName(), groupName, propertyNames);
    }

    @Override
    public List<AbstractStatement> visit(UngroupProperty operation) {
        // UNGROUP: flatten nested structure back to parent
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null) {
            LOGGER.debug("UngroupProperty: parent key {} not mapped — skipping", operation.parentKey());
            return List.of();
        }

        // The group name comes from metadata of the grouping objex being removed
        final var groupMetadata = metadata.getObjex(operation.groupingObjexKey());
        final String groupName = groupMetadata != null ? groupMetadata.label : "group";

        // Resolve property names from the old morphisms (groupingObjex -> property)
        // These are the properties being un-nested, and their names from the metadata
        final var propertyNames = new ArrayList<String>();
        for (final var moved : operation.movedMorphisms()) {
            // Use metadata label of the target objex for the property name
            final var morphismMeta = metadata.getMorphism(moved.oldMorphism().signature());
            if (morphismMeta != null) {
                propertyNames.add(morphismMeta.label);
            } else {
                // Fallback: try to resolve from access path
                final String propName = resolvePropertyName(mapping, moved.newMorphism().signature());
                if (propName != null) {
                    propertyNames.add(propName);
                }
            }
        }

        if (propertyNames.isEmpty()) {
            LOGGER.debug("UngroupProperty: no properties resolved — skipping");
            return List.of();
        }

        return wrapper.evolveUngroupProperty(mapping.kindName(), groupName, propertyNames);
    }

    @Override
    public List<AbstractStatement> visit(JoinProperties operation) {
        // JOIN: concatenate multiple properties into one
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null) {
            LOGGER.debug("JoinProperties: parent key {} not mapped — skipping", operation.parentKey());
            return List.of();
        }

        // Resolve source property names from the mapping
        final var sourceNames = new ArrayList<String>();
        for (final var source : operation.sourceProperties()) {
            final String propName = resolvePropertyName(mapping, source.morphism().signature());
            if (propName != null) {
                sourceNames.add(propName);
            }
        }

        if (sourceNames.isEmpty()) {
            LOGGER.debug("JoinProperties: no source properties resolved — skipping");
            return List.of();
        }

        // Target property name from metadata label of the new target objex
        final var targetMeta = metadata.getObjex(operation.targetProperty().objex().key());
        final String targetName = targetMeta != null ? targetMeta.label : "joined";

        return wrapper.evolveJoinProperties(
            mapping.kindName(), targetName, sourceNames,
            operation.delimiter(), operation.deleteSourceProperties()
        );
    }

    @Override
    public List<AbstractStatement> visit(SplitProperty operation) {
        // SPLIT: split one property into multiple
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null) {
            LOGGER.debug("SplitProperty: parent key {} not mapped — skipping", operation.parentKey());
            return List.of();
        }

        // Source property name from the mapping
        final String sourceName = resolvePropertyName(mapping, operation.sourceProperty().morphism().signature());
        if (sourceName == null) {
            LOGGER.debug("SplitProperty: source property not resolved — skipping");
            return List.of();
        }

        // Target property names from metadata labels of the new target objexes
        final var targetNames = new ArrayList<String>();
        for (final var target : operation.targetProperties()) {
            final var targetMeta = metadata.getObjex(target.objex().key());
            if (targetMeta != null) {
                targetNames.add(targetMeta.label);
            }
        }

        if (targetNames.isEmpty()) {
            LOGGER.debug("SplitProperty: no target properties resolved — skipping");
            return List.of();
        }

        return wrapper.evolveSplitProperty(
            mapping.kindName(), sourceName, targetNames,
            operation.delimiter(), operation.deleteSourceProperty()
        );
    }

    @Override
    public List<AbstractStatement> visit(MoveProperty operation) {
        // MOVE: move property from one location to another
        final Mapping sourceMapping = findMappingByRootKey(operation.sourceKey());
        final Mapping targetMapping = findMappingByRootKey(operation.targetKey());

        if (sourceMapping == null && targetMapping == null) {
            LOGGER.debug("MoveProperty: neither source nor target mapped — skipping");
            return List.of();
        }

        final var allStatements = new ArrayList<AbstractStatement>();
        for (final var moved : operation.movedMorphisms()) {
            final String sourcePropName = sourceMapping != null
                ? resolvePropertyName(sourceMapping, moved.sourceMorphism().signature())
                : null;

            // Target property name: try metadata label first
            String targetPropName = null;
            final var targetMorphismMeta = metadata.getMorphism(moved.targetMorphism().signature());
            if (targetMorphismMeta != null) {
                targetPropName = targetMorphismMeta.label;
            } else if (targetMapping != null) {
                targetPropName = resolvePropertyName(targetMapping, moved.targetMorphism().signature());
            }

            if (sourcePropName == null || targetPropName == null) {
                LOGGER.debug("MoveProperty: could not resolve source or target property — skipping");
                continue;
            }

            final String sourceKind = sourceMapping != null ? sourceMapping.kindName() : "";
            final String targetKind = targetMapping != null ? targetMapping.kindName() : sourceKind;

            allStatements.addAll(wrapper.evolveMoveProperty(
                sourceKind, sourcePropName, targetKind, targetPropName
            ));
        }

        return allStatements;
    }

    @Override
    public List<AbstractStatement> visit(UpdateObjex operation) {
        // Schema metadata update — no database migration needed
        return List.of();
    }

    @Override
    public List<AbstractStatement> visit(UpdateMorphism operation) {
        // Schema metadata update — no database migration needed
        return List.of();
    }

    @Override
    public List<AbstractStatement> visit(CopyObjex operation) {
        // CopyObjex is a composite — process its children
        final var statements = new ArrayList<AbstractStatement>();
        for (final var child : operation.children()) {
            statements.addAll(child.accept(this));
        }
        return statements;
    }

    @Override
    public List<AbstractStatement> visit(Composite operation) {
        final var statements = new ArrayList<AbstractStatement>();
        for (final var child : operation.children()) {
            statements.addAll(child.accept(this));
        }
        return statements;
    }

    // --- Resolution helpers ---

    /**
     * Finds the mapping whose root objex matches the given key.
     */
    private @Nullable Mapping findMappingByRootKey(Key key) {
        for (final Mapping mapping : mappings) {
            if (mapping.rootObjex().key().equals(key)) {
                return mapping;
            }
        }
        return null;
    }

    /**
     * Resolves a morphism signature to a database property name using the mapping's access path.
     */
    private @Nullable String resolvePropertyName(Mapping mapping, Signature signature) {
        final AccessPath subpath = mapping.accessPath().getSubpathBySignature(signature);
        if (subpath != null && subpath.name() instanceof StringName stringName) {
            return stringName.value;
        }
        return null;
    }

    /**
     * Resolves a signature across all mappings, returning the kind name and property name.
     */
    private @Nullable MappingResolution resolveSignatureInMappings(Signature signature) {
        for (final Mapping mapping : mappings) {
            final String propName = resolvePropertyName(mapping, signature);
            if (propName != null) {
                return new MappingResolution(mapping.kindName(), propName);
            }
        }
        return null;
    }

    private record MappingResolution(String kindName, String propertyName) {}

}
