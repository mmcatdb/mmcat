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

//	public InstanceCategory(Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms)
//    {
//		this.objects = objects;
//        this.morphisms = morphisms;
//	}
	
	public InstanceCategory() {
		objects = new TreeMap<>();
		morphisms = new TreeMap<>();
	}
	
	public void addObject(InstanceObject object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		
	}
	
	public void addMorphism(Object... TODO) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public InstanceCategory(SchemaCategory schema) {
		// vezme kazdy objekt schematicke kategorie
		// vytvori kopii v instancni kategorii, 1:1
		// totez udela s morfismy
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
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Objects:\t");
		for (String key : objects.keySet()) {
			builder.append(key).append(", ");
		}
		builder.append("\n");

		for (String key : objects.keySet()) {
			var object = objects.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		for (String key : morphisms_TODOSIMPLE.keySet()) {
			var object = morphisms_TODOSIMPLE.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		return builder.toString();
	}
}
