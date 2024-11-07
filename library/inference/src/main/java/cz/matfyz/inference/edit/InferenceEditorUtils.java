package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
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

/**
 * The {@code InferenceEditorUtils} class provides utility methods for editing
 * schema categories and metadata within the inference framework. This class
 * includes methods for creating and adding objects and morphisms, removing
 * morphisms and objects, and updating mappings.
 */
public class InferenceEditorUtils {

    private InferenceEditorUtils() {
        throw new UnsupportedOperationException("Utility class InferenceEditorUtils.");
    }

    /**
     * Gets a new unique signature value for a schema.
     */
    private static Signature getNewSignatureValue(SchemaCategory schema) {
        final var currentSignatures = schema.allMorphisms().stream().map(SchemaMorphism::signature).toList();
        final SignatureGenerator generator = SignatureGenerator.create(currentSignatures);

        return generator.next();
    }

    /**
     * Gets a new unique key value for a schema.
     */
    private static int getNewKeyValue(SchemaCategory schema) {
        int max = 0;
        for (SchemaObject object : schema.allObjects()) {
            int keyVal = object.key().getValue();
            if (keyVal > max)
                max = keyVal;
        }
        return max + 1;
    }

    /**
     * Creates and adds a new morphism to the schema and metadata.
     */
    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObject dom, SchemaObject cod, boolean isDual) {
        Signature signature = getNewSignatureValue(schema);

        if (isDual)
            signature = signature.dual();

        final SchemaMorphism newMorphism = new SchemaMorphism(signature, dom, cod, Min.ONE, Set.of());

        schema.addMorphism(newMorphism);
        metadata.setMorphism(newMorphism, new MetadataMorphism(""));

        return newMorphism.signature();
    }

    /**
     * Creates and adds a new morphism to the schema and metadata.
     */
    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObject dom, SchemaObject cod) {
        return createAndAddMorphism(schema, metadata, dom, cod, false);
    }

    private static SchemaMorphism getMorphismIfExists(SchemaCategory schema, SchemaObject dom, SchemaObject cod) {
        for (SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod))
                return morphism;

        return null;
    }

    /**
     * Creates and adds a new object to the schema and metadata.
     */
    public static Key createAndAddObject(SchemaCategory schema, MetadataCategory metadata, ObjectIds ids, String label) {
        final Key key = new Key(getNewKeyValue(schema));
        final SchemaObject object = new SchemaObject(key, ids, SignatureId.createEmpty());

        schema.addObject(object);
        metadata.setObject(object, new MetadataObject(label, Position.createDefault()));

        return key;
    }

    /**
     * Removes morphisms and objects from the schema based on the specified sets of signatures and keys.
     */
    public static void removeMorphismsAndObjects(SchemaCategory schema, Set<Signature> signaturesToDelete, Set<Key> keysToDelete) {
        for (Signature sig : signaturesToDelete) {
            SchemaMorphism morphism = schema.getMorphism(sig);
            schema.removeMorphism(morphism);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schema);
        editor.deleteObjects(keysToDelete);
    }

    /**
     * Updates the schema category by deleting a specified schema object and adding a new schema object in its place.
     * This method also updates the associated metadata for the new object based on the metadata of the deleted object.
     */
    public static void updateObjects(SchemaCategory schema, MetadataCategory metadata, SchemaObject objectToDelete, SchemaObject objectToAdd) {
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schema);
        editor.deleteObject(objectToDelete.key());

        MetadataObject metadataToDelete = metadata.getObject(objectToDelete);

        schema.addObject(objectToAdd);
        metadata.setObject(objectToAdd, new MetadataObject(metadataToDelete.label, metadataToDelete.position));
    }

    /**
     * Creates a new mapping by merging the specified mappings into a new mapping structure.
     */
    public static Mapping createNewMapping(SchemaCategory schema, Mapping mapping, List<Mapping> mappingsToMerge, ComplexProperty accessPath) {
        final Collection<Signature> primaryKey = new HashSet<>();
        primaryKey.addAll(mapping.primaryKey());

        for (final Mapping mappingToMerge : mappingsToMerge)
            if (mappingToMerge.primaryKey() != null)
                primaryKey.addAll(mappingToMerge.primaryKey());

        return mapping.withSchema(schema, accessPath, primaryKey);
    }

    /**
     * Updates the list of mappings by removing the specified mappings to delete and adding the specified mapping to keep.
     */
    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, Mapping mappingToKeep) {
        return updateMappings(mappings, mappingsToDelete, Arrays.asList(mappingToKeep));
    }

    /**
     * Updates the list of mappings by removing the specified mappings to delete and adding the specified mappings to keep.
     */
    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, List<Mapping> mappingsToKeep) {
        List<Mapping> updatedMappings = new ArrayList<>();
        for (Mapping mapping : mappings)
            if (!mappingsToDelete.contains(mapping))
                updatedMappings.add(mapping);

        for (Mapping mapping : mappingsToKeep)
            updatedMappings.add(mapping);

        return updatedMappings;
    }

    /**
     * Creates a deep copy of the specified schema category.
     */
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

    /**
     * Creates a deep copy of the specified metadata category, based on a given schema.
     */
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

    /**
     * Finds a key from a fully qualified name within the schema and metadata.
     */
    public static Key findKeyFromName(SchemaCategory schemaCategory, MetadataCategory metadata, String fullName) {
        String[] nameParts = fullName.split("/");
        if (nameParts.length != 2)
            throw new IllegalArgumentException("Invalid full name format: " + fullName);

        String parentName = nameParts[0];
        String childName = nameParts[1];

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            SchemaObject dom = morphism.dom();
            SchemaObject cod = morphism.cod();

            MetadataObject metaDom = metadata.getObject(dom);
            MetadataObject metaCod = metadata.getObject(cod);

            if (metaDom.label.equals(parentName) && metaCod.label.equals(childName))
                return cod.key();
        }

        throw new NotFoundException("Key for name " + fullName + " does not exist");
    }

    /**
     * Inner class that provides editing capabilities for a {@code SchemaCategory}.
     */
    public static class SchemaCategoryEditor extends SchemaCategory.Editor {

        public final SchemaCategory schema;

        public SchemaCategoryEditor(SchemaCategory schema) {
            this.schema = schema;
        }

        /**
         * Deletes an object from the schema by its key.
         */
        public void deleteObject(Key key) {
            final var objects = getObjects(schema);
            if (!objects.containsKey(key))
                throw new NotFoundException("SchemaObject with key " + key + " does not exist");

            objects.remove(key);
        }

        /**
         * Deletes multiple objects from the schema by their keys.
         */
        public void deleteObjects(Set<Key> keys) {
            for (Key key : keys) {
                deleteObject(key);
            }
        }
    }
}
