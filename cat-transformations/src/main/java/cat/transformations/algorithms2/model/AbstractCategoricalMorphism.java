/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public interface AbstractCategoricalMorphism {

	public abstract void add(AbstractValue superid, AbstractValue value);	// WARN: Ted umoznujes pouze jednu hodnotu pridavat

	public abstract String getName();
	
	public abstract AbstractCategoricalObject getDomain();
	
	public abstract AbstractCategoricalObject getCodomain();
	
	public abstract String getDomainName();
	
	public abstract String getCodomainName();
}
