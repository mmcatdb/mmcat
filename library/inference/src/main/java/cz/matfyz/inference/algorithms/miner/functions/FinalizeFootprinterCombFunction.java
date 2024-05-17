package cz.matfyz.inference.algorithms.miner.functions;

import org.apache.spark.api.java.function.Function2;
import cz.matfyz.core.rsd.PropertyHeuristics;

public class FinalizeFootprinterCombFunction implements Function2<PropertyHeuristics, PropertyHeuristics, PropertyHeuristics> {
    @Override
    public PropertyHeuristics call(PropertyHeuristics h1, PropertyHeuristics h2) {
        h1.merge(h2);
        return h1;
    }
}
