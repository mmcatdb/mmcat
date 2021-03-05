/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalAttributeObject implements AbstractCategoricalObject {

	// atributovy objekt je tuple
	// domain
	private final Set<AbstractValue> domain = new TreeSet<>();
	// name
	private final String name;
	// type
	private final AbstractType type;
	// source
	private final Object source;

	public CategoricalAttributeObject(String name, AbstractType type) {
		this.name = name;
		this.type = type;
		this.source = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void add(AbstractValue value) {
		domain.add(value);
	}

	@Override
	public boolean remove(AbstractValue value) {
		return domain.remove(value);
	}

	@Override
	public boolean contains(AbstractValue value) {
		return domain.contains(value);
	}

	@Override
	public int compareTo(AbstractCategoricalObject o) {
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(name);
		builder.append(":\t");

		builder.append("{");
		boolean firstSet = true;
		for (var value : domain) {
			if (firstSet) {
				firstSet = !firstSet;
			} else {
				builder.append(", ");
			}
			builder.append(value);

		}
		builder.append("}");

		return builder.toString();
	}

}
