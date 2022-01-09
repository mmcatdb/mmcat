package cat.transformations.algorithms2.model;

import java.util.List;

/**
 *
 * @author pavel.contos
 */
public interface AbstractIdentifier extends AbstractValue {
	
	public void add(List<Object> identifier);
	
	// jeden konkretni identifier? nebo superid?
//	protected List<AbstractProperty> identifier;

}
