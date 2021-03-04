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
public interface AbstractSimpleProperty extends AbstractProperty {

	// JE ZBYTECNE MIT ABSTRACT VALUE, PREDELEJ TO N
	public abstract AbstractValue getValue();

	public abstract String getName();	// tohle je lepsi resit na urovni AbstractProperty, takze duplicitni a zbytecne je getValue

}
