package cz.matfyz.inference.algorithms;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InferenceAlgorithmUtils {

    private final DefaultLocalReductionFunction reductionFunction;

    public InferenceAlgorithmUtils() {
        this.reductionFunction = new DefaultLocalReductionFunction();
    }

    /**
     * Merges two ordered lists of {@link RecordSchemaDescription} objects and removes duplicates (by merging both to one object).
     * The input lists are expected to contain no duplicates by themselves, but the same object can be present in both lists.
     */
    public ObjectArrayList<RecordSchemaDescription> mergeOrderedLists(ObjectArrayList<RecordSchemaDescription> list1, ObjectArrayList<RecordSchemaDescription> list2) {
        final ObjectArrayList<RecordSchemaDescription> mergedList = new ObjectArrayList<>();

        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            final RecordSchemaDescription element1 = list1.get(i);
            final RecordSchemaDescription element2 = list2.get(j);

            final int comparison = element1.compareTo(element2);

            if (comparison < 0) {
                mergedList.add(element1);
                i++;
            } else if (comparison > 0) {
                mergedList.add(element2);
                j++;
            } else {
                RecordSchemaDescription union = reductionFunction.call(element1, element2);
                mergedList.add(union);
                i++;
                j++;
            }
        }

        for (; i < list1.size(); i++)
            mergedList.add(list1.get(i));

        for (; j < list2.size(); j++)
            mergedList.add(list2.get(j));

        return mergedList;
    }

}
