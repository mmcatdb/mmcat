package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;

import cz.matfyz.wrappermongodb.inference.helpers.MapMongoDocument;
import org.apache.spark.api.java.function.PairFunction;
import org.bson.Document;
import scala.Tuple2;

public class MongoRecordToPairFunction implements PairFunction<Document, String, RecordSchemaDescription> {

    @Override
    public Tuple2<String, RecordSchemaDescription> call(Document t) throws Exception {
        //TODO má sa plniť anonymným názvom?
        return new Tuple2<>("_", MapMongoDocument.INSTANCE.process(t));
    }

}
