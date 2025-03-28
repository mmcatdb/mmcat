package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.yarn.webapp.NotFoundException;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code PrimaryKeyMerge} class implements an algorithm for merging primary keys
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides
 * functionality to modify schema objects and mappings based on primary key rules.
 */
public class PrimaryKeyMerge extends InferenceEditAlgorithm {

    public static class Data extends InferenceEdit {

        @JsonProperty("primaryKey")
        @Nullable public Key primaryKey;

        @JsonProperty("primaryKeyIdentified")
        @Nullable public Key primaryKeyIdentified;

        @JsonProperty("candidate")
        @Nullable public PrimaryKeyCandidate candidate;

        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("primaryKey") Key primaryKey,
                @JsonProperty("primaryKeyIdentified") Key primaryKeyIdentified,
                @JsonProperty("candidate") PrimaryKeyCandidate candidate) {
            setId(id);
            setActive(isActive);
            this.primaryKey = primaryKey;
            this.primaryKeyIdentified = primaryKeyIdentified;
            this.candidate = candidate;
        }

        public Data() {
            setId(null);
            setActive(false);
            this.primaryKey = null;
            this.primaryKeyIdentified = null;
            this.candidate = null;
        }

        /**
         * Creates an instance of the {@code PrimaryKeyMerge} algorithm.
         */
        @Override public PrimaryKeyMerge createAlgorithm() {
            return new PrimaryKeyMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMerge.class.getName());

    private final Data data;

    private Key primaryKeyRoot;
    private Pair<Key, Signature> newSignaturePair;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    public PrimaryKeyMerge(Data data) {
        this.data = data;
    }

    /**
     * Applies the primary key merging algorithm to the schema category.
     * It modifies the schema based on primary key rules and removes unnecessary objects and morphisms.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Primary Key Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.primaryKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.hierarchicalName());
            data.primaryKeyIdentified = findPrimaryKeyIdentifiedFromCandidate(newSchema);
        }

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchema);
        final Signature primaryKeySignature = findPKSignature();

        final SchemaObject pkRootObject = newSchema.getObject(primaryKeyRoot);

        if (primaryKeyRoot.equals(data.primaryKeyIdentified)) { // update PK identification for the PK's parent
            final SchemaObject updatedPkRootObject = updateSchemaObjectIds(pkRootObject, primaryKeySignature);

            InferenceEditorUtils.updateObjexes(newSchema, newMetadata, pkRootObject, updatedPkRootObject);
        } else {
            final SchemaObject pkIdentifiedObject = newSchema.getObject(data.primaryKeyIdentified);

            final String primaryKeyLabel = newMetadata.getObject(data.primaryKey).label;

            this.newSignaturePair = createNewMorphism(pkIdentifiedObject, pkRootObject);

            findObjectsAndMorphismsToDelete(primaryKeyLabel);
            InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
        }
    }

    private Key findPrimaryKeyIdentifiedFromCandidate(SchemaCategory schema) {
        for (final SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.cod().key().equals(data.primaryKey))
                return morphism.dom().key();

        throw new NotFoundException("Primary Key Identified has not been found.");
    }

    private Key findPrimaryKeyRoot(SchemaCategory schema) {
        for (final SchemaMorphism morphism : schema.allMorphisms())
            if (morphism.cod().key().equals(data.primaryKey))
                return morphism.dom().key();

        throw new NotFoundException("Primary Key Root has not been found");
    }

    private Signature findPKSignature() {
        for (final SchemaMorphism morphism : newSchema.allMorphisms())
            if (morphism.dom().key().equals(this.primaryKeyRoot) && morphism.cod().key().equals(data.primaryKey))
                return morphism.signature();

        throw new NotFoundException("Primary Key Signature has not been found");
    }

    private SchemaObject updateSchemaObjectIds(SchemaObject schemaObject, Signature signature) {
        final SortedSet<SignatureId> signatureSet = new TreeSet<>(Set.of(new SignatureId(signature)));
        final ObjectIds updatedIds = schemaObject.ids().isSignatures()
            ? new ObjectIds(addSignatureToSet(schemaObject.ids(), signature))
            : new ObjectIds(signatureSet);

        return new SchemaObject(schemaObject.key(), updatedIds, updatedIds.generateDefaultSuperId());
    }

    private SortedSet<SignatureId> addSignatureToSet(ObjectIds ids, Signature signature) {
        final SortedSet<SignatureId> signatureIds = new TreeSet<>(ids.toSignatureIds());
        signatureIds.add(new SignatureId(signature));
        return signatureIds;
    }

    private Pair<Key, Signature> createNewMorphism(SchemaObject dom, SchemaObject cod) {
        final Signature newSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod);
        return Pair.of(data.primaryKeyIdentified, newSignature);
    }

    private void findObjectsAndMorphismsToDelete(String primaryKeyLabel) {
        for (final SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (
                morphism.dom().key().equals(data.primaryKeyIdentified) &&
                newMetadata.getObject(morphism.cod().key()).label.equals(primaryKeyLabel)
            ) {
                keysToDelete.add(morphism.cod().key());
                signaturesToDelete.add(morphism.signature());
                oldSignatureMap.put(morphism.dom().key(), morphism.signature());
            }
        }
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Primary Key Merge Edit on Mapping...");

        if (!primaryKeyRoot.equals(data.primaryKeyIdentified)) {
            final Mapping primaryKeyIdentifiedMapping = findPrimaryKeyIdentifiedMapping(mappings);
            final Mapping cleanedPrimaryKeyIdentifiedMapping = createCleanedMapping(findPrimaryKeyIdentifiedMapping(mappings));

            return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(primaryKeyIdentifiedMapping), Arrays.asList(cleanedPrimaryKeyIdentifiedMapping));
        }
        // updates the primary key, if the ids where updated
        final Mapping primaryKeyMapping = findPrimaryKeyMapping(mappings);
        final Mapping updatedPrimaryKeyMapping = updatePrimaryKeyMapping(primaryKeyMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(primaryKeyMapping), Arrays.asList(updatedPrimaryKeyMapping));
    }

    private Mapping findPrimaryKeyMapping(List<Mapping> mappings) {
        for (final Mapping mapping : mappings) {
            if (mapping.rootObject().key().equals(primaryKeyRoot))
                return mapping;
        }
        throw new NotFoundException("Mapping for object identified with PK has not been found.");
    }

    private Mapping findPrimaryKeyIdentifiedMapping(List<Mapping> mappings) {
        for (final Mapping mapping : mappings) {
            if (mapping.rootObject().key().equals(data.primaryKeyIdentified))
                return mapping;
        }
        throw new NotFoundException("Mapping for object identified with PK has not been found.");
    }

    private Mapping createCleanedMapping(Mapping mapping) {
        final ComplexProperty cleanedComplexProperty = cleanComplexProperty(mapping);
        return Mapping.create(mapping.datasource(), mapping.kindName(), newSchema, mapping.rootObject().key(), cleanedComplexProperty);
    }

    private ComplexProperty cleanComplexProperty(Mapping mapping) {
        final ComplexProperty complexProperty = mapping.accessPath();

        if (oldSignatureMap.isEmpty())
            return complexProperty;

        final Signature oldSignature = oldSignatureMap.get(mapping.rootObject().key());
        final AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(oldSignature);
        final ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);
        return adjustPKComplexProperty(cleanedComplexProperty, accessPathToDelete);
    }

    private ComplexProperty adjustPKComplexProperty(ComplexProperty complexProperty, AccessPath accessPathToDelete) {
        final Signature newPKSignature = Signature.concatenate(this.newSignaturePair.getValue(), findPKSignature());
        final SimpleProperty pkProperty = new SimpleProperty(accessPathToDelete.name(), newPKSignature);

        final List<AccessPath> accessPaths = new ArrayList<>(complexProperty.subpaths());
        accessPaths.add(pkProperty);
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), accessPaths);
    }

    private Mapping updatePrimaryKeyMapping(Mapping mapping) {
        return Mapping.create(mapping.datasource(), mapping.kindName(), newSchema, mapping.rootObject().key(), mapping.accessPath());
    }

}
