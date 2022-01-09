package cat.transformations.category;

/**
 *
 * @author pavel.contos
 */
public interface CategoricalMorphism extends Comparable<CategoricalMorphism> {

	public abstract void add(EntityObject.EntityValue key, Object value);

	public abstract String getName();

	public abstract String getDomain();

	public abstract String getCodomain();

	public abstract Object getValue(EntityObject.EntityValue key);

}
