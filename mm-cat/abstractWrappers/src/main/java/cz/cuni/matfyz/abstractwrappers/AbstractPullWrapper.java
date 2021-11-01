/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractPullWrapper {

	public abstract ForestOfRecords pullForest(String selectAll, ComplexProperty path) throws Exception;

	public abstract ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception;

}
