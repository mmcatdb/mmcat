package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.evolution.exception.MorphismDependencyException;

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
        final List<String> signaturesOfDependentMorphisms = morphisms.getAllUniqueObjects().stream()
            .filter(morphism -> morphism.dom().equals(objectToDelete) || morphism.cod().equals(objectToDelete))
            .map(morphism -> morphism.signature().toString())
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty()) {
            final var message = String.format(
                "Cannot delete object with key: %s because of dependent morphisms: %s.",
                key,
                String.join(", ", signaturesOfDependentMorphisms)
            );
            throw new MorphismDependencyException(message);
        }

        objects.deleteUniqueObject(objectToDelete);
    }

}
