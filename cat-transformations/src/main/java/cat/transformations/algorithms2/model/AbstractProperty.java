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
public interface AbstractProperty extends AbstractValue {

	public abstract boolean isIdentifierCompound();	// na urovni AbstractProperty

	public abstract boolean isReferenceCompound();	// na urovni AbstractProperty

	public abstract boolean isNullable();	// na urovni AbstractProperty

	public abstract AbstractType getType();	// na urovni AbstracProperty

}
