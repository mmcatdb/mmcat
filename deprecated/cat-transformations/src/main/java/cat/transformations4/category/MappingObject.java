package cat.transformations4.category;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class MappingObject {

	private final String name;
	private final List<MappingComponent> components = new ArrayList<>();

	public MappingObject(String name) {
		this.name = name;
	}

	public void addComponent(MappingComponent component) {
		components.add(component);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("   ->   ");
		boolean first = true;
		for (int index = 0; index < components.size(); ++index) {
			if (!first) {
				builder.append(" | ");
			} else {
				first = !first;
			}
			builder.append(components.get(index));
		}
		return builder.toString();
	}

}
