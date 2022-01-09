package cat.transformations4.category;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class AccessPath {

	private final String kindPath;
	private final List<String> attributePaths = new ArrayList<>();

	private final String topLevel;

	public AccessPath(String path) {
		path = path.replace(" ", "");

		if (path.contains("(")) {
			this.kindPath = path.substring(0, path.indexOf("("));
			String attributes = path.substring(path.indexOf("("));
			attributes = attributes.replace("(", "");
			attributes = attributes.replace(")", "");
			String[] atts = attributes.split(",");
			for (String a : atts) {
				attributePaths.add(a);
			}

		} else {
			this.kindPath = path;
		}

//		System.out.print(path.lastIndexOf("/") + " -> ");
		int last = path.lastIndexOf("/");
		if (last == 0) {
			if (path.contains("(")) {
				topLevel = path.substring(last + 1, path.indexOf("("));
				// obsahuje vycet atributu, takze do te doby
			} else {
				topLevel = path.substring(last + 1);
				// neobsahuje vycet atributu, takze do konce
			}
		} else {
			int index = path.substring(path.indexOf("/") + 1).indexOf("/") + 1;
//			System.out.println(index);
			topLevel = path.substring(1, index);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(kindPath);
		builder.append("(");
		boolean first = true;
		for (int index = 0; index < attributePaths.size(); ++index) {
			if (!first) {
				builder.append(", ");
			} else {
				first = !first;
			}
			builder.append(attributePaths.get(index));
		}
		builder.append(")");
		return builder.toString();
	}

}
