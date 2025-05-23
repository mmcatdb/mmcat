package cz.matfyz.inference.algorithms;

import cz.matfyz.inference.algorithms.miner.functions.ReduceHeuristicsFunction;
import cz.matfyz.inference.algorithms.miner.functions.FinalizeFootprinterCombFunction;
import cz.matfyz.core.rsd.*;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.matfyz.inference.algorithms.miner.functions.SetSequentialFlatMapFunction;
import cz.matfyz.inference.algorithms.miner.functions.FlatMapToParentsFunction;
import cz.matfyz.inference.algorithms.miner.functions.SetRequiredTagFlatMapFunction;
import java.util.List;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import scala.Tuple2;

public enum Footprinter {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Footprinter.class);

    public JavaRDD<PropertyHeuristics> process(AbstractInferenceWrapper wrapper) {

        JavaPairRDD<String, PropertyHeuristics> heuristicsToReduce = wrapper.loadPropertyData();

        // merge properties with same key and value
        JavaPairRDD<String, PropertyHeuristics> reducedHeuristics = heuristicsToReduce.reduceByKey(
            new ReduceHeuristicsFunction()
        );

        // trow away key value pair
        JavaRDD<PropertyHeuristics> onlyHeuristics = reducedHeuristics.map(Tuple2::_2);

        // map to (hierarchicalName, heuristics)
        JavaPairRDD<String, PropertyHeuristics> toAgregate = onlyHeuristics.mapToPair(t -> new Tuple2<>(t.getHierarchicalName(), t));

        // agregate by key
        JavaPairRDD<String, PropertyHeuristics> aggregatedHeuristics = toAgregate.reduceByKey(
            new FinalizeFootprinterCombFunction()
        );

        // trow away hierarchicalName
        JavaRDD<PropertyHeuristics> onlyHeuristics2 = aggregatedHeuristics.map(Tuple2::_2);

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
        JavaRDD<PropertyHeuristics> heuristics = withRequired.map(Tuple2::_2);

        // remove later
        /*heuristics.foreach(new VoidFunction<PropertyHeuristics>() {
            @Override public void call(PropertyHeuristics h) throws Exception {
                System.out.println(h);
            }
        });*/

        List<PropertyHeuristics> list = new ObjectArrayList<>(heuristics.collect());

        return heuristics;
    }

}
