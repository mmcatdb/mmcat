/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.inference.common.RecordSchemaDescriptionReducer;
import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import cz.matfyz.core.rsd.PropertyHeuristics;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class DefaultLocalSeqFunction implements AbstractSeqFunction {

	@Override
	public ProcessedProperty call(ProcessedProperty t1, Iterable<ProcessedProperty> t2) throws Exception {
		// agreguje dohromady Object a statistiku, a tedy vklada objekt do min, max, average, inkrementuje count, inkrementuje totalValue, inkrementuje bloom filter

		ProcessedProperty result = null;

		for (ProcessedProperty property : t2) {
			if (result == null) {
				result = property;
			} else {
				RecordSchemaDescription mergedSchema = RecordSchemaDescriptionReducer.call(result.getSchema(), property.getSchema());
				result.setSchema(mergedSchema);
                                // moje
                                (result.getHeuristics()).merge(property.getHeuristics());
			}
		}

		return result;
	}
}
