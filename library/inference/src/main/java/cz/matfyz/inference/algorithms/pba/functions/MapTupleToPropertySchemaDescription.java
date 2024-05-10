package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

public class MapTupleToPropertySchemaDescription implements Function<Tuple2<String, RecordSchemaDescription>, RecordSchemaDescription> {

    @Override
    public RecordSchemaDescription call(Tuple2<String, RecordSchemaDescription> tuple) throws Exception {
        return tuple._2;
    }

}
