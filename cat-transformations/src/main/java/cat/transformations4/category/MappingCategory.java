/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations4.category;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class MappingCategory {

	List<MappingObject> objects = new ArrayList<>();
	List<MappingMorphism> morphisms = new ArrayList<>();

	public void addObject(MappingObject object) {
		objects.add(object);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append(name);
//		builder.append("   ->   ");
//		boolean first = true;
		for (int index = 0; index < objects.size(); ++index) {
//			if (!first) {
//				builder.append(" | ");
//			} else {
//				first = !first;
//			}
			builder.append(objects.get(index));
			builder.append("\n");
		}
		return builder.toString();
	}

}
