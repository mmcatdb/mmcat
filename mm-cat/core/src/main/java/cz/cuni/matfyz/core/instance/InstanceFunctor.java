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
        throw new UnsupportedOperationException(); // TODO
    }
    
    public InstanceMorphism morphism(SchemaMorphism schemaMorphism)
    {
        throw new UnsupportedOperationException(); // TODO   
    }
}
