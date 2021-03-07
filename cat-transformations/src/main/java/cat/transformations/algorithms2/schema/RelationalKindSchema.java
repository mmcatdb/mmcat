/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import cat.transformations.algorithms2.model.Cardinality;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class RelationalKindSchema implements AbstractKindSchema {

	private final String name;

	private final Map<String, AbstractPropertySchema> properties = new TreeMap<>();

	private final Map<String, AbstractIdentifierSchema> identifiers = new TreeMap<>();

	private final Map<String, AbstractReferenceSchema> references = new TreeMap<>();
	// + dalsi integritni omezeni - zatim neresime

	public RelationalKindSchema(String name) {
		this.name = name;
	}

	@Override
	public void createProperty(String name, String dataType, Cardinality cardinality) {
		AbstractPropertySchema property = new RelationalPropertySchema(name, dataType);
		properties.put(name, property);
		// ty muzes ignorovat kardinalitu u relacniho modelu, ale jinde ne...
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("(");

		int index = 0;
		for (String propertyName : properties.keySet()) {

			builder.append(properties.get(propertyName));
			if (++index < properties.size()) {
				builder.append(",");
			}
		}

		builder.append(")");
		builder.append("IDS:{TODO}");
		builder.append("FKS:{TODO}");
		return builder.toString();
	}
}
