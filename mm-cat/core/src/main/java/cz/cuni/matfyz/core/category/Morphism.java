/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.category;

/**
 *
 * @author pavel.koupil
 */
public interface Morphism {

	public abstract CategoricalObject dom();

	public abstract CategoricalObject cod();

	public abstract Morphism dual();
	
	public abstract Signature signature();

}
