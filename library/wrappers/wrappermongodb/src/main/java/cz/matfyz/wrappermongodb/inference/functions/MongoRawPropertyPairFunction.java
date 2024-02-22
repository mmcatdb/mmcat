/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb.inference.functions;

import org.apache.spark.api.java.function.PairFunction;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.Share;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class MongoRawPropertyPairFunction implements PairFunction<RawProperty, RawProperty, Share> {

	@Override
	public Tuple2<RawProperty, Share> call(RawProperty t) {
		return new Tuple2<>(t, new Share(t.getCount(), t.getFirst()));
	}

}
