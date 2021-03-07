/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractSchema {

	public void createKind(String name);

	public void createAttribute(String domain, String codomain, String datatype, Cardinality cardinality);

	// vztahovy pripad, pro mongo a rel a jine modely se resi jinak!
	public void createAttribute(String parent, String current, String name, String datatype, Cardinality cardinality);

	public void createStructuredAttribute(String domain, String codomain);

	// vztahovy pripad, pro mongo a rel a jine modely se resi jinak!
	public void createStructuredAttribute(String parent, String current, String name);

	public void createInlinedStructuredAttribute(String domain, String codomain);

	// vztahovy pripad, pro mongo a rel a jine modely se resi jinak!
	public void createInlinedStructuredAttribute(String parent, String current, String name);

	public Iterable<String> getKindNames();

}
