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
public class DocumentSimpleValue implements AbstractProperty {

	private static final AbstractType TYPE = AbstractType.ATTRIBUTE;

	private final Object value;

	private final String name;

	public DocumentSimpleValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public int compareTo(AbstractValue o) {
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(value);
		return builder.toString();
	}

	@Override
	public boolean isIdentifierCompound() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isReferenceCompound() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isNullable() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public AbstractType getType() {
		return TYPE;
	}

	@Override
	public AbstractValue getValue() {
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

}
