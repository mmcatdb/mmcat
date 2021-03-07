/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalSchema implements AbstractInstance {

	private final Map<String, AbstractCategoricalObject> objects = new TreeMap<>();
//	private final Map<Pair, Set<AbstractCategoricalMorphism>> morphisms = new TreeMap<>();
	private final Map<String, AbstractCategoricalMorphism> morphisms = new TreeMap<>();

	@Override
	public AbstractCategoricalObject getOrCreate(String name, AbstractType type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public AbstractCategoricalObject get(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean create(String name, AbstractType type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public AbstractCategoricalMorphism getOrCreateMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public AbstractCategoricalMorphism getMorphism(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void createMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Set<Map.Entry<String, AbstractCategoricalObject>> objectsEntrySet() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Set<Map.Entry<String, AbstractCategoricalMorphism>> morphismsEntrySet() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int objectsSize() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Set<String> objectsKeySet() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Set<String> morphismsKeySet() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
