package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import java.util.*;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaCategory implements Category
{
	private final Map<Key, SchemaObject> objects = new TreeMap<>();
	private final Map<Signature, SchemaMorphism> morphisms = new TreeMap<>();

	public Iterable<SchemaObject> objects()
    {
		return objects.values();
	}

	public Iterable<SchemaMorphism> morphisms()
    {
		return morphisms.values();
	}

	public SchemaCategory()
    {
        
	}

	public boolean addObject(SchemaObject object)
    {
        if (objects.containsKey(object.key()))
            return false;
        
		objects.put(object.key(), object);
		return true;
	}

	public boolean addMorphism(SchemaMorphism morphism)
    {
        if (morphisms.containsKey(morphism.signature()))
            return false;
        
		morphisms.put(morphism.signature(), morphism);
		morphism.setCategory(this);
		return true;
	}

	public SchemaMorphism dual(Signature signatureOfOriginal)
    {
        final SchemaMorphism result = morphisms.get(signatureOfOriginal.dual());
        assert result != null : "Schema morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
	}
}
