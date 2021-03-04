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
public class DocumentSimpleValue implements AbstractSimpleValue {

	private final Object value;

	public DocumentSimpleValue(Object value) {
		this.value = value;
	}

	@Override
	public AbstractType getType() {
		return null;
	}

	@Override
	public boolean isIdentifierCompound() {
		return false;
	}

	@Override
	public boolean isReferenceCompound() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return true;
	}

	public Object getValue() {
		return value;
	}

}
