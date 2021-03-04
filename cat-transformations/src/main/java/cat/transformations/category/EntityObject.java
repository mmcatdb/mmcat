/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class EntityObject implements CategoricalObject {

	@Override
	public String getName() {
		return entityName;
	}

    @Override
    public int size() {
        return activeDomain.size();
    }

    public void addValue(Object sid) {
        // WARN: TADY MUSIS LEPSI ROZHRANI VYMYSLET, TOHLE JE MOC NEBEZPECNE
        List<List<Object>> set = new ArrayList<>();
        List<Object> identifier = new ArrayList<>();
        identifier.add(sid);
        set.add(identifier);
        EntityValue value = new EntityValue(set);
        activeDomain.add(value);
    }

	public static final class EntityValue implements Comparable<EntityValue> {	// nebylo by vhodnejsi tohle prejmenovat na EntityKey? Kvuli RelationshipValue, aby bylo jasne, ze je to KV par

		private final List<List<Object>> setOfIdentifiers;// = new ArrayList<>();

		public EntityValue() {
			setOfIdentifiers = new ArrayList<>();
		}
                
		public EntityValue(List<List<Object>> setOfIdentifiers) {
			this.setOfIdentifiers = setOfIdentifiers;
		}

		@Override
		public boolean equals(Object object) {
			EntityValue value;
			if (object instanceof EntityValue) {
				value = (EntityValue) object;
			} else {
				return false;
			}
			if (setOfIdentifiers.size() != value.setOfIdentifiers.size()) {
				return false;
			}

			for (int index = 0; index < setOfIdentifiers.size(); ++index) {

				var identifier = setOfIdentifiers.get(index);
				var otherIdentifier = value.setOfIdentifiers.get(index);

				if (identifier.size() != otherIdentifier.size()) {
					return false;
				}

				for (int index2 = 0; index2 < identifier.size(); ++index2) {
					if (!identifier.get(index2).equals(otherIdentifier.get(index2))) {
						return false;
					}
				}

			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 89 * hash + Objects.hashCode(this.setOfIdentifiers);
			return hash;
		}

		@Override
		public int compareTo(EntityValue value) {
			if (setOfIdentifiers.size() != value.setOfIdentifiers.size()) {
				return -1;
			}
			return -1;
			// WARN: TODO THIS!!!!
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			boolean firstIdentifier = true;
			for (var identifier : setOfIdentifiers) {

				if (firstIdentifier) {
					firstIdentifier = !firstIdentifier;
				} else {
					builder.append(",");
				}

				builder.append("{");

				boolean firstAttribute = true;
				for (var attribute : identifier) {
					if (firstAttribute) {
						firstAttribute = !firstAttribute;
					} else {
						builder.append(",");
					}
					builder.append(attribute);
				}

				builder.append("}");
			}
			builder.append("}");

			return builder.toString();
		}
	}

//	// mnozina atributu, tedy superidentifikator
//	private final Set<Object> superidentifier = new TreeSet<>();
//	// mnozina klicu, ktera ma schopnost identifikovat entitni typ
//	private final List<List<Object>> identifiers = new ArrayList<>();
	private final Set<EntityObject.EntityValue> activeDomain = new TreeSet<>();

	private final String entityName;

	public EntityObject(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public void add(/*List<List<Object>>*/Object value) {
		EntityObject.EntityValue _value = (EntityObject.EntityValue) value;
		activeDomain.add(_value);
	}

	public EntityObject.EntityValue get(int index) {
		// WARN: NEEFEKTIVNI METODA!
		int index2 = 0;
		for (var value : activeDomain) {
			if (index2 == index) {
				return value;
			} else {
				++index2;
			}
		}
		return null;
	}

//	public void addIdentifier(List<Object> identifier) {
//		for (var attribute : identifier) {
//			superidentifier.add(attribute);
//		}
//
//		// throw exception if identifier already exists
//		identifiers.add(identifier);
//	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(entityName);
		builder.append(":\t");

		boolean firstSet = true;
		for (var setOfIdentifiers : activeDomain) {
			if (firstSet) {
				firstSet = !firstSet;
			} else {
				builder.append(",");
			}
			builder.append(setOfIdentifiers);

		}

		return builder.toString();
	}

}
