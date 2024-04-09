package cz.matfyz.inference.schemaconversion;

import java.util.HashSet;
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
        AccessTreeNode currentNode = new AccessTreeNode(null, null, null);
        convertToSchemafromRSD(sc, rsd, rootKey, 1, currentNode);
        SCUtils.addIndexObjecttoArr(sc);
        MappingCreator mappingCreator = new MappingCreator(rootKey, root);
        //Mapping mapping = mappingCreator.createMapping(sc, "Full_schema_mapping"); //What will this label be?
        Mapping mapping = mappingCreator.createMapping(sc, "yelpbusinesssample"); //What will this label be?
        System.out.println("root is auxiliary: " + mapping.accessPath().isAuxiliary());
        return new CategoryMappingPair(sc, mapping);
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
            //Signature s = Signature.createBase(1); //this part is not right for the AccessTreeNode creation
            /*
            Signature s = Signature.createEmpty();

            Set<Signature> ss = new HashSet<>();
            ss.add(s);

            ObjectIds objectIds = new ObjectIds(s);

            SignatureId sId = new SignatureId(ss); */
            Signature s = Signature.createEmpty();
            ObjectIds ids = ObjectIds.createGenerated();
            SignatureId superId = SignatureId.createEmpty();
            so = new SchemaObject(keyp, "yelpbusinesssample", ids, superId);
            //so = new SchemaObject(keyp, rsdp.getName(), ids, superId);
            sc.addObject(so);

            //this.root = new AccessTreeNode(AccessTreeNode.State.Complex, rsdp.getName(), s);
            this.root = new AccessTreeNode(AccessTreeNode.State.Complex, "yelpbusinesssample", s);
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
                
                Signature sig = SCUtils.createChildSignature(keyp, keych);
                Min min = SCUtils.findMin(rsdp, rsdch);
/*
                Set<Signature> sigSet = new HashSet<>();
                sigSet.add(sig);

                ObjectIds objectIds = new ObjectIds(sig);

                SignatureId superId = new SignatureId(sigSet);*/
                ObjectIds ids;
                // kdyz nema deti, je to list a chceme mu dat ids value
                if (rsdch.getChildren().isEmpty()) {
                    ids = ObjectIds.createValue();
                }
                else { ids = ObjectIds.createGenerated(); }
                Set<Signature> sigSet = new HashSet<>();
                sigSet.add(sig);

                SignatureId superId = new SignatureId(sigSet);
                SchemaObject soch = new SchemaObject(keych, rsdch.getName(), ids, superId);
                sc.addObject(soch);

                AccessTreeNode child = new AccessTreeNode(AccessTreeNode.State.Simple, soch.label(), sig);
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
    }


    private boolean isTypeArray(RecordSchemaDescription rsd) {
        return (rsd.getTypes() & Type.ARRAY) != 0;
    }
}