package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.hadoop.yarn.webapp.NotFoundException;

public class PrimaryKeyMerge extends InferenceEditAlgorithm {

    public record Data(
        Key primaryKey
    ) implements InferenceEdit {

        @Override public PrimaryKeyMerge createAlgorithm() {
            return new PrimaryKeyMerge(this);
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

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchema);
        SchemaObject dom = newSchema.getObject(primaryKeyRoot);

        final String primaryKeyLabel = newMetadata.getObject(data.primaryKey).label;
        this.keysIdentifiedByPrimary = findKeysIdentifiedByPrimaryKeyLabel(primaryKeyLabel);

        this.newSignatureMap = createNewMorphisms(dom);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
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

        Mapping primaryKeyMapping = findPrimaryKeyMapping(mappings);
        List<Mapping> primaryKeyMappings = findMappingsWithPrimaryKey(mappings);

        Mapping mergedMapping = createMergedMapping(primaryKeyMapping, primaryKeyMappings);

        primaryKeyMappings.add(primaryKeyMapping);
        return InferenceEditorUtils.updateMappings(mappings, primaryKeyMappings, mergedMapping);
    }

    private Mapping findPrimaryKeyMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            if (mapping.rootObject().key().equals(primaryKeyRoot)) {
                return mapping;
            }
        }
        throw new NotFoundException("Primary Key Mapping has not been found");
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

    private Mapping createMergedMapping(Mapping primaryKeyMapping, List<Mapping> primaryKeyMappings) {
        final ComplexProperty mergedComplexProperty = mergeComplexProperties(primaryKeyMapping, primaryKeyMappings);
        return InferenceEditorUtils.createNewMapping(newSchema, primaryKeyMapping, primaryKeyMappings, mergedComplexProperty);
    }

    private ComplexProperty mergeComplexProperties(Mapping primaryKeyMapping, List<Mapping> primaryKeyMappings) {
        List<AccessPath> combinedSubPaths = new ArrayList<>(cleanPrimaryKeySubpaths(primaryKeyMapping).subpaths());
        for (Mapping currentMapping : primaryKeyMappings) {
            String currentMappingLabel = newMetadata.getObject(currentMapping.rootObject()).label;
            Key currentMappingKey = currentMapping.rootObject().key();

            AccessPath accessPathToDelete = currentMapping.accessPath().getSubpathBySignature(oldSignatureMap.get(currentMappingKey));

            ComplexProperty cleanedComplexProperty = currentMapping.accessPath().minusSubpath(accessPathToDelete);

            ComplexProperty newComplexProperty = new ComplexProperty(new StaticName(currentMappingLabel), newSignatureMap.get(currentMappingKey), cleanedComplexProperty.subpaths());
            combinedSubPaths.add(newComplexProperty);
        }
        return new ComplexProperty(primaryKeyMapping.accessPath().name(), primaryKeyMapping.accessPath().signature(), combinedSubPaths);
    }

    private ComplexProperty cleanPrimaryKeySubpaths(Mapping primaryKeyMapping) {
        ComplexProperty primaryKeyComplexProperty = primaryKeyMapping.accessPath();
        for (Signature signature : oldSignatureMap.values()) {
            AccessPath accessPathToDelete = primaryKeyComplexProperty.getSubpathBySignature(signature);
            if (accessPathToDelete != null) {
                primaryKeyComplexProperty = cleanFromAccessPath(primaryKeyComplexProperty, accessPathToDelete);
            }
        }
        return primaryKeyComplexProperty;
    }

    private ComplexProperty cleanFromAccessPath(ComplexProperty complexProperty, AccessPath accessPathToDelete) {
        List<AccessPath> cleanedSubpaths = new ArrayList<>();
        for (AccessPath accessPath : complexProperty.subpaths()) {
            if (accessPath instanceof ComplexProperty currentComplexProperty) {
                if (currentComplexProperty.getSubpathBySignature(accessPathToDelete.signature()) != null) {
                    ComplexProperty cleanedComplexProperty = currentComplexProperty.minusSubpath(accessPathToDelete);
                    cleanedSubpaths.add(cleanedComplexProperty);
                } else {
                    cleanedSubpaths.add(currentComplexProperty);
                }
            } else {
                cleanedSubpaths.add(accessPath);
            }
        }
        return new ComplexProperty(complexProperty.name(), complexProperty.signature(), cleanedSubpaths);
    }

}
