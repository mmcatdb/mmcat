package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;

public class DefaultLocalCombFunction implements AbstractCombFunction {

    @Override public ProcessedProperty call(ProcessedProperty t1, ProcessedProperty t2) throws Exception {
        // TODO: comment back
        final var h1 = t1.getHeuristics();
        h1.merge(t2.getHeuristics());
        t1.setHeuristics(h1);
        // heuristics.merge(property.getHeuristics());      // REMOVE COMMENT IN ORDER TO FIX CANDIDATE MINER ALGORITHM

        final var reductionFunction = new DefaultLocalReductionFunction();
        final var mergedSchema = reductionFunction.call(t1.getSchema(), t2.getSchema());
        t1.setSchema(mergedSchema);

        return t1;
    }

}

