/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.miner.functions;

import cz.matfyz.core.rsd2.PropertyHeuristics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

/**
 * 
 * @author simek.jan
 */
public class FlatMapToParentsFunction implements PairFlatMapFunction<PropertyHeuristics, String, PropertyHeuristics> {
    @Override
    public Iterator<Tuple2<String, PropertyHeuristics>> call(PropertyHeuristics heuristics) {
        List<Tuple2<String, PropertyHeuristics>> list = new ArrayList<>();
        list.add(new Tuple2<>(heuristics.getHierarchicalName(), heuristics));
        int index = heuristics.getHierarchicalName().lastIndexOf("/");
        String parentName = "";
        if (index != -1) {
           parentName = heuristics.getHierarchicalName().substring(0, index);    
        }
        list.add(new Tuple2<>(parentName, heuristics));
        return list.iterator();
    }
}
