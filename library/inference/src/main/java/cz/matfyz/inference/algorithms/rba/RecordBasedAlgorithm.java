package cz.matfyz.inference.algorithms.rba;

import cz.matfyz.inference.algorithms.rba.functions.FilterInvalidRSDFunction;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordBasedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractRSDsReductionFunction merge) {
        wrapper.buildSession();
        wrapper.initiateContext();

        try {
            long start = System.currentTimeMillis();
            JavaRDD<RecordSchemaDescription> allRSDs = wrapper.loadRSDs();
            // System.out.println("RESULT_TIME_RBA OF MAPPIND AFTER: " + (System.currentTimeMillis() - start) + "ms");

            JavaRDD<RecordSchemaDescription> rsds = allRSDs.filter(new FilterInvalidRSDFunction());    // odstrani se prazdne nebo nevalidni RSDs
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
