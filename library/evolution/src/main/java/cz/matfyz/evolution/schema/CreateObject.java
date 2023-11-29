package cz.matfyz.evolution.schema;

import cz.matfyz.core.category.Morphism;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public class CreateObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    public final SchemaObject object;

    public CreateObject(SchemaObject object) {
        this.object = object;
    }

    @Override
    public void up(SchemaCategory category) {
        getObjectContext(category).createUniqueObject(object);
    }

    @Override
    public void down(SchemaCategory category) {
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
            .map(Morphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(object.key(), signaturesOfDependentMorphisms);
    }

}
