package cat.transformations.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class AttributeMorphism implements CategoricalMorphism {

	private final String morphismName;
	private final CategoricalObject domain;
	private final CategoricalObject codomain;
//	private final EntityObject.EntityValue key;
//	private final Object value;
	private final Set<KeyValuePair> mappings = new TreeSet<>();

	@Override
	public int compareTo(CategoricalMorphism other) {
		return morphismName.compareTo(other.getName());

	}

	@Override
	public String getName() {
		return morphismName;
	}

	@Override
	public String getDomain() {
		return domain.getName();
	}

	@Override
	public String getCodomain() {
		return codomain.getName();
	}

	@Override
	public Object getValue(EntityObject.EntityValue key) {
		for (var mapping : mappings) {
			if (mapping.key.equals(key)) {
				return mapping.value;
			}
		}
		return null;
	}

    public void addMapping(Object sid, Object value) {
//        System.out.println("AttributeMorphism -> addMapping() ... namisto sid posilej primo entityValue identifikatoru, at je to provazane, nebo lepe objekt identifikatoru");
        List<List<Object>> set = new ArrayList<>();
        List<Object> identifier = new ArrayList<>();
        identifier.add(sid);
        set.add(identifier);
        EntityObject.EntityValue entityValue = new EntityObject.EntityValue(set);
        mappings.add(new KeyValuePair(entityValue, value));
    }

	private static final class KeyValuePair implements Comparable<KeyValuePair> {

		private final EntityObject.EntityValue key;
		private final Object value;

		public KeyValuePair(EntityObject.EntityValue key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(KeyValuePair other) {
			int keyComparison = key.compareTo(other.key);

			// ERROR: Umoznovat pouze comparable objekty vkladat? protoze object neumoznuje compare to!
			// tohle umozni duplicity!
			return keyComparison != 0 ? keyComparison : -1;// value.compareTo(other.value);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(key);
			builder.append(",");
			builder.append(value);
			builder.append(")");
			return builder.toString();
		}
	}

	public AttributeMorphism(String morphismName, CategoricalObject domain, CategoricalObject codomain) {
		this.morphismName = morphismName;
		this.domain = domain;
		this.codomain = codomain;
	}

	@Override
	public void add(EntityObject.EntityValue key, Object value) {
		mappings.add(new KeyValuePair(key, value));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(morphismName);
		builder.append(":\t");

		builder.append("{");

		boolean firstValue = true;
		for (var value : mappings) {
			if (firstValue) {
				firstValue = !firstValue;
			} else {
				builder.append(",");
			}
			builder.append(value);
		}

		builder.append("}");
		return builder.toString();
	}

}
