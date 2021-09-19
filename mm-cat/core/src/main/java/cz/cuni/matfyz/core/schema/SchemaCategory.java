/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class SchemaCategory implements Category {

	private final List<SchemaObject> objects = new ArrayList<>();
	private final List<SchemaMorphism> morphisms = new ArrayList<>();

	public SchemaCategory() {
	}

	public boolean addObject(SchemaObject object) {
		objects.add(object);
		return true;
	}

	public boolean addMorphism(SchemaMorphism morphism) {
		morphisms.add(morphism);
		morphism.setCategory(this);
		return true;
	}

	public SchemaMorphism dual(Signature signature) {
		for (SchemaMorphism morphism : morphisms) {
			if (morphism.signature().equals(signature)) {
				return morphism;
			}
		}

		return null;	// WARN: CHYBA! TOHLE NESMI NASTAT, PROTOZE KAZDY MORFISMUS MUSI MIT SVUJ DUALNI!
	}

}
