package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.inference.common.RecordSchemaDescriptionReducer;
import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;

public class DefaultLocalSeqFunction implements AbstractSeqFunction {

    @Override
    public ProcessedProperty call(ProcessedProperty t1, Iterable<ProcessedProperty> t2) throws Exception {
        // agreguje dohromady Object a statistiku, a tedy vklada objekt do min, max, average, inkrementuje count, inkrementuje totalValue, inkrementuje bloom filter

        ProcessedProperty result = null;

        for (ProcessedProperty property : t2) {
            if (result == null) {
                result = property;
            } else {
                RecordSchemaDescription mergedSchema = RecordSchemaDescriptionReducer.call(result.getSchema(), property.getSchema());
                result.setSchema(mergedSchema);
                                // moje
                                (result.getHeuristics()).merge(property.getHeuristics());
            }
        }

        return result;
    }
}
