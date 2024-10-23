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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code PrimaryKeyMerge} class implements an algorithm for merging primary keys
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides
 * functionality to modify schema objects and mappings based on primary key rules.
 */
public class PrimaryKeyMerge extends InferenceEditAlgorithm {

    /**
     * Data class representing the parameters and state needed for the primary key merge.
     */
    public static class Data extends InferenceEdit {

        @JsonProperty("primaryKey")
        private Key primaryKey;

        @JsonProperty("primaryKeyIdentified")
        private Key primaryKeyIdentified;

        @JsonProperty("candidate")
        private PrimaryKeyCandidate candidate;

        /**
         * Constructs a {@code Data} instance with specified parameters.
         *
         * @param id The ID of the edit.
         * @param isActive The active status of the edit.
         * @param primaryKey The primary key involved in the merge.
         * @param primaryKeyIdentified The identified primary key for the merge.
         * @param candidate The candidate primary key information.
         */
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

        /**
         * Default constructor initializing data with default values.
         */
        public Data() {
            setId(null);
            setActive(false);
            this.primaryKey = null;
            this.primaryKeyIdentified = null;
            this.candidate = null;
        }

        /**
         * Gets the primary key involved in the merge.
         *
         * @return The primary key.
         */
        public Key getPrimaryKey() {
            return primaryKey;
        }

        /**
         * Gets the identified primary key for the merge.
         *
         * @return The identified primary key.
         */
        public Key getPrimaryKeyIdentified() {
            return primaryKeyIdentified;
        }

        /**
         * Gets the candidate primary key information.
         *
         * @return The primary key candidate.
         */
        public PrimaryKeyCandidate getCandidate() {
            return candidate;
        }

        /**
         * Creates an instance of the {@code PrimaryKeyMerge} algorithm.
         *
         * @return A new instance of {@code PrimaryKeyMerge}.
         */
        @Override
        public PrimaryKeyMerge createAlgorithm() {
            return new PrimaryKeyMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMerge.class.getName());

    private final Data data;

    private Key primaryKeyRoot;
    private Signature primaryKeySignature;
    private Pair<Key, Signature> newSignaturePair;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    /**
     * Constructs a {@code PrimaryKeyMerge} instance with the specified data.
     *
     * @param data The data model containing primary key information and merge settings.
     */
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
        this.primaryKeySignature = findPKSignature();

        if (!primaryKeyRoot.equals(data.primaryKeyIdentified)) { // TODO: if not, set the identification?, similarly in mapping

            SchemaObject cod = newSchema.getObject(primaryKeyRoot);
            SchemaObject dom = newSchema.getObject(data.primaryKeyIdentified);

            final String primaryKeyLabel = newMetadata.getObject(data.primaryKey).label;

            this.newSignaturePair = createNewMorphism(dom, cod);

            SchemaObject updatedCod = updateSchemaObjectIds(cod, this.primaryKeySignature);
            SchemaObject updatedDom = updateSchemaObjectIds(dom, Signature.concatenate(this.newSignaturePair.getValue(), this.primaryKeySignature));

            InferenceEditorUtils.updateObjects(newSchema, newMetadata, cod, updatedCod);
            InferenceEditorUtils.updateObjects(newSchema, newMetadata, dom, updatedDom);

            findObjectsAndMorphismsToDelete(primaryKeyLabel);
            InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
        }
    }

    /**
     * Finds the primary key identified from the candidate within the schema.
     *
     * @param schema The schema category to search within.
     * @return The key of the identified primary key.
     * @throws NotFoundException if the primary key identified is not found.
     */
    private Key findPrimaryKeyIdentifiedFromCandidate(SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (morphism.cod().key().equals(data.primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Identified has not been found.");
    }

    /**
     * Finds the root of the primary key within the schema.
     *
     * @param schema The schema category to search within.
     * @return The key of the primary key root.
     * @throws NotFoundException if the primary key root is not found.
     */
    private Key findPrimaryKeyRoot(SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            // based on the assumption
            if (morphism.cod().key().equals(data.primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Root has not been found");
    }

    private Signature findPKSignature() {
        for (SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (morphism.dom().key().equals(this.primaryKeyRoot) && morphism.cod().key().equals(data.primaryKey)) {
                return morphism.signature();
            }
        }
        return null;
    }

    private SchemaObject updateSchemaObjectIds(SchemaObject schemaObject, Signature signature) {
        ObjectIds updatedIds = schemaObject.ids().isSignatures()
            ? new ObjectIds(addSignatureToSet(schemaObject.ids(), signature))
            : new ObjectIds(signature);

        return new SchemaObject(schemaObject.key(), updatedIds, schemaObject.superId());
    }

    private SortedSet<SignatureId> addSignatureToSet(ObjectIds ids, Signature signature) {
        SortedSet<SignatureId> signatureIds = new TreeSet<>(ids.toSignatureIds());
        signatureIds.add(new SignatureId(signature));
        return signatureIds;
    }

    /**
     * Creates a new morphism from the provided schema object.
     *
     * @param cod The schema object to use for creating the new morphism.
     * @return A map containing the keys and their corresponding new signatures.
     */
    private Pair<Key, Signature> createNewMorphism(SchemaObject dom, SchemaObject cod) {
        Signature newSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod);
        return Pair.of(data.primaryKeyIdentified, newSignature);
    }

    /**
     * Finds objects and morphisms to delete based on the primary key label.
     *
     * @param primaryKeyLabel The label of the primary key used to find objects and morphisms to delete.
     */
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

    /**
     * Applies the mapping edit to a list of mappings.
     * Assumes that the primary key has a unique name and all objects with this name are the same primary keys.
     *
     * @param mappings The list of mappings to edit.
     * @return The updated list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Primary Key Merge Edit on Mapping...");

        if (!primaryKeyRoot.equals(data.primaryKeyIdentified)) {
            Mapping primaryKeyIdentifiedMapping = findPrimaryKeyIdentifiedMapping(mappings);

            Mapping cleanedPrimaryKeyIdentifiedMapping = createCleanedMapping(primaryKeyIdentifiedMapping);

            return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(primaryKeyIdentifiedMapping), Arrays.asList(cleanedPrimaryKeyIdentifiedMapping));
        }

        return mappings;
    }

    /**
     * Finds the mapping corresponding to the object identified with the primary key.
     *
     * @param mappings The list of mappings to search.
     * @return The mapping corresponding to the identified primary key object.
     * @throws NotFoundException if the mapping is not found.
     */
    private Mapping findPrimaryKeyIdentifiedMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.rootObject().key().equals(data.primaryKeyIdentified)) {
                return mapping;
            }
        }
        throw new NotFoundException("Mapping for object identified with PK has not been found.");
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
        Signature oldSignature = oldSignatureMap.get(mapping.rootObject().key());
        AccessPath accessPathToDelete = complexProperty.getSubpathBySignature(oldSignature);
        ComplexProperty cleanedComplexProperty = complexProperty.minusSubpath(accessPathToDelete);

        return adjustPKComplexProperty(cleanedComplexProperty, accessPathToDelete);
    }

    private ComplexProperty adjustPKComplexProperty(ComplexProperty complexProperty, AccessPath accessPathToDelete) {
        Signature rootToPKSignature = findPKSignature();
        Signature newPKSignature = Signature.concatenate(this.newSignaturePair.getValue(), rootToPKSignature);
        SimpleProperty pkProperty = new SimpleProperty(accessPathToDelete.name(), newPKSignature);

        List<AccessPath> newAccessPaths = new ArrayList<>(complexProperty.subpaths());
        newAccessPaths.add(pkProperty);
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), newAccessPaths);
    }

}
