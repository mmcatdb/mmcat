package cat.transformations.algorithms2.model;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author pavel.contos
 */
public interface AbstractInstance {

	public abstract AbstractCategoricalObject getOrCreate(String name, AbstractObjectType type);

	public abstract AbstractCategoricalObject get(String name);

	public abstract boolean create(String name, AbstractObjectType type);

	public abstract AbstractCategoricalMorphism getOrCreateMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain);

	public abstract AbstractCategoricalMorphism getMorphism(String name);

	public abstract void createMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain);

	// NOTE: Odtud nize jsou nove metody pro rozhrani Inst -> Model algoritmu
	//
	public Set<Map.Entry<String, AbstractCategoricalObject>> objectsEntrySet();

	public Set<Map.Entry<String, AbstractCategoricalMorphism>> morphismsEntrySet();

	public int objectsSize();

	public Set<String> objectsKeySet();

	public Set<String> morphismsKeySet();

	public Set<String> morphismsKeySet(String name);

	public Set<String> objectsKeySet(AbstractObjectType abstractObjectType);

}
