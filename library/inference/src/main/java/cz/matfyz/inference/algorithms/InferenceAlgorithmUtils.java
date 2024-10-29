package cz.matfyz.inference.algorithms;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InferenceAlgorithmUtils {

    private final DefaultLocalReductionFunction reductionFunction;

    public InferenceAlgorithmUtils() {
        this.reductionFunction = new DefaultLocalReductionFunction();
    }

    public ObjectArrayList<RecordSchemaDescription> mergeOrderedListsRemoveDuplicates(
            ObjectArrayList<RecordSchemaDescription> list1, ObjectArrayList<RecordSchemaDescription> list2) {

        ObjectArrayList<RecordSchemaDescription> mergedList = new ObjectArrayList<>();
        int i = 0;
        int j = 0;

        while (i < list1.size() && j < list2.size()) {
            RecordSchemaDescription element1 = list1.get(i);
            RecordSchemaDescription element2 = list2.get(j);

            int comparison = element1.compareTo(element2);

            switch (comparison) {
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
        boolean exists = mergedList.stream()
            .anyMatch(e -> e.getName().equals(element.getName()) && e.getTypes() == element.getTypes());

        if (!exists)
            mergedList.add(element);
    }
}
