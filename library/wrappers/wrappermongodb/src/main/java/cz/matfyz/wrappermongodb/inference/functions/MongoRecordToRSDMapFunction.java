package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.*;

import cz.matfyz.wrappermongodb.inference.helpers.MapMongoDocument;
import org.apache.spark.api.java.function.Function;
import org.bson.Document;

public class MongoRecordToRSDMapFunction implements Function<Document, RecordSchemaDescription> {

    @Override
    public RecordSchemaDescription call(Document t1) {
        return MapMongoDocument.INSTANCE.process(t1);
    }


}
