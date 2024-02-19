/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.wrappers.functions;

import cz.cuni.matfyz.mminfer.model.*;

import java.io.Serializable;

import cz.cuni.matfyz.mminfer.wrappers.helpers.MapMongoDocument;
import org.apache.spark.api.java.function.Function;
import org.bson.Document;

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
