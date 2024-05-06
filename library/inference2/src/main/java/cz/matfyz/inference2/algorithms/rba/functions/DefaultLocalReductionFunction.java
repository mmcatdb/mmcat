/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.rba.functions;

import cz.matfyz.inference2.common.RecordSchemaDescriptionReducer;
import cz.matfyz.core.rsd2.RecordSchemaDescription;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class DefaultLocalReductionFunction implements AbstractRSDsReductionFunction {

	@Override
	public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) throws Exception {
		return RecordSchemaDescriptionReducer.call(rsd1, rsd2);
	}

}
