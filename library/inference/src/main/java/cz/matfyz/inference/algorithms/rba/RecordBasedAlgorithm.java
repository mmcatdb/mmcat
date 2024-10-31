package cz.matfyz.inference.algorithms.rba;

import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

public class RecordBasedAlgorithm {

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractRSDsReductionFunction merge) {
        wrapper.startSession();

        // FIXME What exception can be thrown here?
        // If it's truly necessary, the result of this function should be @Nullable.
        // Why isn't the result of this function checked for null in MMInferOneInAll? If we can't recover from an exception, we should ignore it and catch it on the top level.
        try {
            return wrapper.loadRSDs().reduce(merge);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        } finally {
            wrapper.stopSession();
        }
    }

}
