package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class InstanceFunctor
{
    private final InstanceCategory instanceCategory;
    private final SchemaCategory schemaCategory;
    
    public InstanceFunctor(InstanceCategory instanceCategory, SchemaCategory schemaCategory)
    {
        this.instanceCategory = instanceCategory;
        this.schemaCategory = schemaCategory;
    }
    
    public InstanceObject object(SchemaObject schemaObject)
    {
        var object = instanceCategory.object(schemaObject.key());
        if (object == null)
        {
            System.out.println("Requested object is null: ");
            System.out.println(object);
        }
        
        return object;
    }
    
    public InstanceMorphism morphism(SchemaMorphism schemaMorphism)
    {
        var morphism = instanceCategory.morphism(schemaMorphism.signature());
        if (morphism == null)
        {
            System.out.println("Requested morphism is null: ");
            System.out.println(morphism);
        }
        
        return morphism;
    }
}
