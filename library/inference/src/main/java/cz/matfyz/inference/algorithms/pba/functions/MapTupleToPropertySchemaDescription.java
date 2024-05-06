/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil
 */
public class MapTupleToPropertySchemaDescription implements Function<Tuple2<String, RecordSchemaDescription>, RecordSchemaDescription>, Serializable {

	@Override
	public RecordSchemaDescription call(Tuple2<String, RecordSchemaDescription> tuple) throws Exception {
		return tuple._2;
	}

}
