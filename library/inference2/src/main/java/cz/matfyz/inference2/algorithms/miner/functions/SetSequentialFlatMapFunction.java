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
public class SetSequentialFlatMapFunction implements Function<PropertyHeuristics, PropertyHeuristics> {
    @Override
    public PropertyHeuristics call(PropertyHeuristics heuristics) {
        if (heuristics.getMin() instanceof Number && heuristics.getMax() instanceof Number) {
            double min = ((Number) heuristics.getMin()).doubleValue();
            double max = ((Number) heuristics.getMax()).doubleValue();
            if (min % 1 == 0 && max % 1 == 0) {    //test if the values are integers
                if (max - min <= heuristics.getCount() - 1) {
                    heuristics.setSequential(true);
                }
            }
        }
        return heuristics;
    }
}
