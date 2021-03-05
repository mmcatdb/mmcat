/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class DocumentModel implements AbstractModel {

	Map<String, AbstractKind> kinds = new TreeMap<>();

	public DocumentModel() {
	}

	@Override
	public Iterable<String> getKindNames() {
		return kinds.keySet();
	}

	@Override
	public AbstractKind getKind(String name) {
		return kinds.get(name);
	}

	@Override
	public Iterable<AbstractKind> getKinds() {
		return kinds.values();
	}

	@Override
	public void putKind(String name, AbstractKind kind) {
		kinds.put(name, kind);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

//		for (Map.Entry<String, AbstractKind> entry : kinds.entrySet()) {
		kinds.entrySet().forEach(entry -> {
			builder.append(makeLine());
			builder.append(entry.getKey());
			builder.append("\n");
			builder.append(makeLine());

			// documents
			builder.append(entry.getValue());

			builder.append(makeLine());

		});
//		}

		return builder.toString();
	}

	private String makeLine() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 120; ++i) {
			builder.append("-");
		}
		builder.append("\n");
		return builder.toString();
	}

}
