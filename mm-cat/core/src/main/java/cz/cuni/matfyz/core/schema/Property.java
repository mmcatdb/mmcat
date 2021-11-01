package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil
 */
public class Property implements Comparable<Property>
{
	private Signature signature;
	private String name;

	public Property(Signature signature, String name) {
		this.signature = signature;
		this.name = name;
	}

	@Override
	public int compareTo(Property property)
    {
		return signature.compareTo(property.signature);
	}
}
