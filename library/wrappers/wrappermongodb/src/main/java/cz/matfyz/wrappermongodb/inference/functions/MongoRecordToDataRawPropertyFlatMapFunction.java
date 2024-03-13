/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference.functions;

import java.io.Serializable;
import java.util.*;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.Document;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.wrappermongodb.inference.helpers.MongoRecordToRawPropertyFlatMap;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MongoRecordToDataRawPropertyFlatMapFunction implements FlatMapFunction<Document, RawProperty>, Serializable {

    String collectionName;

    public MongoRecordToDataRawPropertyFlatMapFunction(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public Iterator<RawProperty> call(Document t) {
        return MongoRecordToRawPropertyFlatMap.INSTANCE.process(collectionName, t, false, true);
    }

}