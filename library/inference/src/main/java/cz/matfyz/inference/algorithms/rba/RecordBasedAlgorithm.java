package cz.matfyz.inference.algorithms.rba;

import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

public class RecordBasedAlgorithm {

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractRSDsReductionFunction merge) {
        wrapper.startSession();

        final var rsd = wrapper.loadRSDs().reduce(merge);

        wrapper.stopSession();

        return rsd;
    }

}
