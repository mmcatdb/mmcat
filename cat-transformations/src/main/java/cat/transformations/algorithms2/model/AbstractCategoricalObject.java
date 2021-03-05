/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractCategoricalObject extends Comparable<AbstractCategoricalObject> {

	public abstract String getName();

	public abstract void add(AbstractValue value);

	public abstract boolean remove(AbstractValue value);

	public abstract boolean contains(AbstractValue value);

}
