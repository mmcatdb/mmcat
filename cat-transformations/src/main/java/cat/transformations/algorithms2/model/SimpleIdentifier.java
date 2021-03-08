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
 * @author pavel.contos
 */
public class SimpleIdentifier implements AbstractIdentifier {

	private final List<List<Object>> identifiers = new ArrayList<>();

	public SimpleIdentifier() {
	}

	public SimpleIdentifier(AbstractIdentifier a, AbstractIdentifier b) {
		List<Object> identifier = new ArrayList<>();
		identifier.add(new Pair<>(a, b));
//		identifier.add("ARRAY" + (System.currentTimeMillis() % 1000));
		identifiers.add(identifier);
	}

	@Override
	public int compareTo(AbstractValue o) {
		return -1;
	}

	@Override
	public void add(List<Object> identifier) {
		identifiers.add(identifier);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean firstIdentifier = true;
		for (var identifier : identifiers) {

			if (firstIdentifier) {
				firstIdentifier = !firstIdentifier;
			} else {
				builder.append(",");
			}

			builder.append("{");

			boolean firstAttribute = true;
			for (var attribute : identifier) {
				if (firstAttribute) {
					firstAttribute = !firstAttribute;
				} else {
					builder.append(",");
				}
				builder.append(attribute);
			}

			builder.append("}");
		}
		builder.append("}");

		return builder.toString();
	}

}
