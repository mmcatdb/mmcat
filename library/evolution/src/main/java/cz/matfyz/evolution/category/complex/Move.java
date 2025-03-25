package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.category.*;

public record Move(
    SchemaObject originalObject
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        // Use the Copy operation to create a copy of the original object
        Copy copyOperation = new Copy(originalObject);
        copyOperation.up(schemaCategory, metadataCategory);

        // Remove the original object and its associated morphisms
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Remove original object
        editor.getObjects().remove(originalObject.key());

        // Remove all morphisms related to the original object
        schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(originalObject) || morphism.cod().equals(originalObject))
            .forEach(morphism -> editor.getMorphisms().remove(morphism.signature()));
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        // First, remove the copied object and its associated morphisms
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        Key contactAddressKey = new Key(originalObject.key().getValue() + "_copy".hashCode());
        SchemaObject contactAddress = schemaCategory.getObject(contactAddressKey);
        if (contactAddress != null) {
            editor.getObjects().remove(contactAddress.key());
            schemaCategory.allMorphisms().stream()
                .filter(morphism -> morphism.dom().equals(contactAddress) || morphism.cod().equals(contactAddress))
                .forEach(morphism -> editor.getMorphisms().remove(morphism.signature()));
        }

        // Restore the original object (re-create it if necessary) and morphisms
        Copy copyOperation = new Copy(originalObject);
        copyOperation.down(schemaCategory, metadataCategory);
    }
}
