/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import cat.transformations.commons.Pair;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalInstance implements AbstractInstance {

	private final Map<String, AbstractCategoricalObject> objects = new TreeMap<>();
//	private final Map<Pair, Set<AbstractCategoricalMorphism>> morphisms = new TreeMap<>();
	private final Map<String, AbstractCategoricalMorphism> morphisms = new TreeMap<>();

	@Override
	public AbstractCategoricalObject getOrCreate(String name, AbstractType type) {
		AbstractCategoricalObject result = null;
		switch (type) {
			case KIND ->
				result = getOrCreateEntity(name, type);
			case RECORD ->
				result = getOrCreateEntity(name, type);
			case ARRAY ->
				result = getOrCreateRelationship(name, type);
			case INLINED ->
				result = getOrCreateRelationship(name, type);
			case ATTRIBUTE ->
				result = getOrCreateAttribute(name, type);
			case MULTI_ATTRIBUTE ->
				result = getOrCreateAttribute(name, type);
			case INLINED_ATTRIBUTE ->
				result = getOrCreateAttribute(name, type);
			case STRUCTURED_ATTRIBUTE ->
				result = getOrCreateAttribute(name, type);
			case INLINED_STRUCTURED_ATTRIBUTE ->
				result = getOrCreateAttribute(name, type);
			case IDENTIFIER ->
				result = getOrCreateAttribute(name, type);
			case MULTI_IDENTIFIER ->
				result = getOrCreateAttribute(name, type);
			case REFERENCE ->
				result = null;
//				result = getOrCreate(name, type);
			case MULTI_REFERENCE ->
				result = null;
//				result = getOrCreate(name, type);
		}

		return result;
	}

	private CategoricalAttributeObject getOrCreateAttribute(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return (CategoricalAttributeObject) objects.get(name);// WARN: Exception - casting!
		} else {
			CategoricalAttributeObject attribute = new CategoricalAttributeObject(name, type);
			objects.put(name, attribute);
			return attribute;
		}
	}

	private CategoricalEntityObject getOrCreateEntity(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return (CategoricalEntityObject) objects.get(name);// WARN: Exception - casting!
		} else {
			CategoricalEntityObject entity = new CategoricalEntityObject(name, type);
			objects.put(name, entity);
			return entity;
		}
	}

	private CategoricalRelationshipObject getOrCreateRelationship(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return (CategoricalRelationshipObject) objects.get(name);// WARN: Exception - casting!
		} else {
			CategoricalRelationshipObject relationship = new CategoricalRelationshipObject(name, type);
			objects.put(name, relationship);
			return relationship;
		}
	}

	@Override
	public AbstractCategoricalObject get(String name) {	// TODO: pridavat jeste typ, ktery by to mel byt?
		if (objects.containsKey(name)) {
			return objects.get(name);
		} else {
			return null;
		}
	}

	@Override
	public boolean create(String name, AbstractType type) {
		boolean result = false;
		switch (type) {
			case KIND ->
				result = createEntity(name, type);
			case RECORD ->
				result = createEntity(name, type);
			case ARRAY ->
				result = createRelationship(name, type);
			case INLINED ->
				result = createRelationship(name, type);
			case ATTRIBUTE ->
				result = createAttribute(name, type);
			case MULTI_ATTRIBUTE ->
				result = createAttribute(name, type);
			case INLINED_ATTRIBUTE ->
				result = createAttribute(name, type);
			case STRUCTURED_ATTRIBUTE ->
				result = createAttribute(name, type);
			case INLINED_STRUCTURED_ATTRIBUTE ->
				result = createAttribute(name, type);
			case IDENTIFIER ->
				result = createAttribute(name, type);
			case MULTI_IDENTIFIER ->
				result = createAttribute(name, type);
			case REFERENCE ->
				result = false;
//				result = getOrCreate(name, type);
			case MULTI_REFERENCE ->
				result = false;
//				result = getOrCreate(name, type);
		}

		return result;
	}

	private boolean createAttribute(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return false;
		} else {
			CategoricalAttributeObject attribute = new CategoricalAttributeObject(name, type);
			objects.put(name, attribute);
			return true;
		}
	}

	private boolean createEntity(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return false;
		} else {
			CategoricalEntityObject entity = new CategoricalEntityObject(name, type);
			objects.put(name, entity);
			return true;
		}
	}

	private boolean createRelationship(String name, AbstractType type) {
		if (objects.containsKey(name)) {
			return false;
		} else {
			CategoricalRelationshipObject relationship = new CategoricalRelationshipObject(name, type);
			objects.put(name, relationship);
			return true;
		}
	}

	@Override
	public AbstractCategoricalMorphism getMorphism(String name) {
		return morphisms.get(name);
	}

	@Override
	public AbstractCategoricalMorphism getOrCreateMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
		if (morphisms.containsKey(name)) {
			return morphisms.get(name);
		} else {
			AbstractCategoricalMorphism morphism = new CategoricalMorphism(name, domain, codomain);
			morphisms.put(name, morphism);
			return morphism;
		}
	}

	@Override
	public void createMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain) {
//		Pair<AbstractCategoricalObject, AbstractCategoricalObject> pair = new Pair(domain, codomain);
		AbstractCategoricalMorphism morphism = new CategoricalMorphism(name, domain, codomain);
		morphisms.put(name, morphism);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Objects:\t");
		int index = 0;
		for (String key : objects.keySet()) {
			builder.append(key);
			if (++index < objects.size()) {
				builder.append(", ");
			}
		}
		builder.append("\n");

		for (String key : objects.keySet()) {
			var object = objects.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		for (String key : morphisms.keySet()) {
			var object = morphisms.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		return builder.toString();
	}

}
