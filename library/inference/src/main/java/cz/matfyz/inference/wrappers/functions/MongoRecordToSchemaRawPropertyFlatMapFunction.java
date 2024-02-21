/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.wrappers.functions;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.Document;

import cz.matfyz.inference.model.RawProperty;
import cz.matfyz.inference.wrappers.helpers.MongoRecordToRawPropertyFlatMap;

/**
 *
 * @author pavel.koupil
 */
public class MongoRecordToSchemaRawPropertyFlatMapFunction implements FlatMapFunction<Document, RawProperty>, Serializable {

	String collectionName;

	public MongoRecordToSchemaRawPropertyFlatMapFunction(String collectionName) {
		this.collectionName = collectionName;
	}

	@Override
	public Iterator<RawProperty> call(Document t) {
		return MongoRecordToRawPropertyFlatMap.INSTANCE.process(collectionName, t, true, false);
	}


}
