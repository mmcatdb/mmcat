package cz.matfyz.inference.algorithms.pba;

import cz.matfyz.inference.algorithms.pba.functions.AbstractCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.AbstractSeqFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeSeqFunction;
import cz.matfyz.inference.algorithms.pba.functions.ReduceRSDsFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

public class PropertyBasedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyBasedAlgorithm.class);

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractSeqFunction merge, AbstractCombFunction merge2) {

        wrapper.startSession();

        try {
            JavaPairRDD<String, RecordSchemaDescription> propertiesToReduce = wrapper.loadPropertySchema();

            JavaPairRDD<String, RecordSchemaDescription> reducedProperties = propertiesToReduce.reduceByKey(
                new ReduceRSDsFunction()
            );

            JavaRDD<RecordSchemaDescription> schemas = reducedProperties.map(Tuple2::_2);

            RecordSchemaDescription result = schemas.aggregate(new RecordSchemaDescription(), new FinalizeCombFunction(), new FinalizeSeqFunction());
            return result;

        } finally {
            wrapper.stopSession();
        }
    }

}
