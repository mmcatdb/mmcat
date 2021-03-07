/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.schema;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class RelationalKindSchema implements AbstractKindSchema {

	private final Map<String, AbstractPropertySchema> kinds = new TreeMap<>();

	private final Map<String, AbstractIdentifierSchema> identifiers = new TreeMap<>();

	private final Map<String, AbstractReferenceSchema> references = new TreeMap<>();
	// + dalsi integritni omezeni - zatim neresime
}
