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
public interface AbstractArrayProperty extends AbstractProperty {

	public abstract Iterable<AbstractProperty> getElements();

	public String getName();

	public void add(AbstractProperty property);

}