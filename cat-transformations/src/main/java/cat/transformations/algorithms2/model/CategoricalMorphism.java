/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import cat.transformations.commons.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalMorphism implements AbstractCategoricalMorphism {

	private final String name;

	private final AbstractCategoricalObject domain;

	private final AbstractCategoricalObject codomain;

	private final List<Pair<AbstractValue, AbstractValue>> mappings = new ArrayList<>();

	public CategoricalMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
		this.name = name;
		this.domain = domain;
		this.codomain = codomain;
	}

	@Override
	public void add(AbstractValue superid, AbstractValue value) {
		var pair = new Pair(superid, value);
		mappings.add(pair);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(name);
		builder.append(":\t");

		builder.append("{");

		boolean firstValue = true;
		for (var value : mappings) {
			if (firstValue) {
				firstValue = !firstValue;
			} else {
				builder.append(",");
			}
			builder.append(value);
		}

		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getName() {
		return name;
	}

}
