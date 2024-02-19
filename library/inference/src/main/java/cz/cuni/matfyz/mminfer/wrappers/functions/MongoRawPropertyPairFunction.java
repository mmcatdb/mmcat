/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.wrappers.functions;

import cz.cuni.matfyz.mminfer.model.RawProperty;
import cz.cuni.matfyz.mminfer.model.Share;
import org.apache.spark.api.java.function.PairFunction;
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
