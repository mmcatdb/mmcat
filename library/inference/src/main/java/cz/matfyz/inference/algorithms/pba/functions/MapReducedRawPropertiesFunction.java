package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.Share;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

public class MapReducedRawPropertiesFunction implements Function<Tuple2<RawProperty, Share>, ProcessedProperty> {

    @Override
    public ProcessedProperty call(Tuple2<RawProperty, Share> tuple) throws Exception {
        RawProperty property = tuple._1;
        property.setCount(tuple._2.getTotal());
        property.setFirst(tuple._2.getFirst());
        return new ProcessedProperty(property);
    }

}
