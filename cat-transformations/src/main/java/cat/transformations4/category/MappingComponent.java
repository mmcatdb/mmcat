/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations4.category;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class MappingComponent {

	private String connectionString;

	private List<AccessPath> paths = new ArrayList<>();

	public MappingComponent(String connectionString) {
		this.connectionString = connectionString;
	}

	public void addAccessPath(AccessPath path) {
		paths.add(path);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(connectionString);
		builder.append("   ->   ");
		boolean first = true;
		for (int index = 0; index < paths.size(); ++index) {
			if (!first) {
				builder.append(" | ");
			} else {
				first = !first;
			}
			builder.append(paths.get(index));
		}
		return builder.toString();
	}

}
