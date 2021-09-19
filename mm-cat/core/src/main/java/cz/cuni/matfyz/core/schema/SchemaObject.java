/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class SchemaObject implements CategoricalObject {

	private final int key;
	private String label;
	private Set<Property> superid;
	private Set<Key> ids;

	public SchemaObject(int key) {
		this(key, "", new TreeSet<>(), new TreeSet<>());
	}

	public SchemaObject(int key, String label) {
		this(key, label, new TreeSet<>(), new TreeSet<>());
	}

	public SchemaObject(int key, String label, Set<Property> superid, Set<Key> ids) {
		this.key = key;
		this.label = label;
		this.superid = superid;
		this.ids = ids;
	}

	@Override
	public int objectId() {
		return key;
	}

	public String label() {
		return label;
	}

	public Set<Property> superid() {
		return superid;
	}

	public Set<Key> ids() {
		return ids;
	}

	@Override
	public int compareTo(CategoricalObject o) {
		if (key > o.objectId()) {
			return 1;
		} else if (key < o.objectId()) {
			return -1;
		} else {
			return 0;
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CategoricalObject) {
			return objectId() == ((CategoricalObject) obj).objectId();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return super.toString(); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}

}
