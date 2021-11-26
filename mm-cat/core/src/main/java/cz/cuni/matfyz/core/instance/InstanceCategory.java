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

	public InstanceCategory(Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms)
    {
		this.objects = objects;
        this.morphisms = morphisms;
	}
	
	public InstanceCategory() {
		objects = new TreeMap<>();
		morphisms = new TreeMap<>();
	}

	public InstanceCategory(SchemaCategory schema) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
