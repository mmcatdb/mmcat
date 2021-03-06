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
public class DocumentProperty implements AbstractAttributeProperty {

	private final String name;
	private final boolean isIdentifierCompound;
	private final boolean isReferenceCompound;
	private final boolean isNullable;
	private final AbstractValue value;

	private static final AbstractType TYPE = AbstractType.ATTRIBUTE;

	public DocumentProperty(String name, Object value, boolean isIdentifierCompound, boolean isReferenceCompound, boolean isNullable) {
		this.name = name;
		this.value = DocumentFactory.INSTANCE.createValue(name, value);
		this.isIdentifierCompound = isIdentifierCompound;
		this.isReferenceCompound = isReferenceCompound;
		this.isNullable = isNullable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isIdentifierCompound() {
		return isIdentifierCompound;
	}

	@Override
	public boolean isReferenceCompound() {
		return isReferenceCompound;
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}

	@Override
	public AbstractValue getValue() {
		return value;
	}

	@Override
	public AbstractType getType() {
		return TYPE;
	}

	@Override
	public int compareTo(AbstractValue o) {
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append(name);

//		builder.append(":");
		builder.append(value);

		return builder.toString();
	}

}
