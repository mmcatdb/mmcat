package cz.matfyz.evolution.propagation;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
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
 * Updates Mapping access paths when the schema evolves.
 * Implements the visitor pattern over SMOs to transform ComplexProperty trees
 * so that mappings remain consistent with the evolved schema.
 *
 * After all SMOs are applied, call {@link #getEvolvedMappings()} to get the updated mappings.
 */
public class MappingEvolver implements SchemaEvolutionVisitor<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingEvolver.class);

    private final List<Mapping> mappings;
    private final SchemaCategory schema;
    private final MetadataCategory metadata;

    public MappingEvolver(List<Mapping> mappings, SchemaCategory schema, MetadataCategory metadata) {
        this.mappings = new ArrayList<>(mappings);
        this.schema = schema;
        this.metadata = metadata;
    }

    /**
     * Returns the updated mappings after all SMOs have been applied.
     */
    public List<Mapping> getEvolvedMappings() {
        return List.copyOf(mappings);
    }

    // --- Visitor methods ---

    @Override
    public Void visit(CreateObjex operation) {
        // Creating a new objex doesn't change existing mappings.
        // A new mapping would be created externally if needed.
        return null;
    }

    @Override
    public Void visit(DeleteObjex operation) {
        // If the deleted objex is the root of a mapping, remove that mapping.
        final Key key = operation.schema().key();
        mappings.removeIf(m -> m.rootObjex().key().equals(key));
        return null;
    }

    @Override
    public Void visit(CreateMorphism operation) {
        // A new morphism is created. Add it as a new simple property in the
        // mapping whose root matches the morphism's domain.
        final BaseSignature baseSignature = operation.schema().signature();
        final var morphismMeta = metadata.getMorphism(baseSignature);
        final String propertyName = morphismMeta != null ? morphismMeta.label : baseSignature.toString();

        for (int i = 0; i < mappings.size(); i++) {
            final Mapping mapping = mappings.get(i);
            if (mapping.rootObjex().key().equals(operation.schema().domKey())) {
                final var newSubpath = new SimpleProperty(new StringName(propertyName), baseSignature);
                final var newAccessPath = addSubpath(mapping.accessPath(), newSubpath);
                mappings.set(i, mapping.withSchemaAndPath(schema, newAccessPath));
            }
        }
        return null;
    }

    @Override
    public Void visit(DeleteMorphism operation) {
        // Remove the subpath matching the deleted morphism's signature.
        final Signature signature = operation.schema().signature();

        for (int i = 0; i < mappings.size(); i++) {
            final Mapping mapping = mappings.get(i);
            final AccessPath subpath = mapping.accessPath().getSubpathBySignature(signature);
            if (subpath != null) {
                final var newAccessPath = mapping.accessPath().minusSubpath(subpath);
                mappings.set(i, mapping.withSchemaAndPath(schema, newAccessPath));
            }
        }
        return null;
    }

    @Override
    public Void visit(GroupProperties operation) {
        // GROUP: nest properties under a new intermediate ComplexProperty.
        // Before: root { prop1: oldSig1, prop2: oldSig2 }
        // After:  root { groupName: parentToGroupSig { prop1: newSig1, prop2: newSig2 } }
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null)
            return null;

        final int idx = mappings.indexOf(mapping);
        final ComplexProperty rootPath = mapping.accessPath();

        // Collect moved properties and build new subpaths with updated signatures
        final var groupSubpaths = new ArrayList<AccessPath>();
        final var remainingSubpaths = new ArrayList<AccessPath>(rootPath.subpaths());

        for (final var moved : operation.movedMorphisms()) {
            final AccessPath oldSubpath = rootPath.getSubpathBySignature(moved.oldMorphism().signature());
            if (oldSubpath != null) {
                remainingSubpaths.remove(oldSubpath);
                // Create new subpath with the same name but new signature
                final AccessPath newSubpath = rebuildWithSignature(oldSubpath, moved.newMorphism().signature());
                groupSubpaths.add(newSubpath);
            }
        }

        // Create the group ComplexProperty node
        final var groupMeta = metadata.getObjex(operation.groupingObjexKey());
        final String groupName = groupMeta != null ? groupMeta.label : "group";
        final Signature parentToGroupSig = operation.parentToGroupSignature();

        final var groupNode = new ComplexProperty(
            new StringName(groupName), parentToGroupSig, groupSubpaths
        );

        // Build new root path: remaining subpaths + group node
        remainingSubpaths.add(groupNode);
        final var newRootPath = new ComplexProperty(
            rootPath.name(), rootPath.signature(), remainingSubpaths
        );

        mappings.set(idx, mapping.withSchemaAndPath(schema, newRootPath));
        return null;
    }

    @Override
    public Void visit(UngroupProperty operation) {
        // UNGROUP: flatten properties from group back to parent level.
        // Before: root { groupName: sig { prop1: oldSig1, prop2: oldSig2 } }
        // After:  root { prop1: newSig1, prop2: newSig2 }
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null)
            return null;

        final int idx = mappings.indexOf(mapping);
        final ComplexProperty rootPath = mapping.accessPath();

        // Find and remove the group node
        final AccessPath groupSubpath = rootPath.getSubpathBySignature(operation.parentToGroupSignature());
        if (groupSubpath == null || !(groupSubpath instanceof ComplexProperty groupNode))
            return null;

        final var newSubpaths = new ArrayList<AccessPath>(rootPath.subpaths());
        newSubpaths.remove(groupSubpath);

        // Move properties from group to root level with updated signatures
        for (final var moved : operation.movedMorphisms()) {
            final AccessPath oldSubpath = groupNode.getSubpathBySignature(moved.oldMorphism().signature());
            if (oldSubpath != null) {
                final AccessPath newSubpath = rebuildWithSignature(oldSubpath, moved.newMorphism().signature());
                newSubpaths.add(newSubpath);
            }
        }

        final var newRootPath = new ComplexProperty(
            rootPath.name(), rootPath.signature(), newSubpaths
        );

        mappings.set(idx, mapping.withSchemaAndPath(schema, newRootPath));
        return null;
    }

    @Override
    public Void visit(JoinProperties operation) {
        // JOIN: replace source properties with a target property.
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null)
            return null;

        final int idx = mappings.indexOf(mapping);
        final ComplexProperty rootPath = mapping.accessPath();
        final var newSubpaths = new ArrayList<AccessPath>(rootPath.subpaths());

        // Optionally remove source properties
        if (operation.deleteSourceProperties()) {
            for (final var source : operation.sourceProperties()) {
                final AccessPath sourceSubpath = rootPath.getSubpathBySignature(source.morphism().signature());
                if (sourceSubpath != null) {
                    newSubpaths.remove(sourceSubpath);
                }
            }
        }

        // Add target property
        final var targetMeta = metadata.getObjex(operation.targetProperty().objex().key());
        final String targetName = targetMeta != null ? targetMeta.label : "joined";
        final var targetSubpath = new SimpleProperty(
            new StringName(targetName), operation.targetProperty().morphism().signature()
        );
        newSubpaths.add(targetSubpath);

        final var newRootPath = new ComplexProperty(
            rootPath.name(), rootPath.signature(), newSubpaths
        );

        mappings.set(idx, mapping.withSchemaAndPath(schema, newRootPath));
        return null;
    }

    @Override
    public Void visit(SplitProperty operation) {
        // SPLIT: replace source property with multiple target properties.
        final Mapping mapping = findMappingByRootKey(operation.parentKey());
        if (mapping == null)
            return null;

        final int idx = mappings.indexOf(mapping);
        final ComplexProperty rootPath = mapping.accessPath();
        final var newSubpaths = new ArrayList<AccessPath>(rootPath.subpaths());

        // Optionally remove source property
        if (operation.deleteSourceProperty()) {
            final AccessPath sourceSubpath = rootPath.getSubpathBySignature(
                operation.sourceProperty().morphism().signature()
            );
            if (sourceSubpath != null) {
                newSubpaths.remove(sourceSubpath);
            }
        }

        // Add target properties
        for (final var target : operation.targetProperties()) {
            final var targetMeta = metadata.getObjex(target.objex().key());
            final String targetName = targetMeta != null ? targetMeta.label : target.objex().key().toString();
            final var targetSubpath = new SimpleProperty(
                new StringName(targetName), target.morphism().signature()
            );
            newSubpaths.add(targetSubpath);
        }

        final var newRootPath = new ComplexProperty(
            rootPath.name(), rootPath.signature(), newSubpaths
        );

        mappings.set(idx, mapping.withSchemaAndPath(schema, newRootPath));
        return null;
    }

    @Override
    public Void visit(MoveProperty operation) {
        // MOVE: remove properties from source mapping, add to target mapping.
        final Mapping sourceMapping = findMappingByRootKey(operation.sourceKey());
        final Mapping targetMapping = findMappingByRootKey(operation.targetKey());

        // Remove from source
        if (sourceMapping != null) {
            final int sourceIdx = mappings.indexOf(sourceMapping);
            final var sourceSubpaths = new ArrayList<AccessPath>(sourceMapping.accessPath().subpaths());

            for (final var moved : operation.movedMorphisms()) {
                final AccessPath subpath = sourceMapping.accessPath().getSubpathBySignature(
                    moved.sourceMorphism().signature()
                );
                if (subpath != null) {
                    sourceSubpaths.remove(subpath);
                }
            }

            final var newSourcePath = new ComplexProperty(
                sourceMapping.accessPath().name(), sourceMapping.accessPath().signature(), sourceSubpaths
            );
            mappings.set(sourceIdx, sourceMapping.withSchemaAndPath(schema, newSourcePath));
        }

        // Add to target
        if (targetMapping != null) {
            // Re-find in case source == target and we already modified it
            final int targetIdx = findMappingIndexByRootKey(operation.targetKey());
            if (targetIdx < 0)
                return null;

            final Mapping currentTarget = mappings.get(targetIdx);
            final var targetSubpaths = new ArrayList<AccessPath>(currentTarget.accessPath().subpaths());

            for (final var moved : operation.movedMorphisms()) {
                final var morphismMeta = metadata.getMorphism(moved.targetMorphism().signature());
                final String propName = morphismMeta != null ? morphismMeta.label
                    : moved.targetMorphism().signature().toString();
                final var newSubpath = new SimpleProperty(
                    new StringName(propName), moved.targetMorphism().signature()
                );
                targetSubpaths.add(newSubpath);
            }

            final var newTargetPath = new ComplexProperty(
                currentTarget.accessPath().name(), currentTarget.accessPath().signature(), targetSubpaths
            );
            mappings.set(targetIdx, currentTarget.withSchemaAndPath(schema, newTargetPath));
        }

        return null;
    }

    @Override
    public Void visit(UpdateObjex operation) {
        // Metadata-only update, no access path changes needed.
        return null;
    }

    @Override
    public Void visit(UpdateMorphism operation) {
        // Metadata-only update, no access path changes needed.
        return null;
    }

    @Override
    public Void visit(CopyObjex operation) {
        for (final var child : operation.children())
            child.accept(this);
        return null;
    }

    @Override
    public Void visit(Composite operation) {
        for (final var child : operation.children())
            child.accept(this);
        return null;
    }

    // --- Helpers ---

    private @Nullable Mapping findMappingByRootKey(Key key) {
        for (final Mapping mapping : mappings) {
            if (mapping.rootObjex().key().equals(key))
                return mapping;
        }
        return null;
    }

    private int findMappingIndexByRootKey(Key key) {
        for (int i = 0; i < mappings.size(); i++) {
            if (mappings.get(i).rootObjex().key().equals(key))
                return i;
        }
        return -1;
    }

    /**
     * Create a new ComplexProperty that includes an additional subpath.
     */
    private ComplexProperty addSubpath(ComplexProperty parent, AccessPath newSubpath) {
        final var newSubpaths = new ArrayList<AccessPath>(parent.subpaths());
        newSubpaths.add(newSubpath);
        return new ComplexProperty(parent.name(), parent.signature(), newSubpaths);
    }

    /**
     * Rebuild an access path node with a new signature, preserving name and subpaths (if complex).
     */
    private AccessPath rebuildWithSignature(AccessPath original, Signature newSignature) {
        if (original instanceof ComplexProperty complex) {
            return new ComplexProperty(complex.name(), newSignature, complex.subpaths());
        }
        return new SimpleProperty(original.name(), newSignature);
    }

}
