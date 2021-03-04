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
public interface AbstractObject {

	public abstract String getName();

	public abstract void add(Iterable<? extends AbstractValue> superid);

	public abstract void remove(Iterable<? extends AbstractValue> superid);

	public abstract void contains(Iterable<? extends AbstractValue> superid);

	public abstract void add(AbstractValue value);

	public abstract void remove(AbstractValue value);

	public abstract void contains(AbstractValue value);

}
