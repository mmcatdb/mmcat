package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.identifiers.ObjectIds;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.core.rsd.Type;


/**
 * Class for conversion from RSD to Schema Category
 */
public class SchemaConverter {

    /**
     * To label the morphisms
     */
    private enum Label {
        IDENTIFIER, RELATIONAL;
    }


    private final RecordSchemaDescription rsd;
    public SchemaCategory sc;// = new SchemaCategory("Schema from RSD");
    public AccessTreeNode root;
    public Key rootKey = new Key(0);

    public SchemaConverter(RecordSchemaDescription rsd, String schemaCatName) {
        this.rsd = rsd;
        this.sc = new SchemaCategory(schemaCatName);
    }

    /**
     * Helper method for converting to Schema Category
     * @return SchemaCategory object representing RSD
     */
    public CategoryMappingPair convertToSchemaCategoryAndMapping() {
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null);
        convertToSchemafromRSD(this.sc, this.rsd, this.rootKey, 1, currentNode);
        addIndexObjecttoArr(this.sc);
        //root.printTree("");
        Mapping mapping = createMapping(this.sc, "RSD_to_SC"); //What will this label be?
        return new CategoryMappingPair(this.sc, mapping);
    }

    /**
     * This kinda ugly, could be improved on!!
     * Recursive method for converting RSD to Schema Category
     * @param sc Schema Category which is being built
     * @param rsd RSD which is being processed
     * @param key Key of the current parent RSD
     * @param i int for keeping track of the RSD's children
     */
    public void convertToSchemafromRSD(SchemaCategory sc, RecordSchemaDescription rsdp, Key keyp, int i, AccessTreeNode currentNode) {
        SchemaObject so;
        if (sc.allObjects().isEmpty()) { //adding the root
            Signature s = Signature.createBase(1);

            Set<Signature> ss = new HashSet<>();
            ss.add(s);

            ObjectIds objectIds = new ObjectIds(s);

            SignatureId sId = new SignatureId(ss);
            so = new SchemaObject(keyp, rsdp.getName(), objectIds, sId);
            sc.addObject(so);

            this.root = new AccessTreeNode(AccessTreeNode.State.C, rsdp.getName(), s);
            currentNode = this.root;
        }
        else {
            so = this.sc.getObject(keyp);
        }

        if (!rsdp.getChildren().isEmpty()) {
            currentNode = root.findNodeWithName(so.label());
            if (currentNode != null){
                currentNode.state = AccessTreeNode.State.C;
            }

            for (RecordSchemaDescription rsdch: rsdp.getChildren()) {
                // Add child object
                Key keych = createChildKey(keyp, i);

                // Add morphism
                Signature sig = createChildSignature(keyp, keych);
                Min min = findMin(rsdp, rsdch);

                Set<Signature> sigSet = new HashSet<>();
                sigSet.add(sig);

                ObjectIds objectIds = new ObjectIds(sig);

                SignatureId superId = new SignatureId(sigSet);
                SchemaObject soch = new SchemaObject(keych, rsdch.getName(), objectIds, superId);
                sc.addObject(soch);

                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.S, soch.label(), sig);
                currentNode.addChild(child);

                SchemaObject dom = so;
                SchemaObject cod = soch;

                boolean isArrayp = isTypeArray(rsdp);
                boolean isArraych = isTypeArray(rsdch);

                if (isArraych) { // child is an array
                    dom = soch;
                    cod = so;
                }

                String label = createLabel(rsdch, isArrayp, isArraych);
                Set<SchemaMorphism.Tag> tags = new HashSet<>(); //empty for now, because I dont know what to put there
                SchemaMorphism sm = new SchemaMorphism(sig, label, min, tags, dom, cod);
                sc.addMorphism(sm);

                i++;
                convertToSchemafromRSD(sc, rsdch, keych, i, child);
            }
        }
    }

    /**
     * Helper method for creating the child key
     * @param keyParent
     * @param i
     * @return Child's key
     */
    private Key createChildKey(Key keyp, int i) {
        int keyvalp = keyp.getValue();
        int keyvalch = Objects.hash(keyvalp, i);
        Key keych = new Key(keyvalch);
        return keych;
    }

    /**
     * Helper method for creating signature for the parent-child morphism
     * @param keyParent
     * @param keyChild
     * @return Signature
     */
    private Signature createChildSignature(Key keyp, Key keych) {
        int sigval = Objects.hash(keyp.getValue(), keych.getValue());
        Signature sig = Signature.createBase(sigval);
        return sig;
    }

    /**
     * Finding the cardinality between rsds
     * @param rsdp
     * @param rsdch
     * @return
     */
    private Min findMin(RecordSchemaDescription rsdp, RecordSchemaDescription rsdch) {
        Share sharep = rsdp.getShare();
        Share sharech = rsdch.getShare();

        Min min = Min.ONE;

        if (sharep.getTotal() == sharech.getTotal() && sharep.getFirst() == sharech.getFirst()) {
            min = Min.ONE;
        } else if (sharep.getTotal() > sharech.getTotal() && sharep.getFirst() > sharech.getFirst()) {
            min = Min.ZERO;
        } else if (sharep.getTotal() < sharech.getTotal()) {
            if (sharep.getFirst() < sharech.getFirst()) {
                min = Min.ONE;
            } else if (sharep.getFirst() == sharech.getFirst()) {
                min = Min.ZERO;
            }
        }
        return min;
    }

    private boolean isTypeArray(RecordSchemaDescription rsd) {
        //if the parent is an array, then switch dom and cod
        if ((rsd.getTypes() & Type.ARRAY) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Get the right label for the Schema Morphism.
     * For now we label 2 types of morphisms: identification & relational.
     * A morphism is labeled as identification if its codomain is an identificator
     * And it is labeled as relational if its domain is an array type
     * @return
     */
    private String createLabel(RecordSchemaDescription rsdch, boolean isArrayp, boolean isArraych) {

        if (isArraych || isArrayp) { // meaning the parent is an array (works for now, might not work later!)
            return Label.RELATIONAL.name();
        }
        else {
            // the values for Char are TRUE, FALSE and UNKNOWN
            if (rsdch.getUnique() == Char.TRUE) {
                return Label.IDENTIFIER.name();
            }
        }
        // can a relational morphism be also identification? (now I assume that not)
        return null; //when there is no particular label
    }

    /**
     * For each array object add an extra child named "_index".
     * This is done once the whole Schema Category gets created.
     * @param sc
     * @return
     */
    private SchemaCategory addIndexObjecttoArr(SchemaCategory sc) {
        for (SchemaMorphism s: sc.allMorphisms()) {
            if (s.label == Label.RELATIONAL.name()) { // if the object is an array
                SchemaObject sop = s.dom();
                Key keyp = sop.key();
                Key keych = createChildKey(keyp, 0); // probs could just put zero here, but have to check later!!
                SchemaObject soch = new SchemaObject(keych, "_index", null, null);
                sc.addObject(soch);

                Signature sig = createChildSignature(keyp, keych);
                Set<SchemaMorphism.Tag> tags = new HashSet<>();
                SchemaMorphism sm = new SchemaMorphism(sig, null, Min.ZERO, tags, sop, soch); //is this min right?
                sc.addMorphism(sm);
            }
        }
        return sc;
    }

    // should I refactor? Make a separate class for this? Something that woudl have fields for both
    // the SK and the mapping? - ONCE the logic for this is done, then do it!!
    /**
     * Method for creating Mapping for the SchemaCategory
     * */
    public Mapping createMapping(SchemaCategory sc, String kindName) {
        final var rootObject = sc.getObject(this.rootKey);
        //System.out.println("MY_DEBUG root object: " + rootObject);
        ComplexProperty accessPath = buildComplexPropertyFromNode(this.root);
        return Mapping.create(sc, this.rootKey, kindName, accessPath);
    }

    /**
     * Method for creating root Complex property which later server as the access path for the SchemaCat
     * It does so by recursively traversing tree where each node corresponds to a Simple/Complex property
     * @param node
     * @return
     */
    public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
        List<AccessPath> subpaths = new ArrayList<>();

        for (AccessTreeNode child : node.getChildren()) {
            if (child.getState() == AccessTreeNode.State.S) {
                subpaths.add(new SimpleProperty(new StaticName(child.getName()), child.getSig()));
            } else {
                subpaths.add(buildComplexPropertyFromNode(child));
            }
        }
        if (node.getState() == AccessTreeNode.State.S) {
            //return ComplexProperty.createAuxiliary(new StaticName(node.getName()), subpaths);
            return new ComplexProperty(new StaticName(node.getName()), node.getSig(), subpaths);
        } else {
            //return ComplexProperty.create(node.getName(), node.getSig(), subpaths.toArray(new AccessPath[0]));
            AccessPath[] subpathsArr = subpaths.toArray(new AccessPath[0]);
            return new ComplexProperty(new StaticName(node.getName()), node.getSig(), new ArrayList<>(Arrays.asList(subpathsArr)));
        }
    }

}

