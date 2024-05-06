/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms.rba;

import cz.matfyz.inference2.algorithms.rba.functions.FilterInvalidRSDFunction;
import cz.matfyz.inference2.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper2;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author pavel.koupil
 */
@Service
public class RecordBasedAlgorithm {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

	public RecordSchemaDescription process(AbstractInferenceWrapper2 wrapper, AbstractRSDsReductionFunction merge) {
		wrapper.buildSession();
		wrapper.initiateContext();

		try {
			long start = System.currentTimeMillis();
			JavaRDD<RecordSchemaDescription> allRSDs = wrapper.loadRSDs();
			// System.out.println("RESULT_TIME_RBA OF MAPPIND AFTER: " + (System.currentTimeMillis() - start) + "ms");

			JavaRDD<RecordSchemaDescription> rsds = allRSDs.filter(new FilterInvalidRSDFunction());	// odstrani se prazdne nebo nevalidni RSDs
			// System.out.println("RESULT_TIME_RBA OF FILTERING AFTER: " + (System.currentTimeMillis() - start) + "ms");

			RecordSchemaDescription rsd = rsds.reduce(merge);
			// System.out.println("RESULT_TIME_RBA OF MERGE AFTER: " + (System.currentTimeMillis() - start) + "ms");
			return rsd;

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		} finally {
			wrapper.stopSession();
		}
	}

}
