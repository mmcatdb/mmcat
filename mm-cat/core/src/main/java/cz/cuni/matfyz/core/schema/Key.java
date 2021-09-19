/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.schema;

import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class Key implements Comparable<Key> {

	private List<Property> ids;

	public Key(List<Property> ids) {
		this.ids = ids;
	}

	public Key(Property... ids) {
		this.ids = List.of(ids);
	}

	@Override
	public int compareTo(Key o) {
		System.out.println("TODO: Key implementation of Comparable");
		return -1;
	}

}
