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

	private final Map<Key, InstanceObject> objects;
	private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms)
    {
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
        final InstanceMorphism result = morphisms.get(signature);
        assert result != null : "Instance morphism with signature " + signature + " not found in instance category.";
        return result;
	}
    
    public InstanceMorphism dual(Signature signatureOfOriginal)
    {
        final InstanceMorphism result = morphisms.get(signatureOfOriginal.dual());
        assert result != null : "Instance morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
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
        System.out.println("COMPARING INSTANCES");
        return object instanceof InstanceCategory instance ? equals(instance) : false;
    }
    
    public boolean equals(InstanceCategory instance)
    {
        System.out.println("COMPARING INSTANCES SECOND");
        if (instance == null)
            return false;
        
        if (!objects.equals(instance.objects))
            return false;
        
        
        if (morphisms.keySet().size() != instance.morphisms.keySet().size())
            System.out.println("MORPHISMS LENGHTS DIFFER");
        
        for (Signature signature : morphisms.keySet())
            if (!morphisms.get(signature).equals(instance.morphisms.get(signature)))
            {
                System.out.println("MORPHISMS DIFFER:");
                System.out.println(signature);
                System.out.println(morphisms.get(signature));
                System.out.println(instance.morphisms.get(signature));
            }
        
        return (morphisms.equals(instance.morphisms));
        /*
        if (objects.keySet().size() != instance.objects.keySet().size())
            return false;
        
        for (Key key : objects.keySet())
            if (!objects.get(key).equals(instance.objects.get(key)))
                return false;
        
        if (morphisms.keySet().size() != instance.morphisms.keySet().size())
            return false;
        
        for (Signature signature : morphisms.keySet())
            if (!morphisms.get(signature).equals(instance.morphisms.get(signature)))
                return false;
        */
        
        //return true;
    }
}
