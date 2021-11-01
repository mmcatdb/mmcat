/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.*;

import java.util.*;

/**
 *
 * @author pavel.koupil
 */
public class InstanceCategory implements Category {

	private final Map<Key, InstanceObject> objects = new TreeMap<>();
	private final Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();

    /**
     * Construct an empty instance category.
     */
	public InstanceCategory()
    {
		
	}

    /**
     * Construct an instance category based on given schema category (all objects and morphisms are copied).
     * @param schema 
     */
	public InstanceCategory(SchemaCategory schema)
    {
		for (SchemaObject object : schema.objects())
			objects.put(object.key(), new InstanceObject(object));

		for (SchemaMorphism morphism : schema.morphisms())
			morphisms.put(morphism.signature(), new InstanceMorphism(morphism, this));
	}

	public InstanceObject object(Key key)
    {
        final InstanceObject result = objects.get(key);
        assert result != null : "Instance object with key " + key + " not found in instance category";
        return result;
	}
    
    public boolean addObject(InstanceObject object)
    {
        if (objects.containsKey(object.key()))
            return false;
        
		objects.put(object.key(), object);
		return true;
	}

    public boolean addMorphism(InstanceMorphism morphism)
    {
        if (morphisms.containsKey(morphism.signature()))
            return false;
        
		morphisms.put(morphism.signature(), morphism);
		morphism.setCategory(this);
		return true;
	}
    
    public InstanceMorphism dual(Signature signatureOfOriginal)
    {
        final InstanceMorphism result = morphisms.get(signatureOfOriginal.dual());
        assert result != null : "Instance morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
	}
}
