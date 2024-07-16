package cz.matfyz.inference.edit.utils;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.UniqueContext;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.yarn.webapp.NotFoundException;

public class InferenceEditorUtils {

    private InferenceEditorUtils() {
        throw new UnsupportedOperationException("Utility class InferenceEditorUtils.");
    }

    public static int getNewSignatureValue(SchemaCategory schemaCategory) {
        int max = 0;
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            // TODO: here I am relying on the fact, that in inference I create only BaseSignatures
            int signatureVal = Integer.parseInt(morphism.signature().toString());
            if (signatureVal > max) {
                max = signatureVal;
            }
        }
        return max + 1;
    }

    public static Signature createAndAddMorphism(SchemaCategory schemaCategory, SchemaObject dom, Key codKey) {
        SchemaMorphism existingMorphism = getMorphismIfExists(schemaCategory, dom, codKey);
        if (existingMorphism != null) {
            return existingMorphism.signature();
        } else {
            SchemaMorphism newMorphism = createMorphism(schemaCategory, dom, codKey);
            schemaCategory.addMorphism(newMorphism);
            return newMorphism.signature();
        }
    }

    private static SchemaMorphism createMorphism(SchemaCategory schemaCategory, SchemaObject dom, Key codKey) {
        SchemaObject cod = schemaCategory.getObject(codKey);
        BaseSignature signature = Signature.createBase(InferenceEditorUtils.getNewSignatureValue(schemaCategory));
        return new SchemaMorphism(signature, null, Min.ONE, new HashSet<>(), dom, cod);
    }

    private static SchemaMorphism getMorphismIfExists(SchemaCategory schemaCategory, SchemaObject dom, Key codKey) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().equals(dom) && morphism.cod().key().equals(codKey)) {
                return morphism;
            }
        }
        return null;
    }

    public static class SchemaCategoryEditor extends SchemaCategory.Editor {

        public final SchemaCategory schemaCategory;

        public SchemaCategoryEditor(SchemaCategory schemaCategory) {
            this.schemaCategory = schemaCategory;
        }

        public void deleteObject(Key key) {
            UniqueContext<SchemaObject, Key> objectContext = getObjectContext(schemaCategory);
            SchemaObject objectToRemove = objectContext.getUniqueObject(key);
            if (objectToRemove != null) {
                objectContext.deleteUniqueObject(objectToRemove);
            } else {
                throw new NotFoundException("SchemaObject with the provided key does not exist");
            }
        }

        public void deleteObjects(List<Key> keys) {
            for (Key key : keys) {
                deleteObject(key);
            }
        }
    }
}
