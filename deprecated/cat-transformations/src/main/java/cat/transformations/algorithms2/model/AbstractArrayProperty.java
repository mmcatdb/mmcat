package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.contos
 */
public interface AbstractArrayProperty extends AbstractProperty {

	public abstract Iterable<AbstractProperty> getElements();

	public String getName();

	public void add(AbstractProperty property);

}
