/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.miner.functions;

import cz.matfyz.core.rsd2.PropertyHeuristics;
import org.apache.spark.api.java.function.Function;

/**
 *
 * @author simek.jan
 */
public class SuitableReferencePropertiesFilterFunction implements Function<PropertyHeuristics, Boolean>{
    @Override
    public Boolean call(PropertyHeuristics heuristics) throws Exception {
        // TODO: add more filters
        return (heuristics.getMin() != null);        
    }
}
