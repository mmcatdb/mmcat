/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class AttributeObject implements CategoricalObject {

	private final Set<Object> activeDomain = new TreeSet<>();

	private final String attributeName;

	public AttributeObject(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public void add(Object object) {
		activeDomain.add(object);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(attributeName);
		builder.append(":\t");

		activeDomain.forEach(value -> {
			builder.append(value).append(",");
		});
		return builder.toString();
	}

	@Override
	public String getName() {
		return attributeName;
	}

    @Override
    public int size() {
        return activeDomain.size();
    }

}
