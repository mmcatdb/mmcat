package cz.matfyz.evolution.instance;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.evolution.category.SchemaEvolutionVisitor;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.evolution.category.UpdateObjex;
import cz.matfyz.evolution.category.complex.CopyObjex;

import java.util.Set;

public class InstanceEvolver extends InstanceCategory.Editor implements SchemaEvolutionVisitor<Void> {

    private final InstanceCategory instance;

    public InstanceEvolver(InstanceCategory instance) {
        this.instance = instance;
    }

    @Override public Void visit(Composite operation) {
        // Implementation for handling a composite operation in the instance schema
        return null;
    }

    @Override public Void visit(CreateObjex operation) {
        final var key = operation.schema().key();
        final var schemaObjex = instance.schema().getObjex(key);
        // FIXME The set.of might be a problem ...
        final var instanceObjex = new InstanceObjex(schemaObjex, instance, Set.of());
        getObjexes(instance).put(key, instanceObjex);

        return null;
    }

    @Override public Void visit(DeleteObjex operation) {
        // Implementation for deleting an objex in the instance schema
        return null;
    }

    @Override public Void visit(UpdateObjex operation) {
        // Implementation for updating an objex in the instance schema
        return null;
    }

    @Override public Void visit(CreateMorphism operation) {
        // Implementation for creating a morphism in the instance schema
        return null;
    }

    @Override public Void visit(DeleteMorphism operation) {
        // Implementation for deleting a morphism in the instance schema
        return null;
    }

    @Override public Void visit(UpdateMorphism operation) {
        // Implementation for updating a morphism in the instance schema
        return null;
    }


    @Override public Void visit(CopyObjex operation) {
        final var targetKey = operation.target().key();
        final var schemaObjex = instance.schema().getObjex(targetKey);
        // FIXME The set.of might be a problem ...
        final var instanceObjex = new InstanceObjex(schemaObjex, instance, Set.of());
        getObjexes(instance).put(targetKey, instanceObjex);

        final var sourceKey = operation.source().key();
        final InstanceObjex sourceObjex = instance.getObjex(sourceKey);

        instanceObjex.copyRowsFrom(sourceObjex);

        return null;
    }



}
