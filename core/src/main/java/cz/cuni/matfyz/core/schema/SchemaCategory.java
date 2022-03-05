package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.MapUniqueContext;
import cz.cuni.matfyz.core.serialization.UniqueContext;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaCategory implements Category
{
    private final UniqueContext<SchemaObject, Key> objectContext = new MapUniqueContext<>();
    private final UniqueContext<SchemaMorphism, Signature> morphismContext = new MapUniqueContext<>();

    public SchemaCategory()
    {
        
	}

    public SchemaObject addObject(SchemaObject object)
    {
        return objectContext.createUniqueObject(object);
	}

	public SchemaMorphism addMorphism(SchemaMorphism morphism)
    {
        var newMorphism = morphismContext.createUniqueObject(morphism);
        newMorphism.setCategory(this);
		return newMorphism;
	}

	public SchemaMorphism dual(Signature signatureOfOriginal)
    {
        final SchemaMorphism result = signatureToMorphism(signatureOfOriginal.dual());
        assert result != null : "Schema morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
	}

    public SchemaObject keyToObject(Key key)
    {
        return objectContext.getUniqueObject(key);
    }
    
    public SchemaMorphism signatureToMorphism(Signature signature)
    {
        return morphismContext.getUniqueObject(signature);
    }

    public Iterable<SchemaObject> allObjects()
    {
        return objectContext.getAllUniqueObjects();
    }

    public Iterable<SchemaMorphism> allMorphisms()
    {
        return morphismContext.getAllUniqueObjects();
    }
}
