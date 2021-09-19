/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature2;

/**
 *
 * @author pavel.koupil
 */
public class Property implements Comparable<Property> {

	private Signature2 signature;
	private String name;

	public Property(Signature2 signature, String name) {
		this.signature = signature;
		this.name = name;
	}

	@Override
	public int compareTo(Property o) {
		return name.compareTo(o.name);
	}

}
