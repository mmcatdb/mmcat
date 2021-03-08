/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;

/**
 *
 * @author pavel.contos
 */
public interface AbstractKindSchema {

	public void createProperty(String name, String dataType, Cardinality cardinality);
	
}
