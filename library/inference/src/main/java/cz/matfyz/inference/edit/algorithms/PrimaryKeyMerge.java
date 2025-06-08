package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.hadoop.yarn.webapp.NotFoundException;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code PrimaryKeyMerge} class implements an algorithm for merging primary keys
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides
 * functionality to modify objexes and mappings based on primary key rules.
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

    public PrimaryKeyMerge(Data data) {
        this.data = data;
    }

    /**
     * Applies the primary key merging algorithm to the schema category.
     * It modifies the schema based on primary key rules and removes unnecessary objexes and morphisms.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Primary Key Edit on Schema Category...");

        if (data.candidate != null) {
            data.primaryKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.hierarchicalName());
            data.primaryKeyIdentified = findPrimaryKeyIdentifiedFromCandidate(newSchema);
        }

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchema);
        final Signature primaryKeySignature = findPKSignature();

        final SchemaObjex pkRootObjex = newSchema.getObjex(primaryKeyRoot);

        if (primaryKeyRoot.equals(data.primaryKeyIdentified)) { // update PK identification for the PK's parent
            final SchemaObjex updatedPkRootObjex = updateSchemaObjexIds(pkRootObjex, primaryKeySignature);
            InferenceEditorUtils.updateObjexes(newSchema, newMetadata, pkRootObjex, updatedPkRootObjex);
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

    private SchemaObjex updateSchemaObjexIds(SchemaObjex schemaObjex, Signature signature) {
        final SortedSet<SignatureId> signatureSet = new TreeSet<>(Set.of(new SignatureId(signature)));
        final ObjexIds updatedIds = schemaObjex.ids().isSignatures()
            ? new ObjexIds(addSignatureToSet(schemaObjex.ids(), signature))
            : new ObjexIds(signatureSet);

        return new SchemaObjex(schemaObjex.key(), updatedIds);
    }

    private SortedSet<SignatureId> addSignatureToSet(ObjexIds ids, Signature signature) {
        final SortedSet<SignatureId> signatureIds = new TreeSet<>(ids.toSignatureIds());
        signatureIds.add(new SignatureId(signature));
        return signatureIds;
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Primary Key Edit on Mapping...");

        final Mapping primaryKeyMapping = findPrimaryKeyMapping(mappings);
        final Mapping updatedPrimaryKeyMapping = updatePrimaryKeyMapping(primaryKeyMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(primaryKeyMapping), Arrays.asList(updatedPrimaryKeyMapping));
    }

    private Mapping findPrimaryKeyMapping(List<Mapping> mappings) {
        for (final Mapping mapping : mappings) {
            if (mapping.rootObjex().key().equals(primaryKeyRoot))
                return mapping;
        }
        throw new NotFoundException("Mapping for objex identified with PK has not been found.");
    }

    private Mapping updatePrimaryKeyMapping(Mapping mapping) {
        return Mapping.create(mapping.datasource(), mapping.kindName(), newSchema, mapping.rootObjex().key(), mapping.accessPath());
    }

}
