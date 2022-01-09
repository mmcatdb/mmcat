package cat.transformations.category;

import java.util.List;

/**
 *
 * @author pavel.contos
 */
public interface CategoricalObject {

	public abstract void add(Object object);
	
	public abstract String getName();

    public int size();
	
}
