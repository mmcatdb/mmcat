package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.Name.StringName;
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
import cz.matfyz.inference.schemaconversion.RSDToAccessTreeConverter;

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

        @JsonProperty("referencingKey")
        @Nullable public Key referencingKey;

        @JsonProperty("referencedKey")
        @Nullable public Key referencedKey;

        @JsonProperty("candidate")
        @Nullable public ReferenceCandidate candidate;

        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("referencingKey") Key referencingKey,
                @JsonProperty("referencedKey") Key referencedKey,
                @JsonProperty("candidate") ReferenceCandidate candidate) {
            setId(id);
            setActive(isActive);
            this.referencingKey = referencingKey;
            this.referencedKey = referencedKey;
            this.candidate = candidate;
        }

        public Data() {
            setId(null);
            setActive(false);
            this.referencingKey = null;
            this.referencedKey = null;
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
    private static final String ARRAY_LABEL = "Array";

    private final Data data;

    private boolean referenceIsArray;
    private Signature referenceSignature;
    private Signature parentReferenceSignature;

    public ReferenceMerge(Data data) {
        this.data = data;
    }

    /*
     * Applies the primary key merging algorithm to the schema category.
     * It accounts for the reference being both a single object as well as an array.
     * If the reference is an array, it assumes that the elements of the array are simple elements. Meaning the reference has only value and index outgoing morphisms.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.referencingKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referencing());
            data.referencedKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referred());
        }

        referenceIsArray = isReferenceArray(newSchema, newMetadata);

        final Key referenceParentKey = getReferenceParentKey(newSchema, newMetadata);

        SchemaObject dom = newSchema.getObject(referenceParentKey);
        final SchemaObject cod = newSchema.getObject(data.referencedKey);

        if (referenceIsArray) {
            this.parentReferenceSignature = InferenceEditorUtils.findSignatureBetween(newSchema, newSchema.getObject(data.referencingKey), dom);
            dom = newSchema.getObject(data.referencingKey);

            InferenceEditorUtils.updateMetadataObjectsLabel(dom, newMetadata, ARRAY_LABEL);

            this.referenceSignature = findReferenceSignatureWithValueKey(data.referencingKey);
        } else {
            this.referenceSignature = InferenceEditorUtils.findSignatureBetween(newSchema, dom, newSchema.getObject(data.referencingKey));
            keysToDelete.add(data.referencingKey);
        }
        InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod, this.referenceSignature);

        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
    }

    private boolean isReferenceArray(SchemaCategory schema, MetadataCategory metadata) {
        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().key().equals(data.referencingKey) && metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL))
                return true;
        }
        return false;
    }

    private Key getReferenceParentKey(SchemaCategory schema, MetadataCategory metadata) {
        if (referenceIsArray) {
            for (final SchemaMorphism morphism : schema.allMorphisms()) {
                if (
                    morphism.dom().key().equals(data.referencingKey) && (
                        !metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL) ||
                        !metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL)
                    )
                )
                    return morphism.cod().key();
            }

            throw new NotFoundException("Parent key has not been found");
        }

        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.cod().key().equals(data.referencingKey))
                return morphism.dom().key();
        }

        throw new NotFoundException("Parent key has not been found");
    }

    private Signature findReferenceSignatureWithValueKey(Key key) {
        final Key valueKey = getValueKey(key);
        keysToDelete.add(valueKey);
        return InferenceEditorUtils.findSignatureBetween(newSchema, newSchema.getObject(data.referencingKey), newSchema.getObject(valueKey));
    }

    private Key getValueKey(Key key) {
        for (final SchemaMorphism morphism : newSchema.allMorphisms())
            if (morphism.dom().key().equals(key) && newMetadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL))
                return morphism.cod().key();

        throw new NotFoundException("Index key has not been found");
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    // adjusted for array, because for now we dont support indexing
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        // arrays will be dealt with once indexing in enabled
        if (referenceIsArray)
            return mappings;

        final Mapping referenceMapping = findReferenceMapping(mappings);

        final Mapping cleanedReferenceMapping = createAdjustedMapping(referenceMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping), Arrays.asList(cleanedReferenceMapping));
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (final Mapping mapping : mappings)
            if (
                mapping.accessPath().getSubpathBySignature(this.referenceSignature) != null ||
                mapping.accessPath().getSubpathBySignature(this.parentReferenceSignature.dual()) != null
            )
                return mapping;

        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping createAdjustedMapping(Mapping mapping) {
        final ComplexProperty cleanedComplexProperty = adjustComplexProperty(mapping);
        return mapping.withSchema(newSchema, cleanedComplexProperty, mapping.primaryKey());
    }

    private ComplexProperty adjustComplexProperty(Mapping mapping) {
        final ComplexProperty complexProperty = mapping.accessPath();

        if (referenceIsArray) {
            // get the part where it has array reference
            final ComplexProperty referenceComplexProperty = (ComplexProperty) complexProperty.getSubpathBySignature(this.parentReferenceSignature.dual());
            final ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(referenceComplexProperty);

            final ComplexProperty adjustedReferenceComplexProperty = adjustReferenceComplexProperty(referenceComplexProperty, ARRAY_LABEL);

            return adjustComplexProperty(cleanedComplexProperty, adjustedReferenceComplexProperty);
        } else {
           return adjustReferenceComplexProperty(complexProperty, complexProperty.name().toString());
        }
    }

    private ComplexProperty adjustReferenceComplexProperty(ComplexProperty complexProperty, String label) {
        final AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(this.referenceSignature);
        final ComplexProperty adjustedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);

        final List<AccessPath> accessPaths = new ArrayList<>(adjustedComplexProperty.subpaths());
        accessPaths.add(createNewReferenceProperty());

        return new ComplexProperty(new StringName(label), complexProperty.signature(), accessPaths);
    }

    private SimpleProperty createNewReferenceProperty() {
        return new SimpleProperty(new StringName(InferenceEditorUtils.findLabelFromKey(data.referencedKey, newMetadata)), this.referenceSignature);
    }

    private ComplexProperty adjustComplexProperty(ComplexProperty complexProperty, ComplexProperty referenceComplexProperty) {
        final List<AccessPath> accessPaths = new ArrayList<>(complexProperty.subpaths());
        accessPaths.add(referenceComplexProperty);
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), accessPaths);
    }

}
