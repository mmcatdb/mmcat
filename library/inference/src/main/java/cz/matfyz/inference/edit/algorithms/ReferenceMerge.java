package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.exception.ObjexNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;
import cz.matfyz.inference.edit.InferenceEditorUtils.KeysAndSignatures;
import cz.matfyz.inference.schemaconversion.RSDToAccessTreeConverter;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code ReferenceMerge} class implements an algorithm for merging references
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides functionality
 * to modify objexes and mappings based on reference merging rules.
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
    private BaseSignature referenceSignature;
    private BaseSignature parentReferenceSignature;

    private final AccessPathBuilder b = new AccessPathBuilder();

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

        SchemaObjex dom = newSchema.getObjex(referenceParentKey);
        final SchemaObjex cod = newSchema.getObjex(data.referencedKey);

        final var toDelete = new KeysAndSignatures();

        if (referenceIsArray) {
            this.parentReferenceSignature = InferenceEditorUtils.findSignatureBetween(newSchema.getObjex(data.referencingKey), dom);
            dom = newSchema.getObjex(data.referencingKey);
            newMetadata.setObjex(dom, new MetadataObjex(ARRAY_LABEL, newMetadata.getObjex(dom).position));

            this.referenceSignature = findReferenceSignatureWithValueKey(toDelete, data.referencingKey);
        }
        else {
            this.referenceSignature = InferenceEditorUtils.findSignatureBetween(dom, newSchema.getObjex(data.referencingKey));
            toDelete.add(data.referencingKey);
        }

        InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, dom, cod, this.referenceSignature);

        InferenceEditorUtils.removeMorphismsAndObjexes(newSchema, toDelete);
    }

    private boolean isReferenceArray(SchemaCategory schema, MetadataCategory metadata) {
        for (final var morphism : schema.getObjex(data.referencingKey).from()) {
            if (metadata.getObjex(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL))
                return true;
        }

        return false;
    }

    private Key getReferenceParentKey(SchemaCategory schema, MetadataCategory metadata) {
        if (referenceIsArray) {
            for (final var morphism : schema.getObjex(data.referencingKey).from()) {
                if (
                    !metadata.getObjex(morphism.cod()).label.equals(RSDToAccessTreeConverter.INDEX_LABEL) ||
                    !metadata.getObjex(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL)
                )
                    return morphism.cod().key();
            }

            throw ObjexNotFoundException.withMessage("Parent key has not been found");
        }


        for (final var morphism : schema.getObjex(data.referencingKey).to())
            return morphism.dom().key();

        throw ObjexNotFoundException.withMessage("Parent key has not been found");
    }

    private BaseSignature findReferenceSignatureWithValueKey(KeysAndSignatures toDelete, Key key) {
        final Key valueKey = getValueKey(key);
        toDelete.add(valueKey);
        return InferenceEditorUtils.findSignatureBetween(newSchema.getObjex(data.referencingKey), newSchema.getObjex(valueKey));
    }

    private Key getValueKey(Key key) {
        for (final var morphism : newSchema.getObjex(key).from()) {
            if (newMetadata.getObjex(morphism.cod()).label.equals(RSDToAccessTreeConverter.VALUE_LABEL))
                return morphism.cod().key();
        }

        throw ObjexNotFoundException.withMessage("Index key has not been found");
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

        throw MorphismNotFoundException.withMessage("Mapping for reference has not been found.");
    }

    private Mapping createAdjustedMapping(Mapping mapping) {
        final ComplexProperty cleanedComplexProperty = adjustComplexProperty(mapping);
        return mapping.withSchemaAndPath(newSchema, cleanedComplexProperty);
    }

    private ComplexProperty adjustComplexProperty(Mapping mapping) {
        final ComplexProperty complexProperty = mapping.accessPath();

        if (referenceIsArray) {
            // get the part where it has array reference
            final ComplexProperty referenceComplexProperty = (ComplexProperty) complexProperty.getSubpathBySignature(this.parentReferenceSignature.dual());
            final ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(referenceComplexProperty);

            final ComplexProperty adjustedReferenceComplexProperty = adjustReferenceComplexProperty(referenceComplexProperty, ARRAY_LABEL);

            return adjustComplexProperty(cleanedComplexProperty, adjustedReferenceComplexProperty);
        }
        else {
           return adjustReferenceComplexProperty(complexProperty, complexProperty.name().toString());
        }
    }

    private ComplexProperty adjustReferenceComplexProperty(ComplexProperty complexProperty, String label) {
        final AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(this.referenceSignature);
        final ComplexProperty adjustedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);

        final List<AccessPath> accessPaths = new ArrayList<>(adjustedComplexProperty.subpaths());
        accessPaths.add(createNewReferenceProperty());

        return b.complex(label, complexProperty.signature(), accessPaths.toArray(AccessPath[]::new));
    }

    private SimpleProperty createNewReferenceProperty() {
        return b.simple(newMetadata.getObjex(data.referencedKey).label, this.referenceSignature);
    }

    private ComplexProperty adjustComplexProperty(ComplexProperty complexProperty, ComplexProperty referenceComplexProperty) {
        final List<AccessPath> accessPaths = new ArrayList<>(complexProperty.subpaths());
        accessPaths.add(referenceComplexProperty);
        return b.complex(complexProperty.name(), complexProperty.signature(), accessPaths.toArray(AccessPath[]::new));
    }

}
