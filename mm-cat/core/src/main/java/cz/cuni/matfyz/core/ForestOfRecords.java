/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 * @author pavel.koupil
 */
public class ForestOfRecords<T> implements Iterable {

	public Iterable<DataRecord> records() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterator iterator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void forEach(Consumer action) {
		Iterable.super.forEach(action); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Spliterator spliterator() {
		return Iterable.super.spliterator(); //To change body of generated methods, choose Tools | Templates.
	}

}
