package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataObjex.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.hadoop.yarn.webapp.NotFoundException;

/**
 * The {@code InferenceEditorUtils} class provides utility methods for editing
 * schema categories and metadata within the inference framework. This class
 * includes methods for creating and adding objexes and morphisms, removing
 * morphisms and objexes, and updating mappings.
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
        for (SchemaObjex objex : schema.allObjexes()) {
            int keyVal = objex.key().getValue();
            if (keyVal > max)
                max = keyVal;
        }
        return max + 1;
    }

    /**
     * Creates and adds a new morphism to the schema and metadata.
     */
    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod, boolean isDual, @Nullable Signature signature) {
        if (signature == null)
            signature = getNewSignatureValue(schema);

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
    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod) {
        return createAndAddMorphism(schema, metadata, dom, cod, false, null);
    }

    /**
     * Creates and adds a new morphism to the schema and metadata.
     */
    public static Signature createAndAddMorphism(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod, @Nullable Signature signature) {
        return createAndAddMorphism(schema, metadata, dom, cod, false, signature);
    }

    /**
     * Creates and adds a new objex to the schema and metadata.
     */
    public static Key createAndAddObjex(SchemaCategory schema, MetadataCategory metadata, ObjexIds ids, String label) {
        final Key key = new Key(getNewKeyValue(schema));
        final SchemaObjex objex = new SchemaObjex(key, ids, ids.generateDefaultSuperId());

        schema.addObjex(objex);
        metadata.setObjex(objex, new MetadataObjex(label, Position.createDefault()));

        return key;
    }

    /**
     * Removes morphisms and objexes from the schema based on the specified sets of signatures and keys.
     */
    public static void removeMorphismsAndObjexes(SchemaCategory schema, Set<Signature> signaturesToDelete, Set<Key> keysToDelete) {
        for (Signature sig : signaturesToDelete) {
            SchemaMorphism morphism = schema.getMorphism(sig);
            schema.removeMorphism(morphism);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schema);
        editor.deleteObjexes(keysToDelete);
    }

    /**
     * Updates the schema category by deleting a specified schema objex and adding a new schema objex in its place.
     * This method also updates the associated metadata for the new objex based on the metadata of the deleted objex.
     */
    public static void updateObjexes(SchemaCategory schema, MetadataCategory metadata, SchemaObjex objexToDelete, SchemaObjex objexToAdd) {
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schema);
        editor.deleteObjex(objexToDelete.key());

        MetadataObjex metadataToDelete = metadata.getObjex(objexToDelete);

        schema.addObjex(objexToAdd);
        metadata.setObjex(objexToAdd, new MetadataObjex(metadataToDelete.label, metadataToDelete.position));
    }

    /**
     * Updates metadata by deleting a specified metadata objex and adding a new metadata objex in its place with changed label.
     */
    public static void updateMetadataObjexesLabel(SchemaObjex objex, MetadataCategory metadata, String newLabel) {
        MetadataObjex metadataToDelete = metadata.getObjex(objex);
        metadata.setObjex(objex, new MetadataObjex(newLabel, metadataToDelete.position));
    }

    /**
     * Finds Signature for a morphism between specified domain and codomain.
     */
    public static Signature findSignatureBetween(SchemaCategory schema, SchemaObjex dom, SchemaObjex cod) {
        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod))
                return morphism.signature();
        }
        throw new NotFoundException("Signature between objexes: " + dom + " and " + cod + " has not been found");
    }

    public static String findLabelFromKey(Key key, MetadataCategory metadata) {
        return metadata.getObjex(key).label;
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
        final List<Mapping> updatedMappings = new ArrayList<>();
        for (final Mapping mapping : mappings)
            if (!mappingsToDelete.contains(mapping))
                updatedMappings.add(mapping);

        for (final Mapping mapping : mappingsToKeep)
            updatedMappings.add(mapping);

        return updatedMappings;
    }

    /**
     * Creates a deep copy of the specified schema category.
     */
    public static SchemaCategory createSchemaCopy(SchemaCategory original) {
        final SchemaCategory copy = new SchemaCategory();

        for (final SchemaObjex objex : original.allObjexes())
            copy.addObjex(new SchemaObjex(objex.key(), objex.ids(), objex.superId()));

        for (final SchemaMorphism morphism : original.allMorphisms())
            copy.addMorphism(new SchemaMorphism(
                morphism.signature(),
                copy.getObjex(morphism.dom().key()),
                copy.getObjex(morphism.cod().key()),
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

        for (final SchemaObjex objex : schema.allObjexes()) {
            final var mo = original.getObjex(objex);
            copy.setObjex(objex, new MetadataObjex(mo.label, mo.position));
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
            SchemaObjex dom = morphism.dom();
            SchemaObjex cod = morphism.cod();

            MetadataObjex metaDom = metadata.getObjex(dom);
            MetadataObjex metaCod = metadata.getObjex(cod);

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
         * Deletes an objex from the schema by its key.
         */
        public void deleteObjex(Key key) {
            final var objexes = getObjexes(schema);
            if (!objexes.containsKey(key))
                throw new NotFoundException("SchemaObjex with key " + key + " does not exist");

            objexes.remove(key);
        }

        /**
         * Deletes multiple objexes from the schema by their keys.
         */
        public void deleteObjexes(Set<Key> keys) {
            for (Key key : keys) {
                deleteObjex(key);
            }
        }
    }
}
