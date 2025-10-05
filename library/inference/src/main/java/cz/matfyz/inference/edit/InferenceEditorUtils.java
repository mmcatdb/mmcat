package cz.matfyz.inference.edit;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.exception.ObjexNotFoundException;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataObjex.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code InferenceEditorUtils} class provides utility methods for editing
 * schema categories and metadata within the inference framework. This class
 * includes methods for creating and adding objexes and morphisms, removing
 * morphisms and objexes, and updating mappings.
 */
public class InferenceEditorUtils {

    private InferenceEditorUtils() {}

    public static Signature addMorphismWithMetadata(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod) {
        return addMorphismWithMetadata(schema, metadata, dom, cod, false, null);
    }

    public static Signature addMorphismWithMetadata(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod, @Nullable Signature signature) {
        return addMorphismWithMetadata(schema, metadata, dom, cod, false, signature);
    }

    public static Signature addMorphismWithMetadata(SchemaCategory schema, MetadataCategory metadata, SchemaObjex dom, SchemaObjex cod, boolean isDual, @Nullable Signature signature) {
        if (signature == null)
            // TODO cache the signature generator.
            signature = schema.createSignatureGenerator().next();

        if (isDual)
            signature = signature.dual();

        final var morphism = schema.addMorphism(new SerializedMorphism(signature, dom.key(), cod.key(), Min.ONE, Set.of()));
        metadata.setMorphism(morphism, new MetadataMorphism(""));

        return morphism.signature();
    }

    public static Key addObjexWithMetadata(SchemaCategory schema, MetadataCategory metadata, ObjexIds ids, String label) {
        // TODO cache the key generator.
        final Key key = schema.createKeyGenerator().next();
        final var objex = schema.addObjex(new SerializedObjex(key, ids));
        metadata.setObjex(objex, new MetadataObjex(label, Position.createDefault()));

        return key;
    }

    public static void removeMorphismsAndObjexes(SchemaCategory schema, KeysAndSignatures toDelete) {
        for (final var signature : toDelete.signatures)
            schema.removeMorphism(signature);

        for (final var key : toDelete.keys)
            schema.removeObjex(key);
    }

    /** Finds Signature for a morphism between specified domain and codomain. */
    public static Signature findSignatureBetween(SchemaCategory schema, SchemaObjex dom, SchemaObjex cod) {
        for (final SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.dom().equals(dom) && morphism.cod().equals(cod))
                return morphism.signature();

        throw MorphismNotFoundException.withMessage("Signature between objexes: " + dom + " and " + cod + " has not been found");
    }

    /** Updates the list of mappings by removing the specified mappings to delete and adding the specified mapping to keep. */
    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, Mapping mappingToKeep) {
        return updateMappings(mappings, mappingsToDelete, Arrays.asList(mappingToKeep));
    }

    /** Updates the list of mappings by removing the specified mappings to delete and adding the specified mappings to keep. */
    public static List<Mapping> updateMappings(List<Mapping> mappings, List<Mapping> mappingsToDelete, List<Mapping> mappingsToKeep) {
        final List<Mapping> updatedMappings = new ArrayList<>();
        for (final Mapping mapping : mappings)
            if (!mappingsToDelete.contains(mapping))
                updatedMappings.add(mapping);

        for (final Mapping mapping : mappingsToKeep)
            updatedMappings.add(mapping);

        return updatedMappings;
    }

    /** Finds a key from a fully qualified name within the schema and metadata. */
    public static Key findKeyFromName(SchemaCategory schemaCategory, MetadataCategory metadata, String fullName) {
        String[] nameParts = fullName.split("/");
        if (nameParts.length != 2)
            throw new IllegalArgumentException("Invalid full name format: " + fullName);

        String parentName = nameParts[0];
        String childName = nameParts[1];

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            final var dom = metadata.getObjex(morphism.dom());
            final var cod = metadata.getObjex(morphism.cod());

            if (dom.label.equals(parentName) && cod.label.equals(childName))
                return morphism.cod().key();
        }

        throw ObjexNotFoundException.withMessage("Key for name " + fullName + " does not exist");
    }

    public static record KeysAndSignatures(Set<Key> keys, Set<Signature> signatures) {

        public KeysAndSignatures() {
            this(new TreeSet<>(), new TreeSet<>());
        }

        public void add(Key key) {
            keys.add(key);
        }

        public void add(Signature signature) {
            signatures.add(signature);
        }

    }

}
