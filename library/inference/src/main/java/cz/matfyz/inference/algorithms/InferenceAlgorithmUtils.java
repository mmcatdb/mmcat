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
     * Merges two ordered lists of {@link RecordSchemaDescription} objects and removes duplicates.
     */
    public ObjectArrayList<RecordSchemaDescription> mergeOrderedLists(ObjectArrayList<RecordSchemaDescription> list1, ObjectArrayList<RecordSchemaDescription> list2) {
        final ObjectArrayList<RecordSchemaDescription> mergedList = new ObjectArrayList<>();

        int i = 0;
        int j = 0;
        while (i < list1.size() && j < list2.size()) {
            final RecordSchemaDescription element1 = list1.get(i);
            final RecordSchemaDescription element2 = list2.get(j);

            final int comparison = element1.compareTo(element2);

            switch (comparison) {
                // FIXME In general, the compareTo function can return any integer, not just -1, 0, or 1.
                case -1:
                    addIfNotDuplicate(mergedList, element1);
                    i++;
                    break;
                case 1:
                    addIfNotDuplicate(mergedList, element2);
                    j++;
                    break;
                default:
                    try {
                        // FIXME Why can this throw an exception?
                        RecordSchemaDescription union = reductionFunction.call(element1, element2);
                        addIfNotDuplicate(mergedList, union);
                    } catch (Exception e) {
                        throw new RuntimeException("Reduction Function failed.", e);
                    }
                    i++;
                    j++;
                    break;
            }
        }

        while (i < list1.size()) {
            addIfNotDuplicate(mergedList, list1.get(i));
            i++;
        }

        while (j < list2.size()) {
            addIfNotDuplicate(mergedList, list2.get(j));
            j++;
        }

        return mergedList;
    }

    private void addIfNotDuplicate(ObjectArrayList<RecordSchemaDescription> mergedList, RecordSchemaDescription element) {
        // FIXME Why is the comparison done by the `compareTo` function while the duplicate check is done by this arbitrary condition?
        // Either create a new function for the duplicate check on the `RecordSchemaDescription` class or use the `compareTo` function for the check.
        // If the second option is chosen, this function will be unnecessary because the `compareTo` function is already usec in the `mergeOrderedListsRemoveDuplicates` function.
        //
        // Also, if the lists are ordered, we only need to check the last element, right?
        // And if the original lists don't contain duplicates, the merged list won't either, because if the `compareTo` function returns 0, the elements are merged to one.
        final boolean exists = mergedList.stream()
            .anyMatch(e -> e.getName().equals(element.getName()) && e.getTypes() == element.getTypes());

        if (!exists)
            mergedList.add(element);
    }
}
