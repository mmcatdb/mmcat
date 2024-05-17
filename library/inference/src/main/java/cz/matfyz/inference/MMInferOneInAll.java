package cz.matfyz.inference;

import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
import cz.matfyz.inference.algorithms.pba.PropertyBasedAlgorithm;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalSeqFunction;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

//// from the old inference version ///

import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;

//// for the new inference version ////
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MMInferOneInAll {

    private AbstractInferenceWrapper wrapper;
    private String kindName;
    private String categoryLabel;

    public MMInferOneInAll input(AbstractInferenceWrapper wrapper, String kindName, String categoryLabel) {
        this.wrapper = wrapper;
        this.kindName = kindName;
        this.categoryLabel = categoryLabel;

        return this;
    }

    public CategoryMappingPair run() {
        try {
            return innerRun();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private CategoryMappingPair innerRun() throws Exception {
        System.out.println("RESULT_TIME ----- ----- ----- ----- -----");

        Map<String, AbstractInferenceWrapper> wrappers = prepareWrappers(wrapper);

        Map<String, RecordSchemaDescription> rsds = wrappers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> MMInferOneInAll.executeRBA(entry.getValue(), true)));
        // or this is the written out version
        /*
        Map<String, RecordSchemaDescription> rsds = new HashMap<>();
        for (String collectionName : wrappers.keySet()) {
            RecordSchemaDescription r = MMInferOneInAll.executeRBA(wrappers.get(collectionName), true);
            rsds.put(collectionName, r);
        } */

        // TODO: review the merging, though
        RecordSchemaDescription rsd = mergeRecordSchemaDescriptions(rsds);

        SchemaConverter scon = new SchemaConverter(rsd, categoryLabel, kindName);
        return scon.convertToSchemaCategoryAndMapping();
    }

    private static Map<String, AbstractInferenceWrapper> prepareWrappers(AbstractInferenceWrapper inputWrapper) throws IllegalArgumentException {
        Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();

        inputWrapper.getKindNames().forEach(kindName -> {
            final AbstractInferenceWrapper copy = inputWrapper.copy();
            copy.kindName = kindName;

            wrappers.put(kindName, copy);
        });

        return wrappers;
    }

    private static RecordSchemaDescription executeRBA(AbstractInferenceWrapper wrapper, boolean printSchema) {
        RecordBasedAlgorithm rba = new RecordBasedAlgorithm();

        AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();

        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = rba.process(wrapper, merge);
        long end = System.currentTimeMillis();

        if (printSchema) {
            System.out.print("RESULT_RECORD_BA: ");
            System.out.println(rsd);
        }

        System.out.println("RESULT_TIME_RECORD_BA TOTAL: " + (end - start) + "ms");

        return rsd;
    }

    private static void executePBA(AbstractInferenceWrapper wrapper, boolean printSchema) {
        PropertyBasedAlgorithm pba = new PropertyBasedAlgorithm();

        DefaultLocalSeqFunction seqFunction = new DefaultLocalSeqFunction();
        DefaultLocalCombFunction combFunction = new DefaultLocalCombFunction();

        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = pba.process(wrapper, seqFunction, combFunction);

//        RecordSchemaDescription rsd2 = finalize.process();        // TODO: SLOUCIT DOHROMADY!
        long end = System.currentTimeMillis();

        if (printSchema) {
            System.out.print("RESULT_PROPERTY_BA: ");
            System.out.println(rsd == null ? "NULL" : rsd);
        }

        System.out.println("RESULT_TIME_PROPERTY_BA TOTAL: " + (end - start) + "ms");
    }


    public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
        // TODO
        BloomFilter.setParams(10000, new BasicHashFunction());
        StartingEndingFilter.setParams(10000);
        CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
        Candidates candidates = candidateMiner.process(wrapper, kinds);

        return candidates;
    }

    private static RecordSchemaDescription mergeRecordSchemaDescriptions(Map<String, RecordSchemaDescription> rsds) {
        if (rsds.size() == 1) {
            return rsds.values().iterator().next();
        }
        return mergeByName(rsds);
    }

    private static RecordSchemaDescription mergeByName(Map<String, RecordSchemaDescription> rsds) {
        RecordSchemaDescription complexRSD = null;

        // Traverse through all entries in rsds
        for (RecordSchemaDescription rsd : rsds.values()) {
            if (mergeChildren(rsd, rsds)) {
                complexRSD = rsd;
            }
        }
        return complexRSD;
    }

    /**
     * Recursively replace children of the given rsd if their names match any key in rsds.
     */
    private static boolean mergeChildren(RecordSchemaDescription rsd, Map<String, RecordSchemaDescription> rsds) {
        // System.out.println("mergeChildren, rsd name: " + rsd.getName());
        if (rsd.getChildren() == null || rsd.getChildren().isEmpty())
            return false;

        ObjectArrayList<RecordSchemaDescription> newChildren = new ObjectArrayList<>();

        for (RecordSchemaDescription child : rsd.getChildren()) {
            if (rsds.containsKey(child.getName())) {
                System.out.println("replacing child: " + child.getName());
                // replace the child with the corresponding RSD from rsds
                RecordSchemaDescription replacementRSD = rsds.get(child.getName());
                replacementRSD.setName(child.getName());
                mergeChildren(replacementRSD, rsds); // ensure to merge its children too
                newChildren.add(replacementRSD);
            } else {
                mergeChildren(child, rsds);
                newChildren.add(child);
            }
        }

        // update children of current rsd
        if (newChildren != rsd.getChildren()) {
            rsd.setChildren(newChildren);
            return true;
        }

        return false;
    }

    /// Follow alternative merging methods ///
    /*
    public static RecordSchemaDescription mergeByNames(Map<String, RecordSchemaDescription> rsds) {
        RecordSchemaDescription complexRSD = new RecordSchemaDescription();

        for (String collectionName : rsds.keySet()) {
            RecordSchemaDescription rsd = rsds.get(collectionName);
            //rsdToAdd.setName(collectionName);
            ObjectArrayList<RecordSchemaDescription> children = complexRSD.getChildren();
            //children.add(rsdToAdd);
        }
        return complexRSD;
    } */


    /**
     * WIP
     * Merges two RSDs into one, creating a new root
     */
    /*
    public static RecordSchemaDescription mergeToComplex(Map<String, RecordSchemaDescription> rsds) {
        RecordSchemaDescription complexRSD = new RecordSchemaDescription();
        complexRSD.setName("_");
        // changing the root name "_" to collectionName
        // be aware that I am not setting any additional fields, like shareFirst or shareTotal for the root
        for (String collectionName : rsds.keySet()) {
            RecordSchemaDescription rsdToAdd = rsds.get(collectionName);
            rsdToAdd.setName(collectionName);
            ObjectArrayList<RecordSchemaDescription> children = complexRSD.getChildren();
            children.add(rsdToAdd);
        }
        return complexRSD;
    } */

    /**
     * WIP
     * Merges 2 rsds w/o any condition
     */
    /*
    public static RecordSchemaDescription mergeRSDs(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        if (rsd1 == null || rsd2 == null) {
            System.out.println("returning just one");
            return rsd1 != null ? rsd1 : rsd2;
        }

        // RecordSchemaDescription mergedRSD = new RecordSchemaDescription(rsd1.name);
        RecordSchemaDescription mergedRSD = new RecordSchemaDescription();
        mergedRSD.setName(rsd1.getName());
        Map<String, RecordSchemaDescription> childMap = new HashMap<>();

        // Process all children from rsd1
        for (RecordSchemaDescription child : rsd1.getChildren()) {
            RecordSchemaDescription ch = new RecordSchemaDescription();
            ch.setName(child.getName());
            childMap.put(child.getName(), ch);
        }
        for (RecordSchemaDescription child : rsd1.getChildren()) {
            RecordSchemaDescription mergedChild = childMap.get(child.getName());
            mergeChildProperties(mergedChild, child);
        }
        for (RecordSchemaDescription child : rsd2.getChildren()) {
            if (childMap.containsKey(child.getName())) {
                RecordSchemaDescription mergedChild = childMap.get(child.getName());
                mergeChildProperties(mergedChild, child);
                mergedChild = mergeRSDs(mergedChild, child);
            } else {
                RecordSchemaDescription ch = new RecordSchemaDescription();
                ch.setName(child.getName());
                childMap.put(child.getName(), ch);
                mergeChildProperties(childMap.get(child.getName()), child);
            }
        }
        ObjectArrayList<RecordSchemaDescription> children = mergedRSD.getChildren();
        for (RecordSchemaDescription ch : childMap.values()) {
            children.add(ch);
        }
        mergedRSD.setChildren(children);
        return mergedRSD;
    }*/

    // helper method to merge properties of two RSDs
    /*
    private static void mergeChildProperties(RecordSchemaDescription target, RecordSchemaDescription source) {
        target.setUnique(target.getUnique() + source.getUnique());
        target.setShareTotal(target.getShareTotal() + source.getShareTotal());
        target.setShareFirst(target.getShareFirst() + source.getShareFirst());
    }*/

}
