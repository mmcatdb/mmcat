/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractIdentifier extends AbstractValue {
	
	public void add(List<Object> identifier);
	
	// jeden konkretni identifier? nebo superid?
//	protected List<AbstractProperty> identifier;

}
