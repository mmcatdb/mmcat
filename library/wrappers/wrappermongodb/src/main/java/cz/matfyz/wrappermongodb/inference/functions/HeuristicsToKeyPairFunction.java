package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.PropertyHeuristics;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class HeuristicsToKeyPairFunction implements PairFunction<PropertyHeuristics, String, PropertyHeuristics> {

    @Override
    public Tuple2<String, PropertyHeuristics> call(PropertyHeuristics t) throws Exception {
        return new Tuple2<>(t.getHierarchicalName(), t);
        }
}
