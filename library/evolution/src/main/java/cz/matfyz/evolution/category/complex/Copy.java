package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.evolution.category.*;

import java.util.List;

public record Copy(
    SchemaObject originalObject
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Step 1: Create contact address as a copy of delivery address
        Key contactAddressKey = new Key(originalObject.key().getValue() + "_copy".hashCode());
        SchemaObject contactAddress = new SchemaObject(contactAddressKey, originalObject.ids(), originalObject.superId());
        editor.getObjects().put(contactAddress.key(), contactAddress);

        // Create auxiliary morphism δ: contact address → delivery address
        SchemaMorphism delta = new SchemaMorphism(contactAddress, originalObject, new Signature("delta"));
        editor.getMorphisms().put(delta.signature(), delta);

        // Step 2: Create morphism f: user → contact address (f = δ ∘ c ∘ b)
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().equals(originalObject)) {
                for (SchemaMorphism intermediate : schemaCategory.allMorphisms()) {
                    if (intermediate.cod().equals(morphism.dom())) {
                        SchemaMorphism f = new SchemaMorphism(intermediate.dom(), contactAddress, new Signature("f"));
                        f.updateCardinality("1..1", "1..*");
                        editor.getMorphisms().put(f.signature(), f);
                    }
                }
            }
        }

        // Step 3: Change δ's cardinality to (0..* ↔ 1..1)
        delta.updateCardinality("0..*", "1..1");

        // Step 4: Transform f to f' by changing its cardinality (1..1 ↔ 1..1)
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().equals(contactAddress)) {
                morphism.updateCardinality("1..1", "1..1");
            }
        }

        // Step 5: Delete auxiliary morphism δ
        editor.getMorphisms().remove(delta.signature());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        SchemaEditor editor = new SchemaEditor(schemaCategory);

        // Remove copied object and all related morphisms
        SchemaObject contactAddress = schemaCategory.getObject(originalObject.key() + "_copy");
        if (contactAddress == null) return;

        List<SchemaMorphism> morphismsToRemove = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(contactAddress) || morphism.cod().equals(contactAddress))
            .toList();

        for (SchemaMorphism morphism : morphismsToRemove) {
            editor.getMorphisms().remove(morphism.signature());
        }

        editor.getObjects().remove(contactAddress.key());
    }
}
