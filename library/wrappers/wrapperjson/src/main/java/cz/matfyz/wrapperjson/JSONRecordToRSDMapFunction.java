package cz.matfyz.wrapperjson;

import java.io.Serializable;

import org.apache.spark.api.java.function.Function;
import org.bson.Document;

import cz.matfyz.core.rsd.*;


public class JSONRecordToRSDMapFunction implements Function<Document, RecordSchemaDescription>, Serializable {

    @Override
    public RecordSchemaDescription call(Document v1) throws Exception {
        return MapJSONDocument.INSTANCE.process(v1);
    }

}
