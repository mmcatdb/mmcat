package cz.matfyz.inference.algorithms.miner.functions;

import org.apache.spark.api.java.function.Function;
import scala.Tuple2;
import cz.matfyz.core.rsd.PropertyHeuristics;

public class MapTupleToHeuristics implements Function<Tuple2<String, PropertyHeuristics>, PropertyHeuristics> {

    @Override
    public PropertyHeuristics call(Tuple2<String, PropertyHeuristics> tuple) throws Exception {
        return tuple._2;
    }

}
