package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public class DocumentPropertyValue implements AbstractProperty {

	private static final AbstractObjectType TYPE = AbstractObjectType.ATTRIBUTE;

	private final Object value;

	private final String name;

	public DocumentPropertyValue(String name, Object value) {
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
	public AbstractObjectType getType() {
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
