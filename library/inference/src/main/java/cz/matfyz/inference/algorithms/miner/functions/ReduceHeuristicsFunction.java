package cz.matfyz.inference.algorithms.miner.functions;

import org.apache.spark.api.java.function.Function2;
import cz.matfyz.core.rsd.PropertyHeuristics;

public class ReduceHeuristicsFunction implements Function2<PropertyHeuristics, PropertyHeuristics, PropertyHeuristics> {

    @Override public PropertyHeuristics call(PropertyHeuristics h1, PropertyHeuristics h2) throws Exception {
            h1.setCount(h1.getCount() + h2.getCount());
            h1.setFirst(h1.getFirst() + h2.getFirst());
            h1.setUnique(false);
            return h1;
        }
}
