/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.algorithms.rba;

import cz.cuni.matfyz.mminfer.model.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class FilterInvalidRSDFunction implements Function<RecordSchemaDescription, Boolean>, Serializable {

	public FilterInvalidRSDFunction() {
		super();
	}

	@Override
	public Boolean call(RecordSchemaDescription t1) throws Exception {
		return true;
	}

}
