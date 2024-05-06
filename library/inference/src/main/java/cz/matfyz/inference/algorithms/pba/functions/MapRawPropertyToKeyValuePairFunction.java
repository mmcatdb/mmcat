/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import java.io.Serializable;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MapRawPropertyToKeyValuePairFunction implements PairFunction<ProcessedProperty, String, ProcessedProperty>, Serializable {

	@Override
	public Tuple2<String, ProcessedProperty> call(ProcessedProperty property) throws Exception {
		// na vstupu vezme dvojici MyTuple a vytvori z ni key/value dvojici, aby se dale dala redukovat (grouppovat)
		Tuple2<String, ProcessedProperty> tuple = new Tuple2(property.getHierarchicalName(), property);
		return tuple;

	}

}
