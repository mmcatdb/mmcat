/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference2.functions;

import cz.matfyz.core.rsd2.RawProperty;
import java.io.Serializable;
import java.util.*;

import cz.matfyz.wrappermongodb.inference2.helpers.MongoRecordToRawPropertyFlatMap;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.Document;

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
