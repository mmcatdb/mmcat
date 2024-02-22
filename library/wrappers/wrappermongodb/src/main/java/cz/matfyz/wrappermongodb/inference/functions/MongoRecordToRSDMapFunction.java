/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference.functions;

import java.io.Serializable;

import org.apache.spark.api.java.function.Function;
import org.bson.Document;

import cz.matfyz.core.rsd.*;
import cz.matfyz.wrappermongodb.inference.helpers.MapMongoDocument;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MongoRecordToRSDMapFunction implements Function<Document, RecordSchemaDescription>, Serializable {

	@Override
	public RecordSchemaDescription call(Document t1) {
		return MapMongoDocument.INSTANCE.process(t1);
	}


}
