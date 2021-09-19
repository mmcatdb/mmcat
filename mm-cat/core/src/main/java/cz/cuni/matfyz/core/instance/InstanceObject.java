/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.Property;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public class InstanceObject implements CategoricalObject {

	private final int key;
//	private String label;
	private final List<ActiveDomainRow> activeDomain;
//	private Set<Key> ids;

	public void addRecord(ActiveDomainRow record) {
		activeDomain.add(record);
	}

	public InstanceObject(int key) {
		this.key = key;
		activeDomain = new ArrayList<>();
	}

	public InstanceObject(SchemaObject object) {
		key = object.objectId();
		activeDomain = new ArrayList<>();

	}

	@Override
	public int objectId() {
		return key;
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

}
