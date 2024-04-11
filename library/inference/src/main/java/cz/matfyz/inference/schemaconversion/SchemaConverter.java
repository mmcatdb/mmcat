package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<Integer> sigVals;

    public SchemaConverter(RecordSchemaDescription rsd, String schemaCatName) {
        this.rsd = rsd;
        this.sc = new SchemaCategory(schemaCatName);
        this.rootKey = new Key(0);
        this.SCUtils = new SchemaConversionUtils();
        this.sigVals = new ArrayList<Integer>();
    }

    /**
     * Method for converting to Schema Category
     * @return SchemaCategory object representing RSD
     */
    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
     
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null, null, null, null, null, false);
        int i = 1;
        buildAccessTree(rsd, rootKey, i, currentNode);
        
        //SCUtils.addIndexObjecttoArr(sc);
        Map<Integer, Integer> mappedSigVals = SCUtils.mapSigVals(this.sigVals);
        this.root = AccessTreeNode.assignSignatures(this.root, mappedSigVals);
        
        //this.root.printTree("");
        
        buildSchemaCategory(this.root);
        //traverseAndBuild(this.root);
        
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        Mapping mapping = mappingCreator.createMapping(sc, "yelpbusinesssample"); //What will this label be?
        
        return new CategoryMappingPair(sc, mapping);
    }
    
    public SchemaCategory buildSchemaCategory(AccessTreeNode root) {
        traverseAndBuild(root);
        return sc;
    }

    private void traverseAndBuild(AccessTreeNode currentNode) {
        SchemaObject currentObject;
        //System.out.println(currentNode.getLabel());
        if (currentNode.getState() == AccessTreeNode.State.Root) {
           // System.out.println("traverseAndBuild() adding root");
            ObjectIds ids = ObjectIds.createGenerated();
            SignatureId superId = SignatureId.createEmpty();
            currentObject = new SchemaObject(currentNode.getKey(), "yelpbusinesssample", ids, superId);
            this.sc.addObject(currentObject);
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
        else { ids = ObjectIds.createGenerated(); }
        
        SchemaObject object = new SchemaObject(node.getKey(), node.getName(), ids, superId); 
        this.sc.addObject(object);
        
        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject so) {
        SchemaObject sop = sc.getObject(node.getParentKey()); 
        
        SchemaObject dom = sop;
        SchemaObject cod = so;
        
        if (node.isArrayType) {
            dom = so;
            cod = sop;
        }
      
        Set<SchemaMorphism.Tag> tags = new HashSet<>(); //empty for now, because I dont know what to put there
        SchemaMorphism sm = new SchemaMorphism(node.getSig(), node.getLabel(), node.getMin(), tags, dom, cod);
        sc.addMorphism(sm);
    }

    /**
     * Recursive method for converting RSD to Schema Category
     * @param sc Schema Category which is being built
     * @param rsd RSD which is being processed
     * @param key Key of the current parent RSD
     * @param i int for keeping track of the RSD's children
     */  
    public void buildAccessTree(RecordSchemaDescription rsdp, Key keyp, int i, AccessTreeNode currentNode) {
        if (this.root == null) { //adding the root
            //System.out.println("adding root");
            this.root = new AccessTreeNode(AccessTreeNode.State.Root, "yelpbusinesssample", null, keyp, null, null, null, false);
        }

        if (!rsdp.getChildren().isEmpty()) {
            //System.out.println("rsdp name: "+rsdp.getName());
            if (rsdp.getName().equals("_")) { // do a different condition here
                //System.out.println("thats right it is _");
                currentNode = this.root;
            }
            else {
                currentNode = this.root.findNodeWithName(rsdp.getName());
            }
            
            if (currentNode != null & currentNode.state != AccessTreeNode.State.Root){
                currentNode.state = AccessTreeNode.State.Complex; 
            }

            for (RecordSchemaDescription rsdch: rsdp.getChildren()) {
                Key keych = SCUtils.createChildKey(keyp, i);
                
                Integer sigVal = SCUtils.createChildSignature(keyp, keych);
                //System.out.println("sigVal: " + sigVal);
                this.sigVals.add(sigVal);
             
                Min min = SCUtils.findMin(rsdp, rsdch);              

                boolean isArrayp = isTypeArray(rsdp);
                boolean isArraych = isTypeArray(rsdch);  


                String label = SCUtils.createLabel(rsdch, isArrayp, isArraych);
    
                // be aware of the label here, once you use is ARRAY!
                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.Simple, rsdch.getName(), sigVal, keych, keyp, label, min, isArraych);
                currentNode.addChild(child);


                i++;
                buildAccessTree(rsdch, keych, i, child);
            }
        }
    }

 
    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }
}