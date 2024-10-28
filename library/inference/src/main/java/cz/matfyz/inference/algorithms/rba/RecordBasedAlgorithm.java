package cz.matfyz.inference.algorithms.rba;

import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordBasedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractRSDsReductionFunction merge) {
        wrapper.startSession();

        try {
            JavaRDD<RecordSchemaDescription> allRSDs = wrapper.loadRSDs();

            RecordSchemaDescription rsd = allRSDs.reduce(merge);
            return rsd;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        } finally {
            wrapper.stopSession();
        }
    }

}
