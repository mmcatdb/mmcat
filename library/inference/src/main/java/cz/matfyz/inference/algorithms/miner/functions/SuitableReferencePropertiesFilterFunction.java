package cz.matfyz.inference.algorithms.miner.functions;

import cz.matfyz.core.rsd.PropertyHeuristics;
import org.apache.spark.api.java.function.Function;

public class SuitableReferencePropertiesFilterFunction implements Function<PropertyHeuristics, Boolean> {
    @Override
    public Boolean call(PropertyHeuristics heuristics) throws Exception {
        // TODO: add more filters
        return (heuristics.getMin() != null);
    }
}
