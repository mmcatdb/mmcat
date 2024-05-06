/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.miner.functions;

import cz.matfyz.core.rsd2.ProcessedProperty;
import java.io.Serializable;

import cz.matfyz.core.rsd2.RawProperty;
import cz.matfyz.core.rsd2.Share;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MapReduceRawPropertiesFunction implements Function<Tuple2<RawProperty, Share>, ProcessedProperty>, Serializable {

	@Override
	public ProcessedProperty call(Tuple2<RawProperty, Share> tuple) throws Exception {
		RawProperty property = tuple._1;
		property.setCount(tuple._2.getTotal());
		property.setFirst(tuple._2.getFirst());
		return new ProcessedProperty(property);
	}

}