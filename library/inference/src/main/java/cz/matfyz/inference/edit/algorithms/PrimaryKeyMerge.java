package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimaryKeyMerge extends InferenceEditAlgorithm {

    public static class Data extends InferenceEdit {

        @JsonProperty("primaryKey")
        private Key primaryKey;

        @JsonProperty("primaryKeyIdentified")
        private Key primaryKeyIdentified;

        @JsonProperty("candidate")
        private PrimaryKeyCandidate candidate;

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

        public Key getPrimaryKey() {
            return primaryKey;
        }

        public Key getPrimaryKeyIdentified() {
            return primaryKeyIdentified;
        }

        public PrimaryKeyCandidate getCandidate() {
            return candidate;
        }

        @Override
        public PrimaryKeyMerge createAlgorithm() {
            return new PrimaryKeyMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMerge.class.getName());

    private final Data data;

    private Key primaryKeyRoot;
    private Map<Key, Signature> newSignatureMap;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    public PrimaryKeyMerge(Data data) {
        this.data = data;
    }

    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Primary Key Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.primaryKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.hierarchicalName());
            data.primaryKeyIdentified = findPrimaryKeyIdentifiedFromCandidate(newSchema);
        }

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchema);

        if (!primaryKeyRoot.equals(data.primaryKeyIdentified)) { // TODO: if not, set the identification?, similarly in mapping
            SchemaObject cod = newSchema.getObject(primaryKeyRoot);

            final String primaryKeyLabel = newMetadata.getObject(data.primaryKey).label;

            this.newSignatureMap = createNewMorphism(cod);

            findObjectsAndMorphismsToDelete(primaryKeyLabel);
            InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
        }
    }

    private Key findPrimaryKeyIdentifiedFromCandidate(SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.cod().key().equals(data.primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Identified has not been found.");
    }

    private Key findPrimaryKeyRoot(SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            // based on the assumption
            if (morphism.cod().key().equals(data.primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Root has not been found");
    }

    private Map<Key, Signature> createNewMorphism(SchemaObject cod) {
        Map<Key, Signature> signatureMap = new HashMap<>();
        Signature newSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(data.primaryKeyIdentified), cod);
        signatureMap.put(data.primaryKeyIdentified, newSignature);
        return signatureMap;
    }

    private void findObjectsAndMorphismsToDelete(String primaryKeyLabel) {
        for (SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (morphism.dom().key().equals(data.primaryKeyIdentified) &&
                newMetadata.getObject(morphism.cod().key()).label.equals(primaryKeyLabel)) {

                keysToDelete.add(morphism.cod().key());
                signaturesToDelete.add(morphism.signature());
                oldSignatureMap.put(morphism.dom().key(), morphism.signature());
            }
        }
    }

    // TODO: maybe I actually keep the PKs in the mapping --> but would it correspond w/ the schema?
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        /*
        * Assumption: the primary key has a unique name. All the objects w/ this
        * name are the same primary keys. The primary key is a single object
        */
        LOGGER.info("Applying Primary Key Merge Edit on Mapping...");

        if (!primaryKeyRoot.equals(data.primaryKeyIdentified)) {
            Mapping primaryKeyIdentifiedMapping = findPrimaryKeyIdentifiedMapping(mappings);

            Mapping cleanedPrimaryKeyIdentifiedMapping = createCleanedMapping(primaryKeyIdentifiedMapping);

            return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(primaryKeyIdentifiedMapping), Arrays.asList(cleanedPrimaryKeyIdentifiedMapping));
        }

        return mappings;
    }

    private Mapping findPrimaryKeyIdentifiedMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.rootObject().key().equals(data.primaryKeyIdentified)) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for object identified with PK has not been found.");
    }

    private Mapping createCleanedMapping(Mapping mapping) {
        ComplexProperty cleanedComplexProperty = cleanComplexProperty(mapping);
        return new Mapping(newSchema, mapping.rootObject().key(), mapping.kindName(), cleanedComplexProperty, mapping.primaryKey());
    }

    private ComplexProperty cleanComplexProperty(Mapping mapping) {
        ComplexProperty complexProperty = mapping.accessPath();
        Signature oldSignature = oldSignatureMap.get(mapping.rootObject().key());
        AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(oldSignature);
        return complexProperty.minusSubpath(accessPathToDelete);
    }

}
