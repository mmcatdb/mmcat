package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.schema.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class InstanceFunctor
{
    private static Logger LOGGER = LoggerFactory.getLogger(InstanceFunctor.class);

    private final InstanceCategory instanceCategory;
    private final SchemaCategory schemaCategory; // TODO
    
    public InstanceFunctor(InstanceCategory instanceCategory, SchemaCategory schemaCategory)
    {
        this.instanceCategory = instanceCategory;
        this.schemaCategory = schemaCategory;
    }
    
    public InstanceObject object(SchemaObject schemaObject)
    {
        var object = instanceCategory.object(schemaObject.key());
        if (object == null)
            LOGGER.error("Requested schema object with key " + schemaObject.key() + " not found in instance functor.");
        
        return object;
    }
    
    public InstanceMorphism morphism(SchemaMorphism schemaMorphism)
    {
        var morphism = instanceCategory.morphism(schemaMorphism.signature());
        if (morphism == null)
            LOGGER.error("Requested schema morphism with signature " + schemaMorphism.signature() + " not found in instance functor.");
            
        return morphism;
    }
}
