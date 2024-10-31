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
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code ReferenceMerge} class implements an algorithm for merging references
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides functionality
 * to modify schema objects and mappings based on reference merging rules.
 */
public class ReferenceMerge extends InferenceEditAlgorithm {

    public static class Data extends InferenceEdit {

        @JsonProperty("referenceKey")
        @Nullable public Key referenceKey;

        @JsonProperty("referredKey")
        @Nullable public Key referredKey;

        @JsonProperty("candidate")
        @Nullable public ReferenceCandidate candidate;

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

        public Data() {
            setId(null);
            setActive(false);
            this.referenceKey = null;
            this.referredKey = null;
            this.candidate = null;
        }

        /**
         * Creates an instance of the {@code ReferenceMerge} algorithm.
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

    public ReferenceMerge(Data data) {
        this.data = data;
    }

    /*
     * Applies the primary key merging algorithm to the schema category.
     * Assumption: when there is a reference and it is an array object, it has 2 outgoing morphisms: one for _index and one for the original parent node.
     * If it is not an array, it has only one incoming morphism from the root.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.referenceKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referencing());
            data.referredKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referred());
        }

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

    private boolean isReferenceArray(SchemaCategory schema, MetadataCategory metadata) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().key().equals(data.referenceKey) && metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL)) {
                return true;
            }
        }
        return false;
    }

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

    private Key getIndexKey(SchemaCategory schema, MetadataCategory metadata, Key key) {
        for (SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.dom().key().equals(key) && metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL))
                return morphism.cod().key();

        throw new NotFoundException("Index key has not been found");
    }

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
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        /*
        if (referenceIsArray) {
            this.oldReferenceSignature = this.oldReferenceSignature.dual();
            this.newReferenceSignature = this.newReferenceSignature.dual();
        }*/

        Mapping referenceMapping = findReferenceMapping(mappings);

        Mapping cleanedReferenceMapping = createCleanedMapping(referenceMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping), Arrays.asList(cleanedReferenceMapping));
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings)
            if (mapping.accessPath().getSubpathBySignature(oldReferenceSignature) != null)
                return mapping;

        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping findReferredMapping(List<Mapping> mappings, SchemaCategory schema) {
        // 1) in the schema find the signature where key is dom or cod
        // 2) check in which mapping this signature appears, it should appear in exactly one
        Signature referredSignature = findReferredSignature(schema);
        for (Mapping mapping : mappings)
            if (mapping.accessPath().getSubpathBySignature(referredSignature) != null)
                return mapping;

        throw new NotFoundException("Mapping for referred with signature " + referredSignature + " has not been found.");
    }

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

    private Mapping createCleanedMapping(Mapping mapping) {
        ComplexProperty cleanedComplexProperty = cleanComplexProperty(mapping);
        return new Mapping(newSchema, mapping.rootObject().key(), mapping.kindName(), cleanedComplexProperty, mapping.primaryKey());
    }

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
