/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference2.functions;

import cz.matfyz.core.rsd2.RecordSchemaDescription;
//import cz.cuni.matfyz.mminfer.persister.model.RecordSchemaDescription;
import java.io.Serializable;

import cz.matfyz.wrappermongodb.inference2.helpers.MapMongoDocument;
import org.apache.spark.api.java.function.PairFunction;
import org.bson.Document;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MongoRecordToPairFunction implements PairFunction<Document, String, RecordSchemaDescription>, Serializable {

	@Override
	public Tuple2<String, RecordSchemaDescription> call(Document t) throws Exception {
		//TODO má sa plniť anonymným názvom?
		return new Tuple2<>("_", MapMongoDocument.INSTANCE.process(t));
	}

}
