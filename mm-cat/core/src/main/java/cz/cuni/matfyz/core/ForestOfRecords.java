/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 *
 * @author pavel.koupil
 */
public class ForestOfRecords/*<T> extends AbstractList<T>*/ {
	
	// tady mas list recordu
	private List<RecordRoot> records = new ArrayList<>();
	// a navic tu mas mapu, ktera jako klic ma kategoricky identifikator objektu a jako hodnotu ma ukazatel do recordu, tedy do stromu, na konkretni misto!
	private Map<Object, RecordProperty> quickAccess = new TreeMap<>();
	

	public Iterable<RecordRoot> records() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

//	@Override
//	public Iterator iterator() {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}

//	@Override
//	public void forEach(Consumer action) {
//		Iterable.super.forEach(action); //To change body of generated methods, choose Tools | Templates.
//	}
//
//	@Override
//	public Spliterator spliterator() {
//		return Iterable.super.spliterator(); //To change body of generated methods, choose Tools | Templates.
//	}

}
