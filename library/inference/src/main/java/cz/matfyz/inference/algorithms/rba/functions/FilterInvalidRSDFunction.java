package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function;

public class FilterInvalidRSDFunction implements Function<RecordSchemaDescription, Boolean> {

    public FilterInvalidRSDFunction() {
        super();
    }

    @Override
    public Boolean call(RecordSchemaDescription t1) throws Exception {
        return true;
    }

}
