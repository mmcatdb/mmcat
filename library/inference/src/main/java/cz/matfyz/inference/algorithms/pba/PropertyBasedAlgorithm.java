package cz.matfyz.inference.algorithms.pba;

import cz.matfyz.inference.algorithms.pba.functions.AbstractCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.AbstractSeqFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeSeqFunction;
import cz.matfyz.inference.algorithms.pba.functions.ReduceRSDsFunction;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.Tuple2;

@Service
public class PropertyBasedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractSeqFunction merge, AbstractCombFunction merge2) {

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

//            long count = reducedProperties.count();
//            System.out.println("RESULT_TIME_PBA COUNT OPTIMISED: " + count);
//            return null;
            JavaRDD<RecordSchemaDescription> schemas = reducedProperties.map(Tuple2::_2);

            RecordSchemaDescription result = schemas.aggregate(new RecordSchemaDescription(), new FinalizeCombFunction(), new FinalizeSeqFunction());
            System.out.println("RESULT_TIME_PBA AFTER_AGGREGATION_TO_SINGLE_RSD " + (System.currentTimeMillis() - start) + "ms");
            return result;

        } finally {
            // stop the Spark Session when everything is computed
            wrapper.stopSession();
        }
    }

}
