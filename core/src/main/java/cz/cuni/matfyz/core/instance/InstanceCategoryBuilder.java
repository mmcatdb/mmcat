package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class InstanceCategoryBuilder
{
    private SchemaCategory schemaCategory;
    private InstanceCategory result;
    
    private Map<Key, InstanceObject> objects = new TreeMap<>();
    private Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();
    
    public InstanceCategoryBuilder setSchemaCategory(SchemaCategory schemaCategory)
    {
        this.schemaCategory = schemaCategory;
        return this;
    }
    
    public InstanceCategory build()
    {
        result = new InstanceCategory(schemaCategory, objects, morphisms);
        
        for (SchemaObject schemaObject : schemaCategory.allObjects())
        {
            InstanceObject instanceObject = createObject(schemaObject);
            objects.put(instanceObject.key(), instanceObject);
        }

		for (SchemaMorphism schemaMorphism : schemaCategory.allMorphisms())
        {
            InstanceMorphism instanceMorphism = createMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }
        
        return result;
    }
    
    private InstanceObject createObject(SchemaObject schemaObject)
    {
        return new InstanceObject(schemaObject);
    }
    
    private InstanceMorphism createMorphism(SchemaMorphism schemaMorphism)
    {
        InstanceObject domain = objects.get(schemaMorphism.dom().key());
        InstanceObject codomain = objects.get(schemaMorphism.cod().key());
        
        return new InstanceMorphism(schemaMorphism, domain, codomain, result);
    }
}
