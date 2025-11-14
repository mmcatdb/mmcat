package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.inference.algorithms.InferenceAlgorithmUtils;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 * The {@link DefaultLocalReductionFunction} class implements the {@link AbstractRSDsReductionFunction}
 * interface to provide a default reduction operation for merging two {@link RecordSchemaDescription} RSDs.
 * This reduction function combines two RSDs by aggregating various attributes and merging their children lists.
 *
 * <p>The reduction operation computes a new {@link RecordSchemaDescription} that:
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
     * Merges two {@link RecordSchemaDescription} RSDs into a single {@link RecordSchemaDescription}.
     * The resulting RSD aggregates properties from both input RSDs.
     */
    @Override public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        final var result = new RecordSchemaDescription(
            rsd1.getName(),
            Char.min(rsd1.getUnique(), rsd2.getUnique()),
            Char.min(rsd1.getId(), rsd2.getId()),
            rsd1.getShareTotal() + rsd2.getShareTotal(),
            rsd1.getShareFirst() + rsd2.getShareFirst()
        );

        result.setTypes(rsd1.getTypes() | rsd2.getTypes());

        // Merge the children lists of the RSDs
        final var utils = new InferenceAlgorithmUtils();
        result.setChildren(utils.mergeOrderedLists(rsd1.getChildren(), rsd2.getChildren()));

        return result;
    }

}
