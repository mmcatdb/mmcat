/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.miner.functions;

import cz.matfyz.core.rsd.PropertyHeuristics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

/**
 *
 * @author simek.jan
 */
public class SetRequiredTagFlatMapFunction implements PairFlatMapFunction<Tuple2<String, Iterable<PropertyHeuristics>>, String, PropertyHeuristics> {
    @Override
    public Iterator<Tuple2<String, PropertyHeuristics>> call(Tuple2<String, Iterable<PropertyHeuristics>> tuple) {
        List<Tuple2<String, PropertyHeuristics>> result = new ArrayList<>(); 
        int parentCount = 0;
        PropertyHeuristics parent = null;
        for (PropertyHeuristics p : tuple._2) {
            if (p.getHierarchicalName().equals(tuple._1)) {
                parentCount = p.getCount();
                parent = p;
                // result.add(new Tuple2<>(tuple._1, p));
                break;
            }
        }
        if (parent != null) {
            for (PropertyHeuristics p : tuple._2) {   
                if (!parent.equals(p)) {
                    if (parentCount == p.getCount()) {
                        p.setRequired(true);
                    }
                    result.add(new Tuple2<>(tuple._1, p));
                }
            }
        }   
        else {
            for (PropertyHeuristics p : tuple._2) {
                result.add(new Tuple2<>(tuple._1, p));
            }
        }
        return result.iterator();
    }
}
