/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.rba.functions;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class DefaultLocalReductionFunction implements AbstractRSDsReductionFunction {

    @Override
    public RecordSchemaDescription call(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) throws Exception {
        return process(rsd1, rsd2);
    }

    private RecordSchemaDescription process(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName(rsd1.getName());
//        Share newShare = new Share(0, t1.getShare().getFirst())
        result.setShare(rsd1.getShare().add(rsd2.getShare()));

        result.setUnique(Char.min(rsd1.getUnique(), rsd2.getUnique()));
        result.setId(Char.min(rsd1.getId(), rsd2.getId()));

        result.setModels(rsd1.getModels() | rsd2.getModels());
        result.setTypes(rsd1.getTypes() | rsd2.getTypes());

        result.setChildren(mergeOrderedListsRemoveDuplicates(rsd1.getChildren(), rsd2.getChildren()));

//        System.out.println("VYSLEDEK: " + result);
        return result;
    }

    public List<RecordSchemaDescription> mergeOrderedListsRemoveDuplicates(List<RecordSchemaDescription> list1, List<RecordSchemaDescription> list2) {
        List<RecordSchemaDescription> mergedList = new ArrayList<>();
        int i = 0, j = 0;
//        RecordSchemaDescription lastMergedElement = null;

        while (i < list1.size() && j < list2.size()) {
            RecordSchemaDescription element1 = list1.get(i);
            RecordSchemaDescription element2 = list2.get(j);

            int comparison = element1.compareTo(element2);

            switch (comparison) {
                case -1:    // element1 < element 2
                    mergedList.add(element1);
                    ++i;
                    break;
                case 1:    // element1 > element 2
                    mergedList.add(element2);
//                    }
                    ++j;
                    break;
                default:
                    // Both elements are equal
                    RecordSchemaDescription union = process(element1, element2);
                    mergedList.add(union);
                    ++i;
                    ++j;
                    break;
            }
        }

        // Add any remaining elements from list1 (if any)
        while (i < list1.size()) {
            RecordSchemaDescription element = list1.get(i);
            mergedList.add(element);
            ++i;
        }

        // Add any remaining elements from list2 (if any)
        while (j < list2.size()) {
            RecordSchemaDescription element = list2.get(j);
            mergedList.add(element);
//            }
            ++j;
        }

        return mergedList;
    }

}
