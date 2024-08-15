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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimaryKeyMerge extends InferenceEditAlgorithm {

    public static class Data implements InferenceEdit {

        private Integer id;

        @JsonProperty("isActive")
        private boolean isActive;

        @JsonProperty("primaryKey")
        private Key primaryKey;

        @JsonProperty("candidate")
        private PrimaryKeyCandidate candidate;

        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("primaryKey") Key primaryKey,
                @JsonProperty("candidate") PrimaryKeyCandidate candidate) {
            this.id = id;
            this.isActive = isActive;
            this.primaryKey = primaryKey;
            this.candidate = candidate;
        }

        public Data() {
            this.id = null;
            this.isActive = false;
            this.primaryKey = null;
            this.candidate = null;
        }

        @Override
        public PrimaryKeyMerge createAlgorithm() {
            return new PrimaryKeyMerge(this);
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public boolean isActive() {
            return isActive;
        }

        @Override
        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMerge.class.getName());

    private final Data data;

    private Key primaryKeyRoot;
    private List<Key> keysIdentifiedByPrimary;
    private Map<Key, Signature> newSignatureMap;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    public PrimaryKeyMerge(Data data) {
        this.data = data;
    }

    /*
     * Assumption: the primary key has a unique name. All the objects w/ this
     * name are the same primary keys. The primary key is a single object
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Primary Key Merge Edit on Schema Category...");

        if (data.candidate != null) {
            data.primaryKey = InferenceEditorUtils.findKeyFromName(newSchema, newMetadata, data.candidate.hierarchicalName());
        }

        System.out.println("schema in PK before: " + newSchema.allObjects());
        System.out.println("pk: " + data.primaryKey);
        System.out.println("isActive: " + data.isActive);
        System.out.println("id: " + data.id);
        this.primaryKeyRoot = findPrimaryKeyRoot(newSchema);
        SchemaObject dom = newSchema.getObject(primaryKeyRoot);

        final String primaryKeyLabel = newMetadata.getObject(data.primaryKey).label;
        this.keysIdentifiedByPrimary = findKeysIdentifiedByPrimaryKeyLabel(primaryKeyLabel);

        this.newSignatureMap = createNewMorphisms(dom);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
        System.out.println("schema in PK after: " + newSchema.allObjects());
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

    private List<Key> findKeysIdentifiedByPrimaryKeyLabel(String primaryKeyLabel) {
        final List<Key> keys = new ArrayList<>();
        for (final SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (newMetadata.getObject(morphism.cod()).label.equals(primaryKeyLabel) && !morphism.dom().key().equals(primaryKeyRoot)) {
                keys.add(morphism.dom().key());

                signaturesToDelete.add(morphism.signature());
                keysToDelete.add(morphism.cod().key());
                oldSignatureMap.put(morphism.dom().key(), morphism.signature());
            }
        }
        return keys;
    }

    private Map<Key, Signature> createNewMorphisms(SchemaObject dom) {
        Map<Key, Signature> signatureMap = new HashMap<>();
        for (Key key : keysIdentifiedByPrimary) {
            Signature newSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, newSchema.getObject(key));
            signatureMap.put(key, newSignature);
        }
        return signatureMap;
    }

    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        /*
         * Assumption: When we find object which is identified by the primary key,
         * we assume that the object is a root in its "part" of the schema
         */
        LOGGER.info("Applying Primary Key Merge Edit on Mapping...");

        List<Mapping> primaryKeyMappings = findMappingsWithPrimaryKey(mappings);

        List<Mapping> cleanedPrimaryKeyMappings = cleanPrimaryKeyMappings(primaryKeyMappings);

        return InferenceEditorUtils.updateMappings(mappings, primaryKeyMappings, cleanedPrimaryKeyMappings);
    }

    private List<Mapping> findMappingsWithPrimaryKey(List<Mapping> mappings) {
        List<Mapping> primaryKeyMappings = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (keysIdentifiedByPrimary.contains(mapping.rootObject().key())) {
                primaryKeyMappings.add(mapping);
            }
        }
        return primaryKeyMappings;
    }

    private List<Mapping> cleanPrimaryKeyMappings(List<Mapping> primaryKeyMappings) {
        List<Mapping> cleanedPrimaryKeyMappings = new ArrayList<>();
        for (Mapping mapping : primaryKeyMappings) {
            cleanedPrimaryKeyMappings.add(createCleanedMapping(mapping));
        }
        return cleanedPrimaryKeyMappings;
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
