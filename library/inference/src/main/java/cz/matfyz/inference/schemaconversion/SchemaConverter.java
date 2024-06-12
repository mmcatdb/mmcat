package cz.matfyz.inference.schemaconversion;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.MappingCreator;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;

/**
 * Class for conversion from RSD to Schema Category
 * RSD -> Access Tree -> SK (should I separate the logic RSD -> Access Tree; Access Tree -> SK)
 */
public class SchemaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaConverter.class);

    private final RecordSchemaDescription rsd;
    public SchemaCategory schemaCategory;
    public String kindName; // TODO: I need this to name the root of my SK and my mapping (probably the same as kind name). Getting it as user inpu rn, but in case of MongoDB, it has to match a collection name! (otherwise cant pull from it)
    public AccessTreeNode root;
    public Key rootKey;
    private final UniqueNumberGenerator keyGenerator;
    private final UniqueNumberGenerator signatureGenerator;

    public SchemaConverter(RecordSchemaDescription rsd, String categoryLabel, String kindName) {
        this.rsd = rsd;
        this.schemaCategory = new SchemaCategory(categoryLabel);
        this.kindName = kindName;
        this.keyGenerator = new UniqueNumberGenerator(0);
        this.signatureGenerator = new UniqueNumberGenerator(0);
        this.rootKey = new Key(keyGenerator.next());
    }

    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
        LOGGER.info("Converting RSD to SchemaCategory...");

        System.out.println(rsd);
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null, null, null, null, null, false);

        rsd.setName("root"); // first change the root name, so that it is more easily recognizable while building the tree
        int i = 1;
        buildAccessTree(rsd, rootKey, i, currentNode);
        System.out.println("Initial access tree: ");
        root.printTree(" ");

        System.out.println("Processing the array nodes...");
        root.transformArrayNodes();

        System.out.println("Building the SchemaCategory...");
        buildSchemaCategory(this.root);

        System.out.println("Morphisms in the final SK: ");
        for (SchemaMorphism m : schemaCategory.allMorphisms()) {
            System.out.println(m.dom() == null ? "Domain is null" : "Domain: " + m.dom().label());
            System.out.println(m.cod() == null ? "Codomain is null" : "Codomain: " + m.cod().label());
            System.out.println();
        }

        System.out.println("Creating mapping...");
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        Mapping mapping = mappingCreator.createMapping(schemaCategory, this.kindName); //What will this label be?

        return new CategoryMappingPair(schemaCategory, mapping);
    }

    public void buildAccessTree(RecordSchemaDescription rsdParent, Key keyParent, int i, AccessTreeNode currentNode) {
        if (this.root == null) { //adding the root
            this.root = new AccessTreeNode(AccessTreeNode.State.ROOT, this.kindName, null, keyParent, null, null, null, false);
        }

        if (!rsdParent.getChildren().isEmpty()) {
            if (rsdParent.getName().equals("root")) {
                currentNode = this.root;
            }
            if (currentNode != null & currentNode.getState() != AccessTreeNode.State.ROOT) {
                currentNode.setState(AccessTreeNode.State.COMPLEX);
            }

            for (RecordSchemaDescription rsdChild: rsdParent.getChildren()) {
                BaseSignature signature = Signature.createBase(signatureGenerator.next());
                Key keyChild = new Key(keyGenerator.next());
                Min min = findMin(rsdParent, rsdChild);
                boolean isArray = isTypeArray(rsdChild);
                String label = createLabel(rsdChild, isArray);

                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.SIMPLE, rsdChild.getName(), signature, keyChild, keyParent, label, min, isArray);
                currentNode.addChild(child);

                buildAccessTree(rsdChild, keyChild, i++, child);
            }
        }
    }

    private void buildSchemaCategory(AccessTreeNode root) {
        traverseAndBuild(root);
    }

    private void traverseAndBuild(AccessTreeNode currentNode) {
        SchemaObject currentObject;
        if (currentNode.getState() == AccessTreeNode.State.ROOT) {
            currentObject = new SchemaObject(currentNode.getKey(), this.kindName, ObjectIds.createGenerated(), SignatureId.createEmpty());
            this.schemaCategory.addObject(currentObject);
        } else {
            System.out.println("Creating SO and SM for node: " + currentNode.getName());
            currentObject = createSchemaObject(currentNode);
            createSchemaMorphism(currentNode, currentObject);
        }

        for (AccessTreeNode childNode : currentNode.getChildren()) {
            traverseAndBuild(childNode);
        }
    }

    private SchemaObject createSchemaObject(AccessTreeNode node) {
        ObjectIds ids = node.getChildren().isEmpty() ? ObjectIds.createValue() : ObjectIds.createGenerated();
        SchemaObject object = new SchemaObject(node.getKey(), node.getName(), ids, SignatureId.createEmpty());
        this.schemaCategory.addObject(object);
        return object;
    }

    private void createSchemaMorphism(AccessTreeNode node, SchemaObject schemaObject) {
        SchemaObject schemaObjectParent = schemaCategory.getObject(node.getParentKey());

        if (schemaObjectParent == null) {
            System.out.println("SK after accessing the parent node");
            System.out.println(schemaCategory.allObjects());
            System.out.println("Error while creating morphism. Domain is null and codomain is " + schemaObject.label());
            System.out.println("Node key: " + node.getKey());
            System.out.println("Parent key: " + node.getParentKey());
        }

        SchemaObject dom = schemaObjectParent;
        SchemaObject cod = schemaObject;

        if (node.getIsArrayType()) { // the morphism turns around when it is an array
            dom = schemaObject;
            cod = schemaObjectParent;
            addIndexObject(schemaObject);
        }

        SchemaMorphism sm = new SchemaMorphism(node.getSignature(), node.getLabel(), node.getMin(), new HashSet<>(), dom, cod);
        this.schemaCategory.addMorphism(sm);
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }

    private Min findMin(RecordSchemaDescription rsdParent, RecordSchemaDescription rsdChild) {
        int shareParentTotal = rsdParent.getShareTotal();
        int shareChildTotal = rsdChild.getShareTotal();

        int shareParentFirst = rsdParent.getShareFirst();
        int shareChildFirst = rsdChild.getShareFirst();

        if ((shareParentTotal > shareChildTotal && shareParentFirst > shareChildFirst) ||
            (shareParentTotal < shareChildTotal && shareParentFirst == shareChildFirst)) {
            return Min.ZERO;
        }
        return Min.ONE;
    }

    public enum Label {
        IDENTIFIER, RELATIONAL;
    }

    /**
     * For getting the right label for the Schema Morphism.
     * For now we label 2 types of morphisms: identification & relational.
     * A morphism is labeled as identification if its codomain is an identificator
     * And it is labeled as relational if its domain is an array type
     */
    private String createLabel(RecordSchemaDescription rsd, boolean isArray) {
        if (isArray) {
            return Label.RELATIONAL.name();
        }
        else {
            // the values for Char are TRUE, FALSE and UNKNOWN
            if (rsd.getUnique() == Char.TRUE) {
                return Label.IDENTIFIER.name();
            }
        }
        // can a relational morphism be also identification? (probably not)
        return null; //when there is no particular label
    }

    /**
     * For creating an extra identification property
     * For each array object add an extra child named "_index".
     */
    private void addIndexObject(SchemaObject schemaObjectParent) {
        SchemaObject schemaObjectChild = new SchemaObject(new Key(keyGenerator.next()), "_index", ObjectIds.createValue(), SignatureId.createEmpty());
        this.schemaCategory.addObject(schemaObjectChild);

        BaseSignature signature = Signature.createBase(signatureGenerator.next());
        SchemaMorphism newMorphism = new SchemaMorphism(signature, null, Min.ZERO, new HashSet<>(), schemaObjectParent, schemaObjectChild);
        this.schemaCategory.addMorphism(newMorphism);
    }
}
