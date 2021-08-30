/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

/**
 *
 * @author pavel.contos
 */
public interface CategoricalMorphism extends Comparable<CategoricalMorphism> {

	public abstract void add(EntityObject.EntityValue key, Object value);

	public abstract String getName();

	public abstract String getDomain();

	public abstract String getCodomain();

	public abstract Object getValue(EntityObject.EntityValue key);

}
