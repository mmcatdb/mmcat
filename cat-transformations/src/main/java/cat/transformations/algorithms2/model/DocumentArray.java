/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class DocumentArray implements AbstractArrayProperty {

	private static final List<AbstractProperty> elements = new ArrayList<>();

	private final String name;

	public DocumentArray(String name) {
		this.name = name;
	}

	@Override
	public AbstractType getType() {
		return AbstractType.ARRAY;
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
	public Iterable<AbstractProperty> getElements() {
		return elements;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AbstractValue getValue() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int compareTo(AbstractValue o) {
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		int index = 0;
		for (var element : elements) {
			builder.append(element);
			if (++index < elements.size()) {
				builder.append(",");
			}
		}
		builder.append("]");

		return builder.toString();
	}

	@Override
	public void add(AbstractProperty property) {
		elements.add(property);
	}

}
