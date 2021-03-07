/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class RelationalSchema implements AbstractSchema {

	private final Map<String, AbstractKindSchema> kinds = new TreeMap<>();

	@Override
	public void addKind(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addAttribute(String domain, String codomain, String datatype, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addStructuredAttribute(String domain, String codomain, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addInlinedStructuredAttribute(String domain, String codomain, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addAttribute(String parent, String current, String name, String datatype, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addStructuredAttribute(String parent, String current, String name, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addInlinedStructuredAttribute(String parent, String current, String name, Cardinality cardinality) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<String> getKindNames() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
