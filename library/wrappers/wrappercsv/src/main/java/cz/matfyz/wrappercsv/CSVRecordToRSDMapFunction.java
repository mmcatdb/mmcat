package cz.matfyz.wrappercsv;

import java.io.Serializable;
import java.util.Map;

import org.apache.spark.api.java.function.Function;

import cz.matfyz.core.rsd.*;

public class CSVRecordToRSDMapFunction implements Function<Map<String, String>,RecordSchemaDescription>, Serializable{

    @Override
    public RecordSchemaDescription call(Map<String, String> v1) throws Exception {
        return MapCSVDocument.INSTANCE.process(v1);
    }

    
}
