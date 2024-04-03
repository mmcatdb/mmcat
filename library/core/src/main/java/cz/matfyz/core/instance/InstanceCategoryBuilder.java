package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachymb.bartik
 */
public class InstanceCategoryBuilder {

    private SchemaCategory schemaCategory;
    private InstanceCategory result;

    private Map<Key, InstanceObject> objects = new TreeMap<>();
    private Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();

    public InstanceCategoryBuilder setSchemaCategory(SchemaCategory schemaCategory) {
        this.schemaCategory = schemaCategory;
        return this;
    }

    public InstanceCategory build() {
        result = new InstanceCategory(schemaCategory, objects, morphisms);
        //System.out.println("building the instance category");

        for (SchemaObject schemaObject : schemaCategory.allObjects()) {
            //System.out.println("schema object in InstanceCategoryBuilder: " + schemaObject);
            InstanceObject instanceObject = createObject(schemaObject);
            objects.put(instanceObject.key(), instanceObject);
        }

        // The base moprhisms must be created first because the composite ones use them.
        var baseMorphisms = schemaCategory.allMorphisms().stream().filter(SchemaMorphism::isBase).toList();
        for (SchemaMorphism schemaMorphism : baseMorphisms) {
            //System.out.println("schema morphism in InstanceCategoryBuilder: " + schemaMorphism);
            InstanceMorphism instanceMorphism = createMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        var compositeMorphisms = schemaCategory.allMorphisms().stream().filter(morphism -> !morphism.isBase()).toList();
        for (SchemaMorphism schemaMorphism : compositeMorphisms) {
            InstanceMorphism instanceMorphism = createMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        return result;
    }

    private InstanceObject createObject(SchemaObject schemaObject) {
        return new InstanceObject(schemaObject);
    }

    private InstanceMorphism createMorphism(SchemaMorphism schemaMorphism) {
        InstanceObject domain = objects.get(schemaMorphism.dom().key());
        InstanceObject codomain = objects.get(schemaMorphism.cod().key());

        return new InstanceMorphism(schemaMorphism, domain, codomain, result);
    }

}
