/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.miner.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;
import cz.matfyz.core.rsd2.PropertyHeuristics;

/**
 *
 * @author simek.jan
 */
public class FinalizeFootprinterCombFunction implements Function2<PropertyHeuristics, PropertyHeuristics, PropertyHeuristics>, Serializable {
    @Override
    public PropertyHeuristics call(PropertyHeuristics h1, PropertyHeuristics h2) {
        h1.merge(h2);
        return h1;
    }
}
