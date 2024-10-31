package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.inference.algorithms.InferenceAlgorithmUtils;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;

// FIXME This class would benefit from some documentation.
public class DefaultLocalReductionFunction implements AbstractRSDsReductionFunction {

    @Override public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) throws Exception {
        final RecordSchemaDescription result = new RecordSchemaDescription();    // replace result for rsd1
        final InferenceAlgorithmUtils utils = new InferenceAlgorithmUtils();

        result.setName(rsd1.getName());
        result.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());
        result.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
        result.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
        result.setId(Char.min(rsd1.getId(), rsd2.getId()));
        result.setModels(rsd1.getModels() | rsd2.getModels());
        result.setTypes(rsd1.getTypes() | rsd2.getTypes());
        result.setChildren(utils.mergeOrderedLists(rsd1.getChildren(), rsd2.getChildren()));

        return result;
    }

}
