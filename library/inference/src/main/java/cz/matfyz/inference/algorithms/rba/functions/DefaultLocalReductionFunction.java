package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.inference.common.RecordSchemaDescriptionReducer;
import cz.matfyz.core.rsd.RecordSchemaDescription;

public class DefaultLocalReductionFunction implements AbstractRSDsReductionFunction {

    @Override
    public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) throws Exception {
        return RecordSchemaDescriptionReducer.call(rsd1, rsd2);
    }

}
