package cz.matfyz.inference.algorithms;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import scala.Tuple2;

public class Footprinter {

    public static JavaRDD<PropertyHeuristics> process(AbstractInferenceWrapper wrapper) {

        final JavaRDD<PropertyHeuristics> loaded = wrapper.loadPropertyData();

        // map to (hierarchicalName, heuristics)
        final JavaPairRDD<String, PropertyHeuristics> toAgregate = loaded.mapToPair(t -> new Tuple2<>(t.getHierarchicalName(), t));

        // agregate by key
        final JavaPairRDD<String, PropertyHeuristics> aggregatedHeuristics = toAgregate.reduceByKey((h1, h2) -> {
            h1.merge(h2);
            return h1;
        });

        // trow away hierarchicalName
        final JavaRDD<PropertyHeuristics> onlyHeuristics2 = aggregatedHeuristics.map(Tuple2::_2);

        // nastaveni sequential tagu
        final JavaRDD<PropertyHeuristics> withSequential = onlyHeuristics2.map(Footprinter::setSequentialFlatMap);

        // namapujeme na (hierarchicalName, heuristics) a (parentName, heuristics)
        final JavaPairRDD<String, PropertyHeuristics> mappedToParents = withSequential.flatMapToPair(Footprinter::flatMapToParents);

        final JavaPairRDD<String, Iterable<PropertyHeuristics>> groupedWithParent = mappedToParents.groupByKey();

        // nastaveni required tagu
        final JavaPairRDD<String, PropertyHeuristics> withRequired = groupedWithParent.flatMapToPair(Footprinter::setRequiredTagFlatMap);

        // trow away hierarchicalName
        final JavaRDD<PropertyHeuristics> heuristics = withRequired.map(Tuple2::_2);

        // remove later
        /*heuristics.foreach(new VoidFunction<PropertyHeuristics>() {
            @Override public void call(PropertyHeuristics h) throws Exception {
                System.out.println(h);
            }
        });*/

        final var list = new ObjectArrayList<PropertyHeuristics>(heuristics.collect());

        return heuristics;
    }

    private static PropertyHeuristics setSequentialFlatMap(PropertyHeuristics heuristics) {
        if (!(heuristics.getMin() instanceof Number minNumber) || !(heuristics.getMax() instanceof Number maxNumber))
            return heuristics;

        final double min = minNumber.doubleValue();
        final double max = maxNumber.doubleValue();

        if (
            // Test if the values are integers.
            min % 1 == 0 &&
            max % 1 == 0 &&
            max - min <= heuristics.getCount() - 1
        )
            heuristics.setIsSequential(true);

        return heuristics;
    }

    private static Iterator<Tuple2<String, PropertyHeuristics>> flatMapToParents(PropertyHeuristics heuristics) {
        final var list = new ArrayList<Tuple2<String, PropertyHeuristics>>();
        list.add(new Tuple2<>(heuristics.getHierarchicalName(), heuristics));

        final int index = heuristics.getHierarchicalName().lastIndexOf("/");
        final String parentName = index != - 1
            ? heuristics.getHierarchicalName().substring(0, index)
            : "";

        list.add(new Tuple2<>(parentName, heuristics));

        return list.iterator();
    }

    private static Iterator<Tuple2<String, PropertyHeuristics>> setRequiredTagFlatMap(Tuple2<String, Iterable<PropertyHeuristics>> tuple) {
        final List<Tuple2<String, PropertyHeuristics>> result = new ArrayList<>();
        int parentCount = 0;
        PropertyHeuristics parent = null;

        for (final PropertyHeuristics p : tuple._2) {
            if (p.getHierarchicalName().equals(tuple._1)) {
                parentCount = p.getCount();
                parent = p;
                // result.add(new Tuple2<>(tuple._1, p));
                break;
            }
        }

        if (parent != null) {
            for (final PropertyHeuristics p : tuple._2) {
                if (!parent.equals(p)) {
                    if (parentCount == p.getCount())
                        p.setIsRequired(true);

                    result.add(new Tuple2<>(tuple._1, p));
                }
            }
        }
        else {
            for (final PropertyHeuristics p : tuple._2)
                result.add(new Tuple2<>(tuple._1, p));
        }

        return result.iterator();
    }

}
