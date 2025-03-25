package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.category.*;

import java.util.List;

public record Transform(
    SchemaObject originalObject,
    List<SchemaObject> originalProperties,    // The subset X = {X1, ..., Xm}
    List<SchemaObject> newProperties,         // The new set X' = {X'_1, ..., X'_n}
    TransformationFunction transformationFunction // The function Φ to transform properties
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Step 1: Link the original properties to the original object Y
        for (SchemaObject originalProperty : originalProperties) {
            SchemaMorphism morphism = new SchemaMorphism(originalObject, originalProperty, new Signature("f_" + originalProperty.key()));
            editor.getMorphisms().put(morphism.signature(), morphism);
        }

        // Step 2: Create new properties and link them to the original object Y
        for (SchemaObject newProperty : newProperties) {
            SchemaMorphism newMorphism = new SchemaMorphism(originalObject, newProperty, new Signature("f_prime_" + newProperty.key()));
            editor.getMorphisms().put(newMorphism.signature(), newMorphism);
        }

        // Step 3: Apply the transformation function to map the original properties to the new properties
        for (int i = 0; i < originalProperties.size(); i++) {
            SchemaObject originalProperty = originalProperties.get(i);
            SchemaObject newProperty = newProperties.get(i);
            // Apply the transformation function (Φ) to the values of the properties
            transformationFunction.transform(originalProperty, newProperty);
        }

        // Step 4: Optionally delete the original properties from the schema (if needed)
        // In this case, we remove the original properties after transformation
        for (SchemaObject originalProperty : originalProperties) {
            editor.getObjects().remove(originalProperty.key());
        }
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        // The down method here should reverse the transformations, restoring the original properties
        // to their previous state if needed. For simplicity, this will just remove the new properties
        // and re-link the original ones.

        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Remove the newly created properties and their morphisms
        for (SchemaObject newProperty : newProperties) {
            editor.getObjects().remove(newProperty.key());
            schemaCategory.allMorphisms().stream()
                .filter(morphism -> morphism.cod().equals(newProperty))
                .forEach(morphism -> editor.getMorphisms().remove(morphism.signature()));
        }

        // Re-link the original properties (if they were deleted)
        for (SchemaObject originalProperty : originalProperties) {
            SchemaMorphism reverseMorphism = new SchemaMorphism(originalObject, originalProperty, new Signature("f_reverse_" + originalProperty.key()));
            editor.getMorphisms().put(reverseMorphism.signature(), reverseMorphism);
        }
    }

    // Inner interface for transformation function
    public interface TransformationFunction {
        void transform(SchemaObject originalProperty, SchemaObject newProperty);
    }
}
