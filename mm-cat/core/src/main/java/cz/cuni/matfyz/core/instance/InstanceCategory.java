/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class InstanceCategory implements Category {

	private final List<InstanceObject> objects = new ArrayList<>();
	private final List<InstanceMorphism> morphisms = new ArrayList<>();

	public InstanceCategory() {
		// vytvoreni prazdne schematicke kategorie
	}

	public InstanceCategory(SchemaCategory schema) {
		// vytvoreni instancni kategorie na zaklade schematicke kategorie
		for (SchemaObject object : schema.objects()) {
			objects.add(new InstanceObject(object));
		}

		for (SchemaMorphism morphism : schema.morphisms()) {
			morphisms.add(new InstanceMorphism(morphism, this));
		}
	}

	public boolean addObject(InstanceObject object) {
		objects.add(object);
		return true;
	}

	public InstanceObject object(int key) {
		for (InstanceObject object : objects) {
			if (object.objectId() == key) {
				return object;
			}
		}
		return null;
	}

	public boolean addMorphism(InstanceMorphism morphism) {
		morphisms.add(morphism);
		morphism.setCategory(this);
		return true;
	}

	public InstanceMorphism dual(Signature signature) {
		for (InstanceMorphism morphism : morphisms) {
			if (morphism.signature().equals(signature)) {
				return morphism;
			}
		}

		return null;	// WARN: CHYBA! TOHLE NESMI NASTAT, PROTOZE KAZDY MORFISMUS MUSI MIT SVUJ DUALNI!
	}

}
