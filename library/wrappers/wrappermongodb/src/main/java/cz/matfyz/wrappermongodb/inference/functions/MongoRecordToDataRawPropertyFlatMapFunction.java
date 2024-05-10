package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.RawProperty;
import java.util.*;

import cz.matfyz.wrappermongodb.inference.helpers.MongoRecordToRawPropertyFlatMap;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.Document;

public class MongoRecordToDataRawPropertyFlatMapFunction implements FlatMapFunction<Document, RawProperty> {

    String collectionName;

    public MongoRecordToDataRawPropertyFlatMapFunction(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public Iterator<RawProperty> call(Document t) {
        return MongoRecordToRawPropertyFlatMap.INSTANCE.process(collectionName, t, false, true);
    }

}
