package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.category.*;

import java.util.List;

public record Group(
    SchemaObject originalObject
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Step 1: Select a subset X = {X1, ..., Xm} of properties linked to Y
        List<SchemaMorphism> originalMorphisms = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(originalObject))
            .toList();

        // Step 2: Create object G and morphism g: Y -> G
        SchemaObject groupObject = new SchemaObject(originalObject.key() + "_group", originalObject.ids(), originalObject.superId());
        editor.getObjects().put(groupObject.key(), groupObject);

        // Create morphism g: Y -> G
        SchemaMorphism g = new SchemaMorphism(originalObject, groupObject, new Signature("g"));
        editor.getMorphisms().put(g.signature(), g);

        // Step 3: For each object X_i, create morphism f'_i: G -> X_i
        for (SchemaMorphism originalMorphism : originalMorphisms) {
            SchemaObject targetObject = originalMorphism.cod();
            SchemaMorphism fPrime = new SchemaMorphism(groupObject, targetObject, new Signature("f_prime_" + targetObject.key()));
            editor.getMorphisms().put(fPrime.signature(), fPrime);
        }

        // Step 4: Delete the original morphisms
        for (SchemaMorphism originalMorphism : originalMorphisms) {
            editor.getMorphisms().remove(originalMorphism.signature());
        }
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Step 1: Remove object G and its associated morphisms
        SchemaObject groupObject = schemaCategory.getObject(originalObject.key() + "_group");
        if (groupObject != null) {
            // Remove morphisms involving G
            schemaCategory.allMorphisms().stream()
                .filter(morphism -> morphism.dom().equals(groupObject) || morphism.cod().equals(groupObject))
                .forEach(morphism -> editor.getMorphisms().remove(morphism.signature()));

            // Remove object G
            editor.getObjects().remove(groupObject.key());
        }

        // Step 2: Restore the original morphisms
        List<SchemaMorphism> originalMorphisms = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(originalObject))
            .toList();

        // Restore the original morphisms
        for (SchemaMorphism originalMorphism : originalMorphisms) {
            editor.getMorphisms().put(originalMorphism.signature(), originalMorphism);
        }
    }
}
