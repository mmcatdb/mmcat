/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class Main {

	List<Mapping> mappings = new ArrayList();
	Map<String, Mapping> output = new TreeMap<>();

	public void createMappings() {
		mappings.clear();

		String[] objects = {"kind", "nested", "a", "b", "array", "c", "document", "d", "e"};
		String[] paths = {"/KIND(a->A1,b->A2)", "/KIND/NESTED(c->A3)", "/KIND(a->A1)", "/KIND(b->A2)", "/KIND/ARRAY", "/KIND/NESTED(c->A3)", "/KIND/ARRAY/[](d->A4,e->A5)", "/KIND/ARRAY/[](d->A4)", "/KIND/ARRAY/[](e->A5)"};

		for (int index = 0; index < objects.length; ++index) {
			String object = objects[index];
			String path = paths[index];

			Mapping mapping = new Mapping(object);
			mapping.addPath(path);
			mappings.add(mapping);
		}
	}

	private String getTopLevelNavigationStep(String path) {
		System.out.print(path.lastIndexOf("/") + " -> ");
		int last = path.lastIndexOf("/");
		if (last == 0) {
			if (path.contains("(")) {
				return path.substring(last + 1, path.indexOf("("));
				// obsahuje vycet atributu, takze do te doby
			} else {
				return path.substring(last + 1);
				// neobsahuje vycet atributu, takze do konce
			}
		} else {
			int index = path.substring(path.indexOf("/") + 1).indexOf("/") + 1;
//			System.out.println(index);
			return path.substring(1, index);
		}
//		return "";
	}

	public void map() {
		// vstupem je mappings
		// výstupem je nejaka hashmap

		output.clear();

		for (Mapping mapping : mappings) {
			mapping.getPaths().stream().forEach(path -> {
				String topLevel = getTopLevelNavigationStep(path);
				System.out.println(topLevel);
			});

		}
	}

	public static void main(String... args) {
		Main main = new Main();
		main.createMappings();
		System.out.println(main);
		main.map();
		System.out.println(main);

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Mapping mapping : mappings) {
			builder.append(mapping);
			builder.append("\n");
		}
		return builder.toString();
	}

}
