package cz.matfyz.evolution.schema;

import cz.matfyz.core.category.Morphism;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public class DeleteObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    final Key key;

    public DeleteObject(Key key) {
        this.key = key;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var objects = getObjectContext(category);
        final var objectToDelete = objects.getUniqueObject(key);

        // Check if there aren't any dependent morphisms
        final var morphisms = getMorphismContext(category);
        final List<Signature> signaturesOfDependentMorphisms = morphisms.getAllUniqueObjects().stream()
            .filter(morphism -> morphism.dom().equals(objectToDelete) || morphism.cod().equals(objectToDelete))
            .map(Morphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(key, signaturesOfDependentMorphisms);

        objects.deleteUniqueObject(objectToDelete);
    }

}
