package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.evolution.exception.DependencyException;

import java.util.List;

public class DeleteObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    final SchemaObject object;

    public DeleteObject(SchemaObject object) {
        this.object = object;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var objects = getObjectContext(category);
        final var objectToDelete = objects.getUniqueObject(object.key());

        // Check if there aren't any dependent morphisms
        final var morphisms = getMorphismContext(category);
        final List<Signature> signaturesOfDependentMorphisms = morphisms.getAllUniqueObjects().stream()
            .filter(morphism -> morphism.dom().equals(objectToDelete) || morphism.cod().equals(objectToDelete))
            .map(Morphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(object.key(), signaturesOfDependentMorphisms);

        objects.deleteUniqueObject(objectToDelete);
    }

}
