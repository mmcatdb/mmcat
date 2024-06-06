package cz.matfyz.inference.schemaconversion.utils;

import java.util.Objects;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.schema.SchemaMorphism.Min;

public class SchemaConversionUtils {

    /**
     * To label the morphisms
     */
    public enum Label {
        IDENTIFIER, RELATIONAL;
    }

    private SchemaConversionUtils() {}

    /**
     * For creating the child key.
     * The keys need to be positive, otherwise there are problems with retrieving Positions in the MetadatContext later.
     * (But it might create collisions)
     */
    public static Key createChildKey(Key keyp, int i) {
        int keyvalp = keyp.getValue();
        int keyvalch = Math.abs(Objects.hash(keyvalp, i));
        return new Key(keyvalch);
    }
    /**
     * For finding the cardinality between rsds
     */
    public static Min findMin(RecordSchemaDescription rsdp, RecordSchemaDescription rsdch) {
        int sharepTotal = rsdp.getShareTotal();
        int sharechTotal = rsdch.getShareTotal();

        int sharepFirst = rsdp.getShareFirst();
        int sharechFirst = rsdch.getShareFirst();

        Min min = Min.ONE;

        if (sharepTotal == sharechTotal && sharepFirst == sharechFirst) {
            min = Min.ONE;
        } else if (sharepTotal > sharechTotal && sharepFirst > sharechFirst) {
            min = Min.ZERO;
        } else if (sharepTotal < sharechTotal) {
            if (sharepFirst < sharechFirst) {
                min = Min.ONE;
            } else if (sharepFirst == sharechFirst) {
                min = Min.ZERO;
            }
        }
        return min;
    }

    /**
     * For getting the right label for the Schema Morphism.
     * For now we label 2 types of morphisms: identification & relational.
     * A morphism is labeled as identification if its codomain is an identificator
     * And it is labeled as relational if its domain is an array type
     */
    public static String createLabel(RecordSchemaDescription rsd, boolean isArray) {
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
     * This is done once the whole Schema Category gets created.
     *//*
    public SchemaCategory addIndexObjecttoArr(SchemaCategory sc) {
        for (SchemaMorphism s: sc.allMorphisms()) {
            if (s.label == Label.RELATIONAL.name()) { // if the object is an array
                SchemaObject sop = s.dom();
                Key keyp = sop.key();
                Key keych = createChildKey(keyp, 0); // is the 0 ok?

                SchemaObject soch = new SchemaObject(keych, "_index", null, null);
                sc.addObject(soch);

                Signature sig = createChildSignature(keyp, keych);
                Set<SchemaMorphism.Tag> tags = new HashSet<>();
                SchemaMorphism sm = new SchemaMorphism(sig, null, Min.ZERO, tags, sop, soch); //is the min right?
                sc.addMorphism(sm);
            }
        }
        return sc;
    }*/

}
