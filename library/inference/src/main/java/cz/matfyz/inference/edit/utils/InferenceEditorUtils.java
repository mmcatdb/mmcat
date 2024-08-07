package cz.matfyz.inference.edit.utils;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.identifiers.UniqueContext;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.yarn.webapp.NotFoundException;

public class InferenceEditorUtils {

    private InferenceEditorUtils() {
        throw new UnsupportedOperationException("Utility class InferenceEditorUtils.");
    }

    private static int getNewSignatureValue(SchemaCategory schemaCategory) {
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

    private static int getNewKeyValue(SchemaCategory schemaCategory) {
        int max = 0;
        for (SchemaObject object : schemaCategory.allObjects()) {
            // TODO: here I am relying on the fact, that in inference I create only BaseSignatures
            int keyVal = object.key().getValue();
            if (keyVal > max) {
                max = keyVal;
            }
        }
        return max + 1;
    }

    public static Signature createAndAddMorphism(SchemaCategory schemaCategory, SchemaObject dom, SchemaObject cod) {
        SchemaMorphism existingMorphism = getMorphismIfExists(schemaCategory, dom, cod);
        if (existingMorphism != null) {
            return existingMorphism.signature();
        } else {
            SchemaMorphism newMorphism = createMorphism(schemaCategory, dom, cod);
            schemaCategory.addMorphism(newMorphism);
            return newMorphism.signature();
        }
    }

    private static SchemaMorphism createMorphism(SchemaCategory schemaCategory, SchemaObject dom, SchemaObject cod) {
        BaseSignature signature = Signature.createBase(getNewSignatureValue(schemaCategory));
        return new SchemaMorphism(signature, null, Min.ONE, new HashSet<>(), dom, cod);
    }

    private static SchemaMorphism getMorphismIfExists(SchemaCategory schemaCategory, SchemaObject dom, SchemaObject cod) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod)) {
                return morphism;
            }
        }
        return null;
    }

    public static Key createAndAddObject(SchemaCategory schemaCategory, String objectLabel, ObjectIds ids) {
        SchemaObject newObject = createObject(schemaCategory, objectLabel, ids);
        schemaCategory.addObject(newObject);
        return newObject.key();
    }

    private static SchemaObject createObject(SchemaCategory schemaCategory, String objectLabel, ObjectIds ids) {
        Key key = new Key(getNewKeyValue(schemaCategory));
        return new SchemaObject(key, objectLabel, ids, SignatureId.createEmpty());
    }

    public static void removeMorphismsAndObjects(SchemaCategory schemaCategory, Set<Signature> signaturesToDelete, Set<Key> keysToDelete) {
        for (Signature sig : signaturesToDelete) {
            SchemaMorphism morphism = schemaCategory.getMorphism(sig);
            schemaCategory.removeMorphism(morphism);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schemaCategory);
        editor.deleteObjects(keysToDelete);
    }

    public static Mapping createNewMapping(SchemaCategory schemaCategory, Mapping mapping, List<Mapping> mappingsToMerge, ComplexProperty accessPath) {
        Collection<Signature> primaryKey = new HashSet<>();
        if (mapping.primaryKey() != null) {
            primaryKey.addAll(mapping.primaryKey());
        }
        for (Mapping mappingToMerge : mappingsToMerge) {
            if (mappingToMerge.primaryKey() != null) {
                primaryKey.addAll(mappingToMerge.primaryKey());
            }
        }
        return new Mapping(schemaCategory, mapping.rootObject().key(), mapping.kindName(), accessPath, primaryKey);
    }

    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, Mapping mappingToKeep) {
        return updateMappings(mappings, mappingsToDelete, Arrays.asList(mappingToKeep));
    }

    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, List<Mapping> mappingsToKeep) {
        List<Mapping> updatedMappings = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (!mappingsToDelete.contains(mapping)) {
                updatedMappings.add(mapping);
            }
        }
        for (Mapping mapping : mappingsToKeep) {
            updatedMappings.add(mapping);
        }

        return updatedMappings;
    }

    public static SchemaCategory createSchemaCategoryCopy(SchemaCategory original) {
        SchemaCategory copy = new SchemaCategory(original.label);

        for (SchemaObject schemaObject : original.allObjects()) {
            SchemaObject objectCopy = new SchemaObject(schemaObject.key(), schemaObject.label(), schemaObject.ids(), schemaObject.superId());
            copy.addObject(objectCopy);
        }

        for (SchemaMorphism morphism : original.allMorphisms()) {
            SchemaObject domCopy = copy.getObject(morphism.dom().key());
            SchemaObject codCopy = copy.getObject(morphism.cod().key());
            SchemaMorphism morphCopy = new SchemaMorphism(morphism.signature(), morphism.label, morphism.min(), morphism.tags(), domCopy, codCopy); // Adjust according to your SchemaMorphism constructor
            copy.addMorphism(morphCopy);
        }

        return copy;
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

        public void deleteObjects(Set<Key> keys) {
            for (Key key : keys) {
                deleteObject(key);
            }
        }
    }
}
