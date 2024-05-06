/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms;

import cz.matfyz.inference2.algorithms.miner.functions.ReduceHeuristicsFunction;
import cz.matfyz.inference2.algorithms.miner.functions.MapTupleToHeuristics;
import cz.matfyz.inference2.algorithms.miner.functions.FinalizeFootprinterCombFunction;
import cz.matfyz.core.rsd2.*;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper2;
import cz.matfyz.wrappermongodb.inference2.functions.HeuristicsToKeyPairFunction;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.matfyz.inference2.algorithms.miner.functions.SetSequentialFlatMapFunction;
import cz.matfyz.inference2.algorithms.miner.functions.FlatMapToParentsFunction;
import cz.matfyz.inference2.algorithms.miner.functions.SetRequiredTagFlatMapFunction;
import java.util.List;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public enum Footprinter {
	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(Footprinter.class);

	public JavaRDD<PropertyHeuristics> process(AbstractInferenceWrapper2 wrapper) {
                long start = System.currentTimeMillis();

                JavaPairRDD<String, PropertyHeuristics> heuristicsToReduce = wrapper.loadPropertyData();

                // merge properties with same key and value
                JavaPairRDD<String, PropertyHeuristics> reducedHeuristics = heuristicsToReduce.reduceByKey(
                                new ReduceHeuristicsFunction()
                );

                // trow away key value pair
                JavaRDD<PropertyHeuristics> onlyHeuristics = reducedHeuristics.map(
                                new MapTupleToHeuristics()
                );

                // map to (hierarchicalName, heuristics)
                JavaPairRDD<String, PropertyHeuristics> toAgregate = onlyHeuristics.mapToPair(
                                new HeuristicsToKeyPairFunction()
                );

                // agregate by key
                JavaPairRDD<String, PropertyHeuristics> aggregatedHeuristics = toAgregate.reduceByKey(
                                new FinalizeFootprinterCombFunction()
                );

                // trow away hierarchicalName
                JavaRDD<PropertyHeuristics> onlyHeuristics2 = aggregatedHeuristics.map(
                                new MapTupleToHeuristics()
                );
                 
                // nastaveni sequential tagu
                JavaRDD<PropertyHeuristics> withSequential = onlyHeuristics2.map(
                                new SetSequentialFlatMapFunction()
                );
                                
                // namapujeme na (hierarchicalName, heuristics) a (parentName, heuristics)
                JavaPairRDD<String, PropertyHeuristics> mappedToParents = withSequential.flatMapToPair(
                                new FlatMapToParentsFunction()
                );
                                
                JavaPairRDD<String, Iterable<PropertyHeuristics>> groupedWithParent = mappedToParents.groupByKey();
                                
                // nastaveni required tagu
                JavaPairRDD<String, PropertyHeuristics> withRequired = groupedWithParent.flatMapToPair(
                                new SetRequiredTagFlatMapFunction()
                );
                                
                // trow away hierarchicalName
                JavaRDD<PropertyHeuristics> heuristics = withRequired.map(
                                new MapTupleToHeuristics()
                );
                
                // remove later
                /*heuristics.foreach(new VoidFunction<PropertyHeuristics>() {
                    @Override
                    public void call(PropertyHeuristics h) throws Exception {
                        System.out.println(h);
                    }
                });*/

                List<PropertyHeuristics> list = new ObjectArrayList<>(heuristics.collect());

                // remove later
                for(PropertyHeuristics item : list) {
                        System.out.println(item.toString());
                }
                
                long end = System.currentTimeMillis();
                System.out.println("RESULT_TIME_NEW_FOOTPRINTER WITHOUT LIST CONVERSION: " + (end - start) + "ms");
                
                return heuristics;
	}

}
