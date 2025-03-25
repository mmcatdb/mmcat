package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.category.*;

import java.util.List;

public record UnGroup(
    SchemaObject groupedObject
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Step 1: Identify objects U and O (groupedObject is O)
        SchemaObject groupObject = schemaCategory.getObject(groupedObject.key());
        if (groupObject == null) return;

        // Find the morphism f: U -> O (U is the object linked to the grouped object)
        SchemaMorphism f = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.cod().equals(groupObject))
            .findFirst()
            .orElse(null);

        if (f == null) return;

        // Step 2: Identify the morphisms x'_i linking O to each A_i (properties of the group)
        List<SchemaMorphism> groupMorphisms = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(groupObject))
            .toList();

        // Step 3: Create new morphisms x''_i = x'_i ∘ f for each property A_i
        for (SchemaMorphism groupMorphism : groupMorphisms) {
            SchemaObject targetObject = groupMorphism.cod();
            SchemaMorphism newMorphism = new SchemaMorphism(f.dom(), targetObject, new Signature("x_prime_prime_" + targetObject.key()));
            editor.getMorphisms().put(newMorphism.signature(), newMorphism);
        }

        // Step 4: Delete the group object O and all its associated morphisms
        editor.getObjects().remove(groupObject.key());
        for (SchemaMorphism groupMorphism : groupMorphisms) {
            editor.getMorphisms().remove(groupMorphism.signature());
        }
        editor.getMorphisms().remove(f.signature());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        // The down method is the inverse of up for this operation, but in this case, it is simpler
        // because UNGROUP doesn't need to restore lost data.

        // No specific down implementation is needed as UNGROUP can be a one-way operation.
    }
}
