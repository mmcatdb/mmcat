/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil
 */
public class ReduceRSDsFunction implements Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription>, Serializable {

	@Override
	public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) throws Exception {
		rsd1.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
		rsd1.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());

//		rsd1.setName(rsd1.getName());
//		rsd1.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());
//		rsd1.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
		rsd1.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
		rsd1.setId(Char.min(rsd1.getId(), rsd2.getId()));
		rsd1.setModels(rsd1.getModels() | rsd2.getModels());
		rsd1.setTypes(rsd1.getTypes() | rsd2.getTypes());
//		rsd1.setChildren(mergeOrderedListsRemoveDuplicates(rsd1.getChildren(), rsd2.getChildren()));

		// TODO: REDUCE TAKE TYPY A DALSI?
		return rsd1;
	}

}
