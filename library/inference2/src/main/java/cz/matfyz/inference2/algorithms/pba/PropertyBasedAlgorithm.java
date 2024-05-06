/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.pba;

import cz.matfyz.inference2.algorithms.pba.functions.AbstractCombFunction;
import cz.matfyz.inference2.algorithms.pba.functions.AbstractSeqFunction;
import cz.matfyz.inference2.algorithms.pba.functions.FinalizeCombFunction;
import cz.matfyz.inference2.algorithms.pba.functions.FinalizeSeqFunction;
import cz.matfyz.inference2.algorithms.pba.functions.MapProcessedPropertyToRecordSchemaDescription;
import cz.matfyz.inference2.algorithms.pba.functions.MapRawPropertyToKeyValuePairFunction;
import cz.matfyz.inference2.algorithms.pba.functions.MapReducedRawPropertiesFunction;
import cz.matfyz.inference2.algorithms.pba.functions.MapTupleToPropertySchemaDescription;
import cz.matfyz.inference2.algorithms.pba.functions.ReduceRSDsFunction;
import cz.matfyz.inference2.algorithms.pba.functions.ReduceRawPropertiesFunction;
import cz.matfyz.inference2.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.core.rsd2.ProcessedProperty;
import cz.matfyz.core.rsd2.RawProperty;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import cz.matfyz.core.rsd2.Share;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper2;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author pavel.koupil
 */
@Service
public class PropertyBasedAlgorithm {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

	public RecordSchemaDescription process(AbstractInferenceWrapper2 wrapper, AbstractSeqFunction merge, AbstractCombFunction merge2) {

		// create Spark Session (and initiate context)
		wrapper.buildSession();
		wrapper.initiateContext();

		try {
			long start = System.currentTimeMillis();

			JavaPairRDD<String, RecordSchemaDescription> propertiesToReduce = wrapper.loadPropertySchema();
			System.out.println("RESULT_TIME_PBA AFTER MAPPING: " + (System.currentTimeMillis() - start) + "ms");

			JavaPairRDD<String, RecordSchemaDescription> reducedProperties = propertiesToReduce.reduceByKey(
					new ReduceRSDsFunction()
			);

//			long count = reducedProperties.count();
//			System.out.println("RESULT_TIME_PBA COUNT OPTIMISED: " + count);
//			return null;
			JavaRDD<RecordSchemaDescription> schemas = reducedProperties.map(
					new MapTupleToPropertySchemaDescription()
			);

			RecordSchemaDescription result = schemas.aggregate(new RecordSchemaDescription(), new FinalizeCombFunction(), new FinalizeSeqFunction());
			System.out.println("RESULT_TIME_PBA AFTER_AGGREGATION_TO_SINGLE_RSD " + (System.currentTimeMillis() - start) + "ms");
			return result;

		} finally {
			// stop the Spark Session when everything is computed
			wrapper.stopSession();
		}
	}

}
