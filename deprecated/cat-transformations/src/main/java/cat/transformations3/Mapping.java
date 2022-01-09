package cat.transformations3;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class Mapping {

	private final String objectName;
	private final Set<String> paths;

	public Mapping(String objectName) {
		this.objectName = objectName;
		paths = new TreeSet<>();
	}

	public void addPath(String path) {
		paths.add(path);
	}

	public Set<String> getPaths() {
		return paths;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(objectName);
		builder.append(", {");
		paths.stream().forEach(path -> {
			builder.append(path);
			builder.append(", "); 
		});
		builder.append("}");
		builder.append(")");
		return builder.toString();
	}

}
