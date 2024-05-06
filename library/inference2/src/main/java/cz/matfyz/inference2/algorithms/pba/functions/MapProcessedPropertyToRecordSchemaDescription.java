/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.pba.functions;

import cz.matfyz.core.rsd2.ProcessedProperty;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil
 */
public class MapProcessedPropertyToRecordSchemaDescription implements Function<Tuple2<String, ProcessedProperty>, RecordSchemaDescription>, Serializable {

	@Override
	public RecordSchemaDescription call(Tuple2<String, ProcessedProperty> t1) throws Exception {
		RecordSchemaDescription schema = t1._2.getSchema();
		return schema;

	}

}
