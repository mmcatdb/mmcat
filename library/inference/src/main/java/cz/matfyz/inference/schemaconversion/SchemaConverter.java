package cz.matfyz.inference.schemaconversion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.MappingCreator;
import cz.matfyz.inference.schemaconversion.utils.SchemaConversionUtils;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.identifiers.ObjectIds;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;


/**
 * Class for conversion from RSD to Schema Category
 */
public class SchemaConverter {

    private final RecordSchemaDescription rsd;
    public SchemaCategory sc;
    public AccessTreeNode root;
    public Key rootKey;
    public SchemaConversionUtils SCUtils;

    public SchemaConverter(RecordSchemaDescription rsd, String schemaCatName) {
        this.rsd = rsd;
        this.sc = new SchemaCategory(schemaCatName);
        this.rootKey = new Key(0);
        this.SCUtils = new SchemaConversionUtils();
    }

    /**
     * Method for converting to Schema Category
     * @return SchemaCategory object representing RSD
     */
    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
        /*
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null);
        convertToSchemafromRSD(sc, rsd, rootKey, 1, currentNode);
        SCUtils.addIndexObjecttoArr(sc);
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        //Mapping mapping = mappingCreator.createMapping(sc, "Full_schema_mapping"); //What will this label be?
        Mapping mapping = mappingCreator.createMapping(sc, "yelpbusinesssample"); //What will this label be?
        //System.out.println("root is auxiliary: " + mapping.accessPath().isAuxiliary());
        System.out.println("mapping access path: " + mapping.accessPath());
        return new CategoryMappingPair(sc, mapping);*/
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null, null, null, null, null);
        Map<Integer, Integer> signatureOrder = new HashMap<Integer, Integer>();
        int last = 0;
        convertToSchemafromRSD(rsd, rootKey, 1, currentNode, signatureOrder, last);
        
        //SCUtils.addIndexObjecttoArr(sc);
        AccessTreeNode.assignSignatures(this.root, signatureOrder);
        traverseAndBuild(this.root);
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        //Mapping mapping = mappingCreator.createMapping(sc, "Full_schema_mapping"); //What will this label be?
        Mapping mapping = mappingCreator.createMapping(sc, "yelpbusinesssample"); //What will this label be?
        //System.out.println("root is auxiliary: " + mapping.accessPath().isAuxiliary());
        System.out.println("mapping access path: " + mapping.accessPath());
        return new CategoryMappingPair(sc, mapping);
    }
    
    public SchemaCategory buildSchemaCategory(AccessTreeNode root) {
        traverseAndBuild(root);
        return sc;
    }

    private void traverseAndBuild(AccessTreeNode currentNode) {
        SchemaObject currentObject;
        if (currentNode.getState() == AccessTreeNode.State.Root) {
            ObjectIds ids = ObjectIds.createGenerated();
            SignatureId superId = SignatureId.createEmpty();
            currentObject = new SchemaObject(currentNode.getKey(), "yelpbusinesssample", ids, superId);
        }
        else {
            currentObject = createSchemaObject(currentNode);
            createSchemaMorphism(currentNode, currentObject);
        }
        


        for (AccessTreeNode childNode : currentNode.getChildren()) {
            traverseAndBuild(childNode);
        }
    }

    private SchemaObject createSchemaObject(AccessTreeNode node) {
        ObjectIds ids;
        SignatureId superId = SignatureId.createEmpty();
        if (node.getChildren().isEmpty()) {
            ids = ObjectIds.createValue();
        }
        else { 
            ids = ObjectIds.createGenerated();
        }
        SchemaObject object = new SchemaObject(node.getKey(), node.getName(), ids, superId);  // Assuming you can initialize a SchemaObject this way
        sc.addObject(object);
        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject so) {
        SchemaObject sop = sc.getObject(node.getParentKey()); //make sure the keys are changed already!
        
        SchemaObject dom = sop;
        SchemaObject cod = so;
        
        //need to add the logic with the arrays!!
        //Signature sig = Signature.createBase(node.getSigVal()); //is this signature correct?

      
        Set<SchemaMorphism.Tag> tags = new HashSet<>(); //empty for now, because I dont know what to put there
        SchemaMorphism sm = new SchemaMorphism(node.getSig(), node.getLabel(), node.getMin(), tags, dom, cod);
        sc.addMorphism(sm);
    }

    /**
     * This kinda ugly, could be improved on!!
     * Recursive method for converting RSD to Schema Category
     * @param sc Schema Category which is being built
     * @param rsd RSD which is being processed
     * @param key Key of the current parent RSD
     * @param i int for keeping track of the RSD's children
     */  
    public void convertToSchemafromRSD(RecordSchemaDescription rsdp, Key keyp, int i, AccessTreeNode currentNode, Map<Integer, Integer> signatureOrder, int last) {
        if (sc.allObjects().isEmpty()) { //adding the root
            System.out.println("adding root");
            this.root = new AccessTreeNode(AccessTreeNode.State.Root, "yelpbusinesssample", null, keyp, null, null, null);
        }

        if (!rsdp.getChildren().isEmpty()) {
            System.out.println("rsdp name: "+rsdp.getName());
            if (rsdp.getName().equals("_")) {
                System.out.println("thats right it is _");
                currentNode = this.root;
            }
            else {
                currentNode = this.root.findNodeWithName(rsdp.getName());
            }
            
            if (currentNode != null){
                currentNode.state = AccessTreeNode.State.Complex; //neprepisuje se mi to tady potom do Mappingu??
            }

            for (RecordSchemaDescription rsdch: rsdp.getChildren()) {
                Key keych = SCUtils.createChildKey(keyp, i);
                
                Integer sigVal = SCUtils.createChildSignature(keyp, keych);
                if (!signatureOrder.containsKey(sigVal)) {
                    if (signatureOrder.isEmpty()) {
                        signatureOrder.put(sigVal, last);
                    }
                    else {
                        last += 1;
                        signatureOrder.put(sigVal, sigVal);
                    }
                    
                }
                Min min = SCUtils.findMin(rsdp, rsdch);              

                boolean isArrayp = isTypeArray(rsdp);
                boolean isArraych = isTypeArray(rsdch);



                String label = SCUtils.createLabel(rsdch, isArrayp, isArraych);
    
                // be aware of the label here, once you use is ARRAY!
                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.Simple, rsdch.getName(), sigVal, keych, keyp, label, min);
                currentNode.addChild(child);

                i++;
                convertToSchemafromRSD(rsdch, keych, i, child, signatureOrder, last);
            }
        }
    }

    /*
    public void convertToSchemafromRSD(SchemaCategory sc, RecordSchemaDescription rsdp, Key keyp, int i, AccessTreeNode currentNode) {
        SchemaObject so;
        if (sc.allObjects().isEmpty()) { //adding the root
            Signature s = Signature.createEmpty();
            ObjectIds ids = ObjectIds.createGenerated();
            SignatureId superId = SignatureId.createEmpty();
            so = new SchemaObject(keyp, "yelpbusinesssample", ids, superId);
            //so = new SchemaObject(keyp, rsdp.getName(), ids, superId);
            sc.addObject(so);

            this.root = new AccessTreeNode(AccessTreeNode.State.Root, "yelpbusinesssample", null, keyp, null);
            currentNode = root;
        }
        else {
            so = this.sc.getObject(keyp);
        }

        if (!rsdp.getChildren().isEmpty()) {
            currentNode = root.findNodeWithName(so.label());
            if (currentNode != null){
                currentNode.state = AccessTreeNode.State.Complex;
            }

            for (RecordSchemaDescription rsdch: rsdp.getChildren()) {
                Key keych = SCUtils.createChildKey(keyp, i);
                
                Integer sigVal = SCUtils.createChildSignature(keyp, keych);
                Min min = SCUtils.findMin(rsdp, rsdch);

                ObjectIds ids;
                
                // kdyz nema deti, je to list a chceme mu dat ids value
                if (rsdch.getChildren().isEmpty()) {
                    ids = ObjectIds.createValue();
                }
                else { 
                    ids = ObjectIds.createGenerated();
                }
                SignatureId superId = SignatureId.createEmpty();


                
                SchemaObject soch = new SchemaObject(keych, rsdch.getName(), ids, superId);
                sc.addObject(soch);

                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.Simple, soch.label(), sigVal, keych, keyp);
                currentNode.addChild(child);

                SchemaObject dom = so;
                SchemaObject cod = soch;

                boolean isArrayp = isTypeArray(rsdp);
                boolean isArraych = isTypeArray(rsdch);

                if (isArraych) { // child is an array
                    dom = soch;
                    cod = so;
                }

                String label = SCUtils.createLabel(rsdch, isArrayp, isArraych);
                Set<SchemaMorphism.Tag> tags = new HashSet<>(); //empty for now, because I dont know what to put there
                SchemaMorphism sm = new SchemaMorphism(sig, label, min, tags, dom, cod);
                sc.addMorphism(sm);

                i++;
                convertToSchemafromRSD(sc, rsdch, keych, i, child);
            }
        }
    }*/


    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }
}