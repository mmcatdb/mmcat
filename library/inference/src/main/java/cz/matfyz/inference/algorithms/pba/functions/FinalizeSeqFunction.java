package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function2;

public class FinalizeSeqFunction implements Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription> {

    @Override public RecordSchemaDescription call(RecordSchemaDescription t1, RecordSchemaDescription t2) throws Exception {
        return t2.getChildren().isEmpty()
            ? t2
            : (RecordSchemaDescription) t2.getChildren().toArray()[0];    //remove fake root element
    }

}
