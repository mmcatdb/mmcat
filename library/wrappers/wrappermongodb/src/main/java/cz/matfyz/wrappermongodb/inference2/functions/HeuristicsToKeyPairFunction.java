/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference2.functions;

import cz.matfyz.core.rsd2.PropertyHeuristics;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 *
 * @author simek.jan
 */
public class HeuristicsToKeyPairFunction implements PairFunction<PropertyHeuristics, String, PropertyHeuristics> {

	@Override
	public Tuple2<String, PropertyHeuristics> call(PropertyHeuristics t) throws Exception {
		return new Tuple2<>(t.getHierarchicalName(), t);
        } 
}
