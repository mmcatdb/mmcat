package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObject;
import cz.matfyz.core.metadata.MetadataObject.Position;

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

    private static int getNewSignatureValue(SchemaCategory schema) {
        int max = 0;
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            // TODO: here I am relying on the fact, that in inference I create only BaseSignatures
            int signatureVal = Integer.parseInt(morphism.signature().toString());
            if (signatureVal > max) {
                max = signatureVal;
            }
        }
        return max + 1;
    }

    private static int getNewKeyValue(SchemaCategory schema) {
        int max = 0;
        for (SchemaObject object : schema.allObjects()) {
            // TODO: here I am relying on the fact, that in inference I create only BaseSignatures
            int keyVal = object.key().getValue();
            if (keyVal > max) {
                max = keyVal;
            }
        }
        return max + 1;
    }

    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObject dom, SchemaObject cod) {
        final SchemaMorphism existingMorphism = getMorphismIfExists(schema, dom, cod);
        if (existingMorphism != null)
            return existingMorphism.signature();

        final BaseSignature signature = Signature.createBase(getNewSignatureValue(schema));
        final SchemaMorphism newMorphism = new SchemaMorphism(signature, dom, cod, Min.ONE, Set.of());

        schema.addMorphism(newMorphism);
        metadata.setMorphism(newMorphism, new MetadataMorphism(""));

        return newMorphism.signature();
    }

    private static SchemaMorphism getMorphismIfExists(SchemaCategory schema, SchemaObject dom, SchemaObject cod) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod)) {
                return morphism;
            }
        }
        return null;
    }

    public static Key createAndAddObject(SchemaCategory schema, MetadataCategory metadata, ObjectIds ids, String label) {
        final Key key = new Key(getNewKeyValue(schema));
        final SchemaObject object = new SchemaObject(key, ids, SignatureId.createEmpty());

        schema.addObject(object);
        metadata.setObject(object, new MetadataObject(label, Position.createDefault()));

        return key;
    }

    public static void removeMorphismsAndObjects(SchemaCategory schema, Set<Signature> signaturesToDelete, Set<Key> keysToDelete) {
        for (Signature sig : signaturesToDelete) {
            SchemaMorphism morphism = schema.getMorphism(sig);
            schema.removeMorphism(morphism);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schema);
        editor.deleteObjects(keysToDelete);
    }

    public static Mapping createNewMapping(SchemaCategory schema, Mapping mapping, List<Mapping> mappingsToMerge, ComplexProperty accessPath) {
        Collection<Signature> primaryKey = new HashSet<>();
        if (mapping.primaryKey() != null) {
            primaryKey.addAll(mapping.primaryKey());
        }
        for (Mapping mappingToMerge : mappingsToMerge) {
            if (mappingToMerge.primaryKey() != null) {
                primaryKey.addAll(mappingToMerge.primaryKey());
            }
        }
        return new Mapping(schema, mapping.rootObject().key(), mapping.kindName(), accessPath, primaryKey);
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

    public static SchemaCategory createSchemaCopy(SchemaCategory original) {
        final SchemaCategory copy = new SchemaCategory();

        for (final SchemaObject object : original.allObjects())
            copy.addObject(new SchemaObject(object.key(), object.ids(), object.superId()));

        for (final SchemaMorphism morphism : original.allMorphisms())
            copy.addMorphism(new SchemaMorphism(
                morphism.signature(),
                copy.getObject(morphism.dom().key()),
                copy.getObject(morphism.cod().key()),
                morphism.min(),
                morphism.tags()
            ));

        return copy;
    }

    public static MetadataCategory createMetadataCopy(MetadataCategory original, SchemaCategory schema) {
        final MetadataCategory copy = MetadataCategory.createEmpty(schema);

        for (final SchemaObject object : schema.allObjects()) {
            final var mo = original.getObject(object);
            copy.setObject(object, new MetadataObject(mo.label, mo.position));
        }

        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            final var mm = original.getMorphism(morphism);
            copy.setMorphism(morphism, new MetadataMorphism(mm.label));
        }

        return copy;
    }

    public static Key findKeyFromName(SchemaCategory schemaCategory, MetadataCategory metadata, String fullName) {
        String[] nameParts = fullName.split("/");
        if (nameParts.length != 2) {
            throw new IllegalArgumentException("Invalid full name format: " + fullName);
        }

        String parentName = nameParts[0];
        String childName = nameParts[1];

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            SchemaObject dom = morphism.dom();
            SchemaObject cod = morphism.cod();

            MetadataObject metaDom = metadata.getObject(dom);
            MetadataObject metaCod = metadata.getObject(cod);

            if (metaDom.label.equals(parentName) && metaCod.label.equals(childName)) {
                return cod.key();
            }
        }
        throw new NotFoundException("Key for name " + fullName + " does not exist");
    }

    public static class SchemaCategoryEditor extends SchemaCategory.Editor {

        public final SchemaCategory schema;

        public SchemaCategoryEditor(SchemaCategory schema) {
            this.schema = schema;
        }

        public void deleteObject(Key key) {
            final var objects = getObjects(schema);
            if (!objects.containsKey(key))
                throw new NotFoundException("SchemaObject with key " + key + " does not exist");

            objects.remove(key);
        }

        public void deleteObjects(Set<Key> keys) {
            for (Key key : keys) {
                deleteObject(key);
            }
        }
    }
}
