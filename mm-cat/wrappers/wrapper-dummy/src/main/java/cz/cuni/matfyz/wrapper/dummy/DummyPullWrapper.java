/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.wrapper.dummy;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import java.util.Collection;

/**
 *
 * @author pavel.koupil
 */
public class DummyPullWrapper implements AbstractPullWrapper {

	@Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path) throws Exception {
		// dummy wrapper, ktery nacte data z DB
		Collection<String> data = DataProvider.getData();
		
		
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception {
		// dummy wrapper, ktery nacte data z DB
		Collection<String> data = DataProvider.getData();
		
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
