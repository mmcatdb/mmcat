package cz.matfyz.evolution.schema;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public class CreateObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final SchemaObject object;

    public CreateObject(SchemaObject object) {
        this.object = object;
    }

    @Override public void up(SchemaCategory category) {
        getObjectContext(category).createUniqueObject(object);
    }

    @Override public void down(SchemaCategory category) {
        assertObjectIsSingle(category, object);

        getObjectContext(category).deleteUniqueObject(object);
    }

    /**
     * Check if there aren't any dependent morphisms.
     */
    static void assertObjectIsSingle(SchemaCategory category, SchemaObject object) {
        final var morphisms = getMorphismContext(category);
        final List<Signature> signaturesOfDependentMorphisms = morphisms.getAllUniqueObjects().stream()
            .filter(morphism -> morphism.dom().equals(object) || morphism.cod().equals(object))
            .map(SchemaMorphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(object.key(), signaturesOfDependentMorphisms);
    }

}
