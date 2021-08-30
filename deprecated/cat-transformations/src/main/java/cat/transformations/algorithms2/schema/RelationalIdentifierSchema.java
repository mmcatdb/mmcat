/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.contos
 */
public class RelationalIdentifierSchema implements AbstractIdentifierSchema {

	private final List<AbstractPropertySchema> compounds = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}

}
