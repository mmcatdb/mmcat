package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class MapRawPropertyToKeyValuePairFunction implements PairFunction<ProcessedProperty, String, ProcessedProperty> {

    @Override
    public Tuple2<String, ProcessedProperty> call(ProcessedProperty property) throws Exception {
        // na vstupu vezme dvojici MyTuple a vytvori z ni key/value dvojici, aby se dale dala redukovat (grouppovat)
        Tuple2<String, ProcessedProperty> tuple = new Tuple2(property.getHierarchicalName(), property);
        return tuple;

    }

}
