/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

/**
 *
 * @author pavel.contos
 */
public class RelationalPropertySchema implements AbstractPropertySchema {

	private final String name;
	private final String dataType;

	public RelationalPropertySchema(String name, String dataType) {
		this.name = name;
		this.dataType = dataType;
	}

	// name
	// data type
	// integritni omezeni? na urovni atributu nebo na urovni tabulek?
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(":");
		builder.append(dataType);
		return builder.toString();
	}

}
