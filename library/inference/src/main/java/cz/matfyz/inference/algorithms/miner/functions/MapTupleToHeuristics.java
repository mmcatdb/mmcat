/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.miner.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;
import cz.matfyz.core.rsd.PropertyHeuristics;

/**
 *
 * @author simek.jan
 */
public class MapTupleToHeuristics implements Function<Tuple2<String, PropertyHeuristics>, PropertyHeuristics>, Serializable {

	@Override
	public PropertyHeuristics call(Tuple2<String, PropertyHeuristics> tuple) throws Exception {
		return tuple._2;
	}

}
