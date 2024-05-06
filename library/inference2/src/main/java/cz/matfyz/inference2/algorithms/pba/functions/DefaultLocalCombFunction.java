/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.pba.functions;

import cz.matfyz.inference2.common.RecordSchemaDescriptionReducer;
import cz.matfyz.core.rsd2.ProcessedProperty;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import cz.matfyz.core.rsd2.PropertyHeuristics;

/**
 *
 * @author pavel.koupil
 */
public class DefaultLocalCombFunction implements AbstractCombFunction {

	@Override
	public ProcessedProperty call(ProcessedProperty t1, ProcessedProperty t2) throws Exception {
		// TODO: comment back
                PropertyHeuristics h1 = t1.getHeuristics();
                h1.merge(t2.getHeuristics());
                t1.setHeuristics(h1);
		// heuristics.merge(property.getHeuristics());	     // REMOVE COMMENT IN ORDER TO FIX CANDIDATE MINER ALGORITHM

		RecordSchemaDescription mergedSchema = RecordSchemaDescriptionReducer.call(t1.getSchema(), t2.getSchema());
		t1.setSchema(mergedSchema);
		return t1;
	}

}
