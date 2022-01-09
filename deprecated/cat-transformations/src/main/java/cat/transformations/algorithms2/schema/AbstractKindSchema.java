package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;

/**
 *
 * @author pavel.contos
 */
public interface AbstractKindSchema {

	public void createProperty(String name, String dataType, Cardinality cardinality);
	
}
