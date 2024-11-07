package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.inference.algorithms.InferenceAlgorithmUtils;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 * The {@code DefaultLocalReductionFunction} class implements the {@code AbstractRSDsReductionFunction}
 * interface to provide a default reduction operation for merging two {@code RecordSchemaDescription} objects.
 * This reduction function combines two RSDs by aggregating various attributes and merging their children lists.
 *
 * <p>The reduction operation computes a new {@code RecordSchemaDescription} that:
 * <ul>
 *     <li>Combines the names of the RSDs.</li>
 *     <li>Aggregates the total and first share values.</li>
 *     <li>Finds the minimum unique and ID characters from the two inputs.</li>
 *     <li>Combines the model and type bit masks.</li>
 *     <li>Merges the children lists in an ordered manner using utility functions.</li>
 * </ul>
 *
 * <p>This class is designed for use in inference algorithms where reducing or merging schema descriptions is necessary.
 */
public class DefaultLocalReductionFunction implements AbstractRSDsReductionFunction {

    /**
     * Merges two {@code RecordSchemaDescription} objects into a single {@code RecordSchemaDescription}.
     * The resulting RSD aggregates properties from both input RSDs.
     */
    @Override
    public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        final RecordSchemaDescription result = new RecordSchemaDescription();    // initialize the result RSD
        final InferenceAlgorithmUtils utils = new InferenceAlgorithmUtils();

        // Set basic properties by combining the input RSDs' attributes
        result.setName(rsd1.getName());
        result.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());
        result.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
        result.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
        result.setId(Char.min(rsd1.getId(), rsd2.getId()));
        result.setModels(rsd1.getModels() | rsd2.getModels());
        result.setTypes(rsd1.getTypes() | rsd2.getTypes());

        // Merge the children lists of the RSDs
        result.setChildren(utils.mergeOrderedLists(rsd1.getChildren(), rsd2.getChildren()));

        return result;
    }
}
