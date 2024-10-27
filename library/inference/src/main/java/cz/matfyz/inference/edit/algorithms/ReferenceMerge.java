package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code ReferenceMerge} class implements an algorithm for merging references
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides functionality
 * to modify schema objects and mappings based on reference merging rules.
 */
public class ReferenceMerge extends InferenceEditAlgorithm {

    /**
     * Data class representing the parameters and state needed for the reference merge.
     */
    public static class Data extends InferenceEdit {

        @JsonProperty("referenceKey")
        private Key referenceKey;

        @JsonProperty("referredKey")
        private Key referredKey;

        @JsonProperty("candidate")
        private final ReferenceCandidate candidate;

        /**
         * Constructs a {@code Data} instance with specified parameters.
         *
         * @param id The ID of the edit.
         * @param isActive The active status of the edit.
         * @param referenceKey The key representing the reference to merge.
         * @param referredKey The key representing the referred object.
         * @param candidate The candidate reference information.
         */
        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("referenceKey") Key referenceKey,
                @JsonProperty("referredKey") Key referredKey,
                @JsonProperty("candidate") ReferenceCandidate candidate) {
            setId(id);
            setActive(isActive);
            this.referenceKey = referenceKey;
            this.referredKey = referredKey;
            this.candidate = candidate;
        }

        /**
         * Default constructor initializing data with default values.
         */
        public Data() {
            setId(null);
            setActive(false);
            this.referenceKey = null;
            this.referredKey = null;
            this.candidate = null;
        }

        /**
         * Gets the key representing the reference.
         *
         * @return The reference key.
         */
        public Key getReferenceKey() {
            return referenceKey;
        }

        /**
         * Gets the key representing the referred object.
         *
         * @return The referred key.
         */
        public Key getReferredKey() {
            return referredKey;
        }

        /**
         * Gets the candidate reference information.
         *
         * @return The reference candidate.
         */
        public ReferenceCandidate getCandidate() {
            return candidate;
        }

        /**
         * Creates an instance of the {@code ReferenceMerge} algorithm.
         *
         * @return A new instance of {@code ReferenceMerge}.
         */
        @Override public ReferenceMerge createAlgorithm() {
            return new ReferenceMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ReferenceMerge.class.getName());
    private static final String INDEX_LABEL = "_index";

    private final Data data;

    private boolean referenceIsArray;

    private Signature oldReferenceSignature;
    private Signature newReferenceSignature;
    private Signature oldIndexSignature;
    private Signature newIndexSignature;

    /**
     * Constructs a {@code ReferenceMerge} instance with the specified data.
     *
     * @param data The data model containing reference information and merge settings.
     */
    public ReferenceMerge(Data data) {
        this.data = data;
    }

    /*
     * Applies the primary key merging algorithm to the schema category.
     * Assumption: when there is a reference and it is an array object,
     * it has 2 outgoing morphisms: one for _index and one for the original parent node.
     * If it is not an array, it has only one incoming morphism from the root.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.referenceKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referencing());
            data.referredKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referred());
        }

        System.out.println("reference: " + data.referenceKey);
        System.out.println("referred: " + data.referredKey);
        System.out.println("id: " + data.getId());
        System.out.println("isActive: " + data.isActive());

        this.referenceIsArray = isReferenceArray(newSchema, newMetadata);

        SchemaObject referredObject = newSchema.getObject(data.referredKey);

        Key referenceParentKey = getReferenceParentKey(newSchema, newMetadata, referenceIsArray);

        SchemaObject dom = newSchema.getObject(referenceParentKey);
        SchemaObject cod = referredObject;
        Key indexKey = null;
        if (referenceIsArray) {
            dom = referredObject;
            cod = newSchema.getObject(referenceParentKey);

            indexKey = getIndexKey(newSchema, newMetadata, data.referenceKey);
            this.newIndexSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, referredObject, newSchema.getObject(indexKey));
        }
        this.newReferenceSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod);

        findMorphismsAndObjectToDelete(newSchema, indexKey);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
    }

    /**
     * Determines if the reference is an array based on its morphisms.
     *
     * @param schema The schema category to check.
     * @param metadata The metadata category associated with the schema.
     * @return {@code true} if the reference is an array; {@code false} otherwise.
     */
    private boolean isReferenceArray(SchemaCategory schema, MetadataCategory metadata) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().key().equals(data.referenceKey) && metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the parent key of the reference object in the schema based on whether it is an array.
     *
     * @param schema The schema category to search within.
     * @param metadata The metadata category associated with the schema.
     * @param isReferenceArray Flag indicating if the reference is an array.
     * @return The parent key of the reference object.
     * @throws NotFoundException if the parent key is not found.
     */
    private Key getReferenceParentKey(SchemaCategory schema, MetadataCategory metadata, boolean isReferenceArray) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (isReferenceArray) {
                if (morphism.dom().key().equals(data.referenceKey) && !metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL))
                    return morphism.cod().key();
            } else {
                if (morphism.cod().key().equals(data.referenceKey))
                    return morphism.dom().key();
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    /**
     * Finds the index key associated with a given reference key in the schema.
     *
     * @param schema The schema category to search within.
     * @param metadata The metadata category associated with the schema.
     * @param key The reference key to find the index for.
     * @return The index key associated with the reference key.
     * @throws NotFoundException if the index key is not found.
     */
    private Key getIndexKey(SchemaCategory schema, MetadataCategory metadata, Key key) {
        for (SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.dom().key().equals(key) && metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL))
                return morphism.cod().key();

        throw new NotFoundException("Index key has not been found");
    }

    /**
     * Identifies morphisms and objects to delete based on the reference and index keys.
     *
     * @param schema The schema category containing the morphisms and objects.
     * @param indexKey The index key to check for deletion.
     */
    private void findMorphismsAndObjectToDelete(SchemaCategory schema, Key indexKey) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (indexKey != null && morphism.dom().key().equals(data.referenceKey)) {
                signaturesToDelete.add(morphism.signature());
                // find the reference and index signatures
                if (morphism.cod().key().equals(indexKey))
                    oldIndexSignature = morphism.signature();
                else
                    oldReferenceSignature = morphism.signature();
            } else {
                if (morphism.cod().key().equals(data.referenceKey)) {
                    signaturesToDelete.add(morphism.signature());
                    oldReferenceSignature = morphism.signature();
                }
            }
        }
        keysToDelete.add(data.referenceKey);
    }

    /**
     * Applies the mapping edit to a list of mappings.
     *
     * @param mappings The list of mappings to edit.
     * @return The updated list of mappings.
     */
    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        /*
        if (referenceIsArray) {
            this.oldReferenceSignature = this.oldReferenceSignature.dual();
            this.newReferenceSignature = this.newReferenceSignature.dual();
        }*/

        Mapping referenceMapping = findReferenceMapping(mappings);
        // Mapping referredMapping = findReferredMapping(mappings, newSchemaCategory);

        Mapping cleanedReferenceMapping = createCleanedMapping(referenceMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping), Arrays.asList(cleanedReferenceMapping));
    }

    /**
     * Finds the mapping corresponding to the reference based on the old reference signature.
     *
     * @param mappings The list of mappings to search.
     * @return The mapping corresponding to the reference.
     * @throws NotFoundException if the reference mapping is not found.
     */
    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings)
            if (mapping.accessPath().getSubpathBySignature(oldReferenceSignature) != null)
                return mapping;

        throw new NotFoundException("Mapping for reference has not been found.");
    }

    /**
     * Finds the mapping corresponding to the referred object based on the schema.
     *
     * @param mappings The list of mappings to search.
     * @param schema The schema category containing the objects and morphisms.
     * @return The mapping corresponding to the referred object.
     * @throws NotFoundException if the referred mapping is not found.
     */
    private Mapping findReferredMapping(List<Mapping> mappings, SchemaCategory schema) {
        // 1) in the schema find the signature where key is dom or cod
        // 2) check in which mapping this signature appears, it should appear in exactly one
        Signature referredSignature = findReferredSignature(schema);
        for (Mapping mapping : mappings)
            if (mapping.accessPath().getSubpathBySignature(referredSignature) != null)
                return mapping;

        throw new NotFoundException("Mapping for referred with signature " + referredSignature + " has not been found.");
    }

    /**
     * Finds the signature associated with the referred object in the schema.
     *
     * @param schema The schema category to search within.
     * @return The signature associated with the referred object.
     * @throws NotFoundException if the signature is not found.
     */
    private Signature findReferredSignature(SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if ((morphism.dom().key().equals(data.referredKey) || morphism.cod().key().equals(data.referredKey)) && !morphism.signature().equals(newIndexSignature)) {
                final var comparison = referenceIsArray ? newReferenceSignature.dual() : newReferenceSignature;
                if (!morphism.signature().equals(comparison))
                    return morphism.signature();
            }
        }
        throw new NotFoundException("Signature for referred object has not been found");
    }

        /**
     * Creates a cleaned mapping by removing unwanted subpaths.
     *
     * @param mapping The original mapping to clean.
     * @return The cleaned mapping.
     * @throws Exception
     */
    private Mapping createCleanedMapping(Mapping mapping) {
        ComplexProperty cleanedComplexProperty = cleanComplexProperty(mapping);
        return new Mapping(newSchema, mapping.rootObject().key(), mapping.kindName(), cleanedComplexProperty, mapping.primaryKey());
    }

    /**
     * Cleans the complex property by removing subpaths associated with old signatures.
     *
     * @param mapping The mapping containing the complex property to clean.
     * @return The cleaned complex property.
     * @throws Exception
     */
    private ComplexProperty cleanComplexProperty(Mapping mapping) {
        ComplexProperty complexProperty = mapping.accessPath();
        Signature oldSignature = oldReferenceSignature;
        AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(oldSignature);
        ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);

        return adjustPKComplexProperty(cleanedComplexProperty, accessPathToDelete);
    }

    private ComplexProperty adjustPKComplexProperty(ComplexProperty complexProperty, AccessPath accessPathToDelete) {
        SimpleProperty pkProperty = new SimpleProperty(accessPathToDelete.name(), newReferenceSignature);

        List<AccessPath> newAccessPaths = new ArrayList<>(complexProperty.subpaths());
        newAccessPaths.add(pkProperty);
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), newAccessPaths);
    }

}
