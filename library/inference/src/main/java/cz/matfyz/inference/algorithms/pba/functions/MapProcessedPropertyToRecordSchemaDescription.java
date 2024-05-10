package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

public class MapProcessedPropertyToRecordSchemaDescription implements Function<Tuple2<String, ProcessedProperty>, RecordSchemaDescription> {

    @Override
    public RecordSchemaDescription call(Tuple2<String, ProcessedProperty> t1) throws Exception {
        RecordSchemaDescription schema = t1._2.getSchema();
        return schema;

    }

}
