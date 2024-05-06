/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil
 */
public class MongoRsdPairFunction implements PairFunction<RecordSchemaDescription, String, RecordSchemaDescription> {

	@Override
	public Tuple2<String, RecordSchemaDescription> call(RecordSchemaDescription t) throws Exception {
		return new Tuple2<>(t.getName(), t);
//		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

}
