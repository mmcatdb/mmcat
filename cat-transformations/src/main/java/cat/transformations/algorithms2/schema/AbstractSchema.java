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

	public void addKind(String name);

	public void addAttribute(String domain, String codomain, String datatype, Cardinality cardinality);

	public void addStructuredAttribute(String domain, String codomain, Cardinality cardinality);

	public void addInlinedStructuredAttribute(String domain, String codomain, Cardinality cardinality);

	public void addAttribute(String parent, String current, String name, String datatype, Cardinality cardinality);

	public void addStructuredAttribute(String parent, String current, String name, Cardinality cardinality);

	public void addInlinedStructuredAttribute(String parent, String current, String name, Cardinality cardinality);

	public Iterable<String> getKindNames();

}
