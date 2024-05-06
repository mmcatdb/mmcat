/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.pba.functions;

import cz.matfyz.core.rsd2.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil
 */
public class FinalizeSeqFunction implements Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription>, Serializable {

	@Override
	public RecordSchemaDescription call(RecordSchemaDescription t1, RecordSchemaDescription t2) throws Exception {
		if (t2.getChildren().isEmpty()) {
			return t2;
		} else {
			return (RecordSchemaDescription) t2.getChildren().toArray()[0];	//remove fake root element
		}

	}

}
