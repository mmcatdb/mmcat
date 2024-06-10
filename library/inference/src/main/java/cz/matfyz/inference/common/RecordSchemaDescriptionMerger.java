package cz.matfyz.inference.common;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;

public class RecordSchemaDescriptionMerger {

    /***
     * Merging based on the primary key
     * Add the other rsds as children to the root of the primary key rsd
     * But merge the rsd which would have the primary key property as a child - can I do that? That would mean my tree would not be a nice tree, but will have more roots
     */
    public static RecordSchemaDescription mergeBasedOnPrimaryKey(Map<String, RecordSchemaDescription> rsds, PrimaryKeyCandidate candidate) {
        System.out.println("Merging based on primary key...");
        String[] names = candidate.getHierarchicalName().split("/");  // the hierarchical name can have more than 2 parts : dfs/cds/cds/cd
        String pkCollectionName = names[0]; 
        String primaryKey = names[names.length - 1];

        System.out.println("pkCollectionName: " + pkCollectionName);
        System.out.println("pkProperty: " + names[names.length - 2]);
        System.out.println("Primary key: " + primaryKey);

        RecordSchemaDescription prePkRSD = rsds.get(pkCollectionName);
        cleanRSD(prePkRSD, pkCollectionName, new String[] {"_id"});

        RecordSchemaDescription pkRSD = getRSDAccordingToNames(prePkRSD, names);

        for (String collectionName : rsds.keySet()) {
            if (!collectionName.equals(pkCollectionName)) {
                RecordSchemaDescription currentRSD = rsds.get(collectionName);
                cleanRSD(currentRSD, collectionName, new String[] {"_id", primaryKey});         
                pkRSD.addChildren(currentRSD);      
            }
        }
        return pkRSD;
    }

    public static Map<String, RecordSchemaDescription> mergeBasedOnReference(Map<String, RecordSchemaDescription> rsds, ReferenceCandidate candidate) {
        // not all reference candidates might be valid
        // valid: ref property is present in the other rsd(s)   
        
        String[] referredNames = candidate.getReferred().split("/"); 
        String referencingCollectionName = referredNames[0]; 
        String referredProperty = referredNames[referredNames.length - 1]; 

        // Example of refCandidates
        // refCandidates=[ReferenceCandidate{type=reference, referencing= reviews/_id, referred=reviews/App_Name, weak=false, selected=false}, 
        // ReferenceCandidate{type=reference, referencing= reviews/_id, referred=reviews/Sentiment, weak=false, selected=false}], redCandidates=[]}

        System.out.println("Merging based on references...");
        System.out.println("referencing collection name: " + referencingCollectionName);
        System.out.println("refered property: " + referredProperty);

        // Assumptions: 
        // 1) in the reffered rsd we assume that the property is the child of the root --> else we couldn't create an rsd with one root
        // 2) assuming there is only one other rsd reffered (naive assumption); (or the refered rsd is present in just one other rsd)

        RecordSchemaDescription refRSD = rsds.get(referencingCollectionName); // TODO: this might be in a more complicated form dsdhj,dhs

        if (refRSD != null) {// it has not been merged yet
            for (String collectionName : rsds.keySet()) {
                if (!collectionName.equals(referencingCollectionName)) {
                    // check if rsd has the referencing collection as a child
                    RecordSchemaDescription rsd = rsds.get(collectionName);
                    System.out.println("RSD before reference merging: " + rsd);

                    if (rsd.hasParentWithChildName(referencingCollectionName)) {
                        System.out.println("Found an rsd where I reference");
                                                
                        cleanRSD(refRSD, referencingCollectionName, new String[]{"_id", referredProperty});          

                        rsd.addChildrenIfNameMatches(refRSD);

                        rsds.remove(collectionName);
                        rsds.remove(referencingCollectionName);
                        rsds.put(collectionName + "," + referencingCollectionName, rsd);   

                        System.out.println("RSD after reference merging: " + rsd);
                        
                        break;  
                    }                            
                }
            }   
        }        
        return rsds;
    }

    public static RecordSchemaDescription getRSDAccordingToNames(RecordSchemaDescription rsd, String[] names) {
        for (int i = 1; i < names.length; ++i) {
            for (RecordSchemaDescription child : rsd.getChildren()) {
                //System.out.println("in getRSDAccNames, child name: " + child.getName());
                if (child.getName().equals(names[names.length - 1])) // child is the pk
                    //return child;
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
        System.out.println("cleaning RSD: " + newName);
        // get rid of "_id", but this only applies to mongoDB, so be aware! 
        rsd.setName(newName); // changing from "_" to the collection name    
        for (String propertyName : propertyNamesToDelete) 
            rsd.removeChildByName(propertyName);
    }

}
