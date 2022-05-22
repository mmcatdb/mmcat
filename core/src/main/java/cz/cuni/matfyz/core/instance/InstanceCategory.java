package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.*;

import java.util.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceCategory implements Category {

	private final SchemaCategory schema;
	private final Map<Key, InstanceObject> objects;
	private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms)
    {
		this.schema = schema;
		this.objects = objects;
        this.morphisms = morphisms;
	}
	
    /*
	InstanceCategory() {
		objects = new TreeMap<>();
		morphisms = new TreeMap<>();
	}
	
	public void addObject(InstanceObject object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		
	}
	
	public void addMorphism(Object... TODO) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    */
    
    public Map<Key, InstanceObject> objects()
    {
        return objects;
    }
    
	public Map<Signature, InstanceMorphism> morphisms()
    {
        return morphisms;
    }
    
	public InstanceObject object(Key key)
    {
        final InstanceObject result = objects.get(key);
        assert result != null : "Instance object with key " + key + " not found in instance category.";
        return result;
	}
    
    public InstanceMorphism morphism(Signature signature)
    {
        InstanceMorphism morphism = morphisms.get(signature);
		if (morphism == null)
		{
			// This must be a composite morphism. These are created dynamically so we have to add it dynamically.
			SchemaMorphism schemaMorphism = schema.signatureToMorphism(signature);
			InstanceObject dom = object(schemaMorphism.dom().key());
			InstanceObject cod = object(schemaMorphism.cod().key());

			morphism = new InstanceMorphism(schemaMorphism, dom, cod, this);
			morphisms.put(signature, morphism);
		}
        
        return morphism;
	}
    
    public InstanceMorphism dual(Signature signatureOfOriginal)
    {
		/*
        final InstanceMorphism result = morphisms.get(signatureOfOriginal.dual());
        assert result != null : "Instance morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
		*/
		return morphism(signatureOfOriginal.dual());
	}
	
	@Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

		builder.append("Keys: ");
		for (Key key : objects.keySet())
			builder.append(key).append(", ");
		builder.append("\n");
        
        builder.append("Objects:\n");
		for (Key key : objects.keySet())
        {
			InstanceObject object = objects.get(key);
			builder.append(object).append("\n");
		}
		builder.append("\n");

        builder.append("Signatures: ");
		for (Signature signature : morphisms.keySet())
			builder.append(signature).append(", ");
		builder.append("\n");
        
        builder.append("Morphisms:\n");
		for (Signature signature : morphisms.keySet())
        {
			InstanceMorphism morphism = morphisms.get(signature);
			builder.append(morphism).append("\n");
		}
		builder.append("\n");

		return builder.toString();
	}

    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceCategory instance
            && objects.equals(instance.objects)
            && morphisms.equals(instance.morphisms);
    }
}
