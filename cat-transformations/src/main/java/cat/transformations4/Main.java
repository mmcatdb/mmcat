/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations4;

import cat.transformations4.category.AccessPath;
import cat.transformations4.category.MappingCategory;
import cat.transformations4.category.MappingComponent;
import cat.transformations4.category.MappingObject;

/**
 *
 * @author pavel.koupil
 */
public class Main {

	public Main() {
	}

	public static void main(String... args) {

		MappingCategory category = new MappingCategory();

		Main main = new Main();
		main.buildMappingCategory(category);
		System.out.println(category);

	}

	public void buildMappingCategory(MappingCategory category) {

		buildObject(category, "kind", "mongoDB", "/KIND(a->A, b->B)");
		buildObject(category, "a", "mongoDB", "/KIND(a->A)");
		buildObject(category, "b", "mongoDB", "/KIND(b->B)");
		buildObject(category, "array", "mongoDB", "/KIND/ARRAY");
		buildObject(category, "nested", "mongoDB", "/KIND/NESTED(c->C, d->D)");
		buildObject(category, "c", "mongoDB", "/KIND/NESTED(c->C)");
		buildObject(category, "d", "mongoDB", "/KIND/NESTED(d->D)");
		buildObject(category, "document", "mongoDB", "/KIND/ARRAY/[](e->E, f->F)");
		buildObject(category, "e", "mongoDB", "/KIND/ARRAY/[](e->E)");
		buildObject(category, "f", "mongoDB", "/KIND/ARRAY/[](f->F)");
	}

	private void buildObject(MappingCategory category, String name, String connectionString, String accessPath) {
		MappingObject object = new MappingObject(name);
		MappingComponent component = new MappingComponent(connectionString);
		AccessPath path = new AccessPath(accessPath);
		component.addAccessPath(path);
		object.addComponent(component);
		category.addObject(object);
	}

}
