package cz.matfyz.inference.common;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordSchemaDescriptionReducer {

    public static RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        return process(rsd1, rsd2);
    }

    private static RecordSchemaDescription process(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        RecordSchemaDescription result = new RecordSchemaDescription();    // replace result for rsd1

        result.setName(rsd1.getName());
        result.setShareTotal(rsd1.getShareTotal() + rsd2.getShareTotal());
        result.setShareFirst(rsd1.getShareFirst() + rsd2.getShareFirst());
        result.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
        result.setId(Char.min(rsd1.getId(), rsd2.getId()));
        result.setModels(rsd1.getModels() | rsd2.getModels());
        result.setTypes(rsd1.getTypes() | rsd2.getTypes());
        result.setChildren(mergeOrderedListsRemoveDuplicates(rsd1.getChildren(), rsd2.getChildren()));

        return result;
    }

    public static ObjectArrayList<RecordSchemaDescription> mergeOrderedListsRemoveDuplicates(
        ObjectArrayList<RecordSchemaDescription> list1, ObjectArrayList<RecordSchemaDescription> list2) {
        ObjectArrayList<RecordSchemaDescription> mergedList = new ObjectArrayList<>();
        int i = 0;
        int j = 0;

        while (i < list1.size() && j < list2.size()) {
            RecordSchemaDescription element1 = list1.get(i);
            RecordSchemaDescription element2 = list2.get(j);

            int comparison = element1.compareTo(element2);

            switch (comparison) {
                case -1:    // element1 < element 2
                    addIfNotDuplicate(mergedList, element1);
                    ++i;
                    break;
                case 1:     // element1 > element 2
                    addIfNotDuplicate(mergedList, element2);
                    ++j;
                    break;
                default:    // Both elements are equal
                    RecordSchemaDescription union = process(element1, element2);
                    addIfNotDuplicate(mergedList, union);
                    ++i;
                    ++j;
                    break;
            }
        }

        // Add any remaining elements from list1, checking for duplicates
        while (i < list1.size()) {
            addIfNotDuplicate(mergedList, list1.get(i));
            ++i;
        }

        // Add any remaining elements from list2, checking for duplicates
        while (j < list2.size()) {
            addIfNotDuplicate(mergedList, list2.get(j));
            ++j;
        }

        return mergedList;
    }

    private static void addIfNotDuplicate(ObjectArrayList<RecordSchemaDescription> mergedList,
                                        RecordSchemaDescription element) {
        boolean exists = mergedList.stream()
            .anyMatch(e -> e.getName().equals(element.getName()) && e.getTypes() == element.getTypes());

        if (!exists) {
            mergedList.add(element);
        }
    }
}
