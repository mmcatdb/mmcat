package cz.matfyz.inference.algorithms.pba;

import cz.matfyz.inference.algorithms.pba.functions.AbstractCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.AbstractSeqFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.FinalizeSeqFunction;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

public class PropertyBasedAlgorithm {

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractSeqFunction merge, AbstractCombFunction merge2) {
        final JavaPairRDD<String, RecordSchemaDescription> propertiesToReduce = wrapper.loadPropertySchema();

        final JavaPairRDD<String, RecordSchemaDescription> reducedProperties = propertiesToReduce.reduceByKey(PropertyBasedAlgorithm::reduceProperties);

        final JavaRDD<RecordSchemaDescription> schemas = reducedProperties.map(Tuple2::_2);

        return schemas.aggregate(new RecordSchemaDescription(), new FinalizeCombFunction(), new FinalizeSeqFunction());
    }

     private static RecordSchemaDescription reduceProperties(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        rsd1.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
        rsd1.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());
        rsd1.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
        rsd1.setId(Char.min(rsd1.getId(), rsd2.getId()));
        rsd1.setTypes(rsd1.getTypes() | rsd2.getTypes());
//        rsd1.setChildren(mergeOrderedListsRemoveDuplicates(rsd1.getChildren(), rsd2.getChildren()));

        // TODO: REDUCE TAKE TYPY A DALSI?
        return rsd1;
    }

}
