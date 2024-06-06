package cz.matfyz.inference.schemaconversion;

import java.util.HashSet;
import java.util.Set;

import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.MappingCreator;
import cz.matfyz.inference.schemaconversion.utils.SchemaConversionUtils;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;

/**
 * Class for conversion from RSD to Schema Category
 */
public class SchemaConverter {

    private final RecordSchemaDescription rsd;
    public SchemaCategory sc;
    public String kindName; // TODO: I need this to name the root of my SK and my mapping (probably the same as kind name). Getting it as user inpu rn, but in case of MongoDB, it has to match a collection name! (otherwise cant pull from it)
    public AccessTreeNode root;
    public Key rootKey;

    //
    public SchemaConverter(RecordSchemaDescription rsd, String categoryLabel, String kindName) {
        this.rsd = rsd;
        this.sc = new SchemaCategory(categoryLabel);
        this.kindName = kindName;
        this.rootKey = new Key(0);
    }

    /**
     * Method for converting to Schema Category
     * @return SchemaCategory object representing RSD
     */
    public CategoryMappingPair convertToSchemaCategoryAndMapping() {

        System.out.println(rsd);
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null, null, null, null, null, false);
        int i = 1;

        System.out.println("Building the Access Tree...");
        rsd.setName("root"); // first change the root name, so that it is more easily recognizable while building the tree
        buildAccessTree(rsd, rootKey, i, currentNode);
        System.out.println((!root.areKeysUnique() ? "Keys in the tree are not unique." : "Keys in the tree are all unique.") + "\n");
        System.out.println("Initial access tree: ");
        root.printTree(" ");

        System.out.println("Processing the array nodes...");
        root.transformArrayNodes();
        System.out.println("Access tree after processing array nodes: ");
        root.printTree(" ");

        System.out.println("Building the SK...");
        buildSchemaCategory(this.root);

        System.out.println("Morphisms in the final SK: ");
        for (SchemaMorphism m : sc.allMorphisms()) {
            System.out.println(m.dom() == null ? "Domain is null" : "Domain: " + m.dom().label());
            System.out.println(m.cod() == null ? "Codomain is null" : "Codomain: " + m.cod().label());
            System.out.println();
        }
      
        System.out.println("Creating mapping...");
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        Mapping mapping = mappingCreator.createMapping(sc, this.kindName); //What will this label be?

        return new CategoryMappingPair(sc, mapping);
    }

    public SchemaCategory buildSchemaCategory(AccessTreeNode root) {
        traverseAndBuild(root);
        return sc;
    }

    private void traverseAndBuild(AccessTreeNode currentNode) {
        SchemaObject currentObject;
        boolean skipArrayRoot = false;
        //System.out.println(currentNode.getLabel());
        if (currentNode.getState() == AccessTreeNode.State.Root) {
           // System.out.println("traverseAndBuild() adding root");
            ObjectIds ids = ObjectIds.createGenerated();
            SignatureId superId = SignatureId.createEmpty();
            currentObject = new SchemaObject(currentNode.getKey(), this.kindName, ids, superId);
            this.sc.addObject(currentObject);
        }
        else {
            System.out.println("Creating SO and SM for node: " + currentNode.name);
            currentObject = createSchemaObject(currentNode);
            createSchemaMorphism(currentNode, currentObject);
        }

        for (AccessTreeNode childNode : currentNode.getChildren()) {
            if (skipArrayRoot) {
                childNode.setParentKey(currentNode.getParentKey());
            }
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

        SchemaObject object = new SchemaObject(node.getKey(), node.getName(), ids, superId);
        this.sc.addObject(object);

        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject so) {
        
        SchemaObject sop = sc.getObject(node.getParentKey());

        if (sop == null) {
            System.out.println("SK after accessing the parent node");
            System.out.println(sc.allObjects());
            System.out.println("Error while creating morphism. Domain is null and codomain is " + so.label());
            System.out.println("Node key: " + node.getKey());
            System.out.println("Parent key: " + node.getParentKey());
        }
        
        SchemaObject dom = sop;
        SchemaObject cod = so;
        
        if (node.isArrayType) { // the morphism turns around when it is an array
            dom = so;
            cod = sop;
        }

        Set<SchemaMorphism.Tag> tags = new HashSet<>(); //empty for now
        SchemaMorphism sm = new SchemaMorphism(node.getSig(), node.getLabel(), node.getMin(), tags, dom, cod);
        sc.addMorphism(sm);
    }

    // TODO better description
    private int nextKey = 1;
    private int nextSignature = 0;

    /**
     * Recursive method for converting RSD to Schema Category
     * @param sc Schema Category which is being built
     * @param rsd RSD which is being processed
     * @param key Key of the current parent RSD
     * @param i int for keeping track of the RSD's children
     */
    public void buildAccessTree(RecordSchemaDescription parentRsd, Key keyp, int i, AccessTreeNode currentNode) {
        if (this.root == null) { //adding the root
            //System.out.println("adding root");
            this.root = new AccessTreeNode(AccessTreeNode.State.Root, this.kindName, null, keyp, null, null, null, false);
        }

        if (!parentRsd.getChildren().isEmpty()) {
            //System.out.println("rsdp name: "+rsdp.getName());
            if (parentRsd.getName().equals("root")) { // do a different condition here maybe?
                currentNode = this.root;
            }
            if (currentNode != null & currentNode.state != AccessTreeNode.State.Root) {
                currentNode.state = AccessTreeNode.State.Complex;
            }

            for (RecordSchemaDescription childRsd: parentRsd.getChildren()) {
                System.out.println("Building access node for: " + childRsd.getName());
                
                Key keyChild = new Key(nextKey++);
                
                Min min = SchemaConversionUtils.findMin(parentRsd, childRsd);
                
                boolean isArray = isTypeArray(childRsd);
                BaseSignature signature = Signature.createBase(nextSignature++);

                String label = SchemaConversionUtils.createLabel(childRsd, isArray);

                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.Simple, childRsd.getName(), signature, keyChild, keyp, label, min, isArray);
                currentNode.addChild(child);

                i++;
                buildAccessTree(childRsd, keyChild, i, child);
            }
        }
    }


    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }
}
