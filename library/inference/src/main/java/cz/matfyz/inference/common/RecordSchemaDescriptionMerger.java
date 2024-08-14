package cz.matfyz.inference.common;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordSchemaDescriptionMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordSchemaDescriptionMerger.class);

    private RecordSchemaDescriptionMerger() {
        throw new UnsupportedOperationException("Utility class RecordSchemaDescriptionMerger.");
    }

    /**
     * Merging based on the primary key
     * TODO: delete this class later
     */
    public static RecordSchemaDescription mergeBasedOnPrimaryKey(Map<String, RecordSchemaDescription> rsds, PrimaryKeyCandidate candidate) {
        LOGGER.info("Merging based on primary key...");
        System.out.println("Merging based on primary key...");
        String[] names = candidate.hierarchicalName().split("/");  // the hierarchical name can have more than 2 parts : dfs/cds/cds/cd
        String primaryKeyCollectionName = names[0];
        String primaryKey = names[names.length - 1];

        System.out.println("PrimaryKeyCollectionName: " + primaryKeyCollectionName);
        System.out.println("PrimaryKeyProperty: " + names[names.length - 2]);
        System.out.println("Primarykey: " + primaryKey);

        RecordSchemaDescription primaryKeyRSD = cloneRSD(rsds.get(primaryKeyCollectionName));
        cleanRSD(primaryKeyRSD, primaryKeyCollectionName, new String[] {"_id"});

        RecordSchemaDescription primaryKeyRSDToMerge = getRSDAccordingToNames(primaryKeyRSD, names);

        for (String collectionName : rsds.keySet()) {
            if (!collectionName.equals(primaryKeyCollectionName)) {
                RecordSchemaDescription currentRSD = cloneRSD(rsds.get(collectionName));
                cleanRSD(currentRSD, collectionName, new String[] {"_id", primaryKey});
                primaryKeyRSDToMerge.addChildren(currentRSD);
            }
        }
        return primaryKeyRSDToMerge;
    }

    public static Map<String, RecordSchemaDescription> mergeBasedOnReference(Map<String, RecordSchemaDescription> rsds, ReferenceCandidate candidate) {
        // not all reference candidates might be valid
        // valid: ref property is present in the other rsd(s)
        LOGGER.info("Merging based on references...");
        System.out.println("Merging based on references...");
        String[] names = candidate.referred().split("/");
        String referencingCollectionName = names[0];
        String referredProperty = names[names.length - 1];

        System.out.println("Referencing collection name: " + referencingCollectionName);
        System.out.println("Refered property: " + referredProperty);

        Map<String, RecordSchemaDescription> newRSDs = new HashMap<>(rsds);

        // Assumptions:
        // 1) in the reffered rsd we assume that the property is the child of the root --> else we couldn't create an rsd with one root
        // 2) assuming there is only one other rsd reffered (naive assumption); (or the refered rsd is present in just one other rsd)

        RecordSchemaDescription referenceRSD = rsds.get(referencingCollectionName);

        if (referenceRSD != null) { // it has not been merged yet
            RecordSchemaDescription referenceRSDToMerge = cloneRSD(referenceRSD);
            for (Map.Entry<String, RecordSchemaDescription> entry : rsds.entrySet()) {
                String collectionName = entry.getKey();
                RecordSchemaDescription rsd = entry.getValue();
                if (!collectionName.equals(referencingCollectionName)) {
                    RecordSchemaDescription currentRSD = cloneRSD(rsd);
                    if (currentRSD.hasParentWithChildName(referencingCollectionName)) {
                        cleanRSD(referenceRSDToMerge, referencingCollectionName, new String[]{"_id", referredProperty});

                        currentRSD.addChildrenIfNameMatches(referenceRSDToMerge);

                        newRSDs.remove(collectionName);
                        newRSDs.remove(referencingCollectionName);
                        newRSDs.put(collectionName + "," + referencingCollectionName, currentRSD);

                        break;
                    }
                }
            }
        }
        return newRSDs;
    }

    private static RecordSchemaDescription getRSDAccordingToNames(RecordSchemaDescription rsd, String[] names) {
        for (int i = 1; i < names.length; ++i) {
            for (RecordSchemaDescription child : rsd.getChildren()) {
                if (child.getName().equals(names[names.length - 1]))
                    return rsd;
                if (child.getName().equals(names[i])) {
                    rsd = child;
                    break;
                }
            }
        }
        return rsd;
    }

    private static void cleanRSD(RecordSchemaDescription rsd, String newName, String[] propertyNamesToDelete) {
        rsd.setName(newName); // changing from "_" to the collection name
        for (String propertyName : propertyNamesToDelete)
            rsd.removeChildByName(propertyName);
    }

    private static RecordSchemaDescription cloneRSD(RecordSchemaDescription rsd) {
        return new RecordSchemaDescription(rsd);
    }

}
