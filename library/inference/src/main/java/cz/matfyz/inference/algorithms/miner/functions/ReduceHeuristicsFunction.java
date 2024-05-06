/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.miner.functions;

import org.apache.spark.api.java.function.Function2;
import java.io.Serializable;
import cz.matfyz.core.rsd.PropertyHeuristics;
import java.util.Objects;

/**
 *
 * @author simek.jan
 */
public class ReduceHeuristicsFunction implements Function2<PropertyHeuristics, PropertyHeuristics, PropertyHeuristics>, Serializable {
        
	@Override
	public PropertyHeuristics call(PropertyHeuristics h1, PropertyHeuristics h2) throws Exception {           
            h1.setCount(h1.getCount() + h2.getCount());
            h1.setFirst(h1.getFirst() + h2.getFirst());
            h1.setUnique(false);
            return h1;
        }
}
