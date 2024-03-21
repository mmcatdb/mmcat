package cz.matfyz.inference.schemaconversion.utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism.Min;

public class SchemaConversionUtils {
    
    /**
     * To label the morphisms
     */
    public enum Label {
        IDENTIFIER, RELATIONAL;
    }
    
    public SchemaConversionUtils() {}

    /**
     * Helper method for creating the child key
     * @param keyParent
     * @param i
     * @return Child's key
     */
    public Key createChildKey(Key keyp, int i) {
        int keyvalp = keyp.getValue();
        int keyvalch = Objects.hash(keyvalp, i);
        return new Key(keyvalch);
    }

    /**
     * Helper method for creating signature for the parent-child morphism
     * @param keyParent
     * @param keyChild
     * @return Signature
     */
    public Signature createChildSignature(Key keyp, Key keych) {
        int sigval = Objects.hash(keyp.getValue(), keych.getValue());
        return Signature.createBase(sigval);
    }

    /**
     * Finding the cardinality between rsds
     * @param rsdp
     * @param rsdch
     * @return
     */
    public Min findMin(RecordSchemaDescription rsdp, RecordSchemaDescription rsdch) {
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
    /**
     * Get the right label for the Schema Morphism.
     * For now we label 2 types of morphisms: identification & relational.
     * A morphism is labeled as identification if its codomain is an identificator
     * And it is labeled as relational if its domain is an array type
     * @return
     */
    
    public String createLabel(RecordSchemaDescription rsdch, boolean isArrayp, boolean isArraych) {

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
    public SchemaCategory addIndexObjecttoArr(SchemaCategory sc) {
        for (SchemaMorphism s: sc.allMorphisms()) {
            if (s.label == Label.RELATIONAL.name()) { // if the object is an array
                SchemaObject sop = s.dom();
                Key keyp = sop.key();
                Key keych = createChildKey(keyp, 0); // probs could just put zero here, but have to check!!
                
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
}
