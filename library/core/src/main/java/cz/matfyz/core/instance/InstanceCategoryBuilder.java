package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

public class InstanceCategoryBuilder {

    private SchemaCategory schemaCategory;

    private final Map<Key, InstanceObject> objects = new TreeMap<>();
    private final Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();

    public InstanceCategoryBuilder setSchemaCategory(SchemaCategory schemaCategory) {
        this.schemaCategory = schemaCategory;
        return this;
    }

    public InstanceCategory build() {
        final var instance = new InstanceCategory(schemaCategory, objects, morphisms);

        for (SchemaObject schemaObject : schemaCategory.allObjects()) {
            final InstanceObject instanceObject = new InstanceObject(schemaObject, instance);
            objects.put(instanceObject.schema.key(), instanceObject);
        }

        // The base moprhisms must be created first because the composite ones use them.
        final var baseMorphisms = schemaCategory.allMorphisms().stream().filter(SchemaMorphism::isBase).toList();
        for (final var schemaMorphism : baseMorphisms) {
            final var instanceMorphism = new InstanceMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        final var compositeMorphisms = schemaCategory.allMorphisms().stream().filter(morphism -> !morphism.isBase()).toList();
        for (final var schemaMorphism : compositeMorphisms) {
            final var instanceMorphism = new InstanceMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        return instance;
    }

}
