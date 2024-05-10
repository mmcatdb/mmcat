package cz.matfyz.wrappermongodb.inference.functions;

import scala.Tuple2;
import org.apache.spark.api.java.function.PairFunction;
import cz.matfyz.core.rsd.PropertyHeuristics;

public class MongoHeuristicsPairFunction implements PairFunction<PropertyHeuristics, String, PropertyHeuristics> {

    @Override
    public Tuple2<String, PropertyHeuristics> call(PropertyHeuristics t) throws Exception {
        // return new Tuple2<>(t.getHierarchicalName(), t);
                return new Tuple2<>(t.getHierarchicalName(), t);
    }

}
