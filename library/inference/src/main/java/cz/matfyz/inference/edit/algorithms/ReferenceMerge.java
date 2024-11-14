package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
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
     * It account for the reference being both a single object as well as an array.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.referenceKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referencing());
            data.referredKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.referred());
        }

        this.referenceIsArray = isReferenceArray(newSchema, newMetadata);

        Key referenceParentKey = getReferenceParentKey(newSchema, newMetadata, referenceIsArray);

        SchemaObject dom = newSchema.getObject(referenceParentKey);
        SchemaObject cod = newSchema.getObject(data.referredKey);

        if (referenceIsArray) {
            this.parentReferenceSignature = InferenceEditorUtils.findSignatureBetween(newSchema, newSchema.getObject(data.referenceKey), dom);
            dom = newSchema.getObject(data.referenceKey);

            InferenceEditorUtils.updateMetadataObjectsLabel(dom, newMetadata, ARRAY_LABEL);

            this.referenceSignature = findReferenceSignatureWithValueKey(data.referenceKey);
        } else {
            this.referenceSignature = InferenceEditorUtils.findSignatureBetween(newSchema, dom, newSchema.getObject(data.referenceKey));
            keysToDelete.add(data.referenceKey);
        }
        InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod, this.referenceSignature);

        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
    }

    private boolean isReferenceArray(SchemaCategory schema, MetadataCategory metadata) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().key().equals(data.referenceKey) && metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL)) {
                return true;
            }
        }
        return false;
    }

    private Key getReferenceParentKey(SchemaCategory schema, MetadataCategory metadata, boolean isReferenceArray) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (isReferenceArray) {
                if (morphism.dom().key().equals(data.referenceKey) &&
                    (!metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL) ||
                     !metadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL)))
                    return morphism.cod().key();
            } else {
                if (morphism.cod().key().equals(data.referenceKey))
                    return morphism.dom().key();
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    private Signature findReferenceSignatureWithValueKey(Key key) {
        Key valueKey = getValueKey(key);
        keysToDelete.add(valueKey);
        return InferenceEditorUtils.findSignatureBetween(newSchema, newSchema.getObject(data.referenceKey), newSchema.getObject(valueKey));
    }

    private Key getValueKey(Key key) {
        for (SchemaMorphism morphism : newSchema.allMorphisms())
            if (morphism.dom().key().equals(key) && newMetadata.getObject(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL))
                return morphism.cod().key();

        throw new NotFoundException("Index key has not been found");
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        Mapping referenceMapping = findReferenceMapping(mappings);

        Mapping cleanedReferenceMapping = createAdjustedMapping(referenceMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping), Arrays.asList(cleanedReferenceMapping));
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings)
            if (mapping.accessPath().getSubpathBySignature(this.referenceSignature) != null ||
                mapping.accessPath().getSubpathBySignature(this.parentReferenceSignature.dual()) != null)
                return mapping;

        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping createAdjustedMapping(Mapping mapping) {
        ComplexProperty cleanedComplexProperty = adjustComplexProperty(mapping);
        return mapping.withSchema(newSchema, cleanedComplexProperty, mapping.primaryKey());
    }

    private ComplexProperty adjustComplexProperty(Mapping mapping) {
        ComplexProperty complexProperty = mapping.accessPath();

        if (referenceIsArray) {
            // get the part where it has array reference
            ComplexProperty referenceComplexProperty = (ComplexProperty) complexProperty.getSubpathBySignature(this.parentReferenceSignature.dual());
            ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(referenceComplexProperty);

            ComplexProperty adjustedReferenceComplexProperty = adjustReferenceComplexProperty(referenceComplexProperty, ARRAY_LABEL);

            return adjustComplexProperty(cleanedComplexProperty, adjustedReferenceComplexProperty);
        } else {
           return adjustReferenceComplexProperty(complexProperty, complexProperty.name().toString());
        }
    }

    private ComplexProperty adjustReferenceComplexProperty(ComplexProperty complexProperty, String label) {
        AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(this.referenceSignature);
        ComplexProperty adjustedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);

        List<AccessPath> accessPaths = new ArrayList<>(adjustedComplexProperty.subpaths());
        accessPaths.add(createNewReferenceProperty());

        return new ComplexProperty(new StaticName(label), complexProperty.signature(), accessPaths);
    }

    private SimpleProperty createNewReferenceProperty() {
        return new SimpleProperty(new StaticName(InferenceEditorUtils.findLabelFromKey(data.referredKey, newMetadata)), this.referenceSignature);
    }

    private ComplexProperty adjustComplexProperty(ComplexProperty complexProperty, ComplexProperty referenceComplexProperty) {
        List<AccessPath> accessPaths = new ArrayList<>(complexProperty.subpaths());
        accessPaths.add(referenceComplexProperty);
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), accessPaths);
    }

}
