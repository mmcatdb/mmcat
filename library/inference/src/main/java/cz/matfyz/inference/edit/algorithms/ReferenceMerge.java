package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
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

public class ReferenceMerge extends InferenceEditAlgorithm {

    public record Data(
        Key referenceKey,
        Key referredKey
    ) implements InferenceEdit {

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
     * Assumption: when there is a reference and it is an array object
     * it has 2 outgoing morphism, one for _index and one for the original parent node
     * If it is not array, it has only one ingoing morphism from the root
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Reference Merge Edit on Schema Category...");

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

    // based on the assumptions
    private Key getReferenceParentKey(SchemaCategory schema, MetadataCategory metadata, boolean isReferenceArray) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (isReferenceArray) {
                if (morphism.dom().key().equals(data.referenceKey) && !metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL)) {
                    return morphism.cod().key();
                }
            } else {
                if (morphism.cod().key().equals(data.referenceKey)) {
                    return morphism.dom().key();
                }
            }
        }
        throw new NotFoundException("Parent key has not been found");
    }

    private Key getIndexKey(SchemaCategory schema, MetadataCategory metadata, Key key) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.dom().key().equals(key) && metadata.getObject(morphism.cod()).label.equals(INDEX_LABEL)) {
                return morphism.cod().key();
            }
        }
        throw new NotFoundException("Index key has not been found");
    }

    private void findMorphismsAndObjectToDelete(SchemaCategory schema, Key indexKey) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (indexKey != null && morphism.dom().key().equals(data.referenceKey)) {
                signaturesToDelete.add(morphism.signature());
                // find the reference and index signatures
                if (morphism.cod().key().equals(indexKey)) {
                    oldIndexSignature = morphism.signature();
                } else {
                    oldReferenceSignature = morphism.signature();
                }
            } else {
                if (morphism.cod().key().equals(data.referenceKey)) {
                    signaturesToDelete.add(morphism.signature());
                    oldReferenceSignature = morphism.signature();
                }
            }
        }
        keysToDelete.add(data.referenceKey);
    }

    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Reference Merge Edit on Mapping...");

        if (referenceIsArray) {
            this.oldReferenceSignature = this.oldReferenceSignature.dual();
            this.newReferenceSignature = this.newReferenceSignature.dual();
        }

        Mapping referenceMapping = findReferenceMapping(mappings);
        Mapping referredMapping = findReferredMapping(mappings, newSchema);

        Mapping mergedMapping = createMergedMapping(referenceMapping, referredMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(referenceMapping, referredMapping), mergedMapping);
    }

    private Mapping findReferenceMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(oldReferenceSignature) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for reference has not been found.");
    }

    private Mapping findReferredMapping(List<Mapping> mappings, SchemaCategory schema) {
        // 1) in the schema find the signature where key is dom or cod
        // 2) check in which mapping this signature appears, it should appear in exactly one
        Signature referredSignature = findReferredSignature(schema);
        for (Mapping mapping : mappings) {
            if (mapping.accessPath().getSubpathBySignature(referredSignature) != null) {
                return mapping;
            }
        }
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

    private Mapping createMergedMapping(Mapping referenceMapping, Mapping referredMapping) {
        ComplexProperty mergedComplexProperty = mergeComplexProperties(referenceMapping.accessPath(), referredMapping.accessPath());
        return InferenceEditorUtils.createNewMapping(newSchema, referenceMapping, Arrays.asList(referredMapping), mergedComplexProperty);
    }

    private ComplexProperty mergeComplexProperties(ComplexProperty referenceComplexProperty, ComplexProperty referredComplexProperty) {
        List<AccessPath> newSubpaths = new ArrayList<>();
        boolean replaced = false;

        for (AccessPath subpath : referenceComplexProperty.subpaths()) {
            if (!replaced && subpath.signature().equals(oldReferenceSignature)) {
                List<AccessPath> combinedSubpaths = new ArrayList<>(referredComplexProperty.subpaths());
                if (referenceIsArray && subpath instanceof ComplexProperty currentComplexProperty) {
                    AccessPath currentSubpath = currentComplexProperty.getSubpathBySignature(oldIndexSignature); // assuming there is just _index object
                    SimpleProperty newIndexSimpleProperty = new SimpleProperty(currentSubpath.name(), newIndexSignature);
                    combinedSubpaths.add(newIndexSimpleProperty);
                }
                newSubpaths.add(new ComplexProperty(subpath.name(), newReferenceSignature, combinedSubpaths));
                replaced = true;
            } else {
                newSubpaths.add(subpath);
            }
        }
        return new ComplexProperty(referenceComplexProperty.name(), referenceComplexProperty.signature(), newSubpaths);
    }

}
