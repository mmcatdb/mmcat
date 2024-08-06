package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = PrimaryKeyMergeInferenceEdit.Deserializer.class)
public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(PrimaryKeyMergeInferenceEdit.class.getName());

    @JsonProperty("type")
    private final String type = "primaryKey";

    public final Key primaryKey;

    private Key primaryKeyRoot;
    private List<Key> keysIdentifiedByPrimary;
    private Map<Key, Signature> newSignatureMap;
    private Map<Key, Signature> oldSignatureMap = new HashMap<>();

    public PrimaryKeyMergeInferenceEdit(Key primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: the primary key has a unique name. All the objects w/ this
         * name are the same primary keys. The primary key is a single object
         */
        LOGGER.info("Applying Primary Key Merge Edit on Schema Category...");

        setSchemaCategories(schemaCategory);

        this.primaryKeyRoot = findPrimaryKeyRoot(newSchemaCategory);
        SchemaObject dom = newSchemaCategory.getObject(primaryKeyRoot);

        String primaryKeyLabel = newSchemaCategory.getObject(primaryKey).label();
        this.keysIdentifiedByPrimary = findKeysIdentifiedByPrimaryKeyLabel(newSchemaCategory, primaryKeyLabel);

        this.newSignatureMap = createNewMorphisms(newSchemaCategory, dom);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        return newSchemaCategory;
    }

    private Key findPrimaryKeyRoot(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            // based on the assumption
            if (morphism.cod().key().equals(primaryKey)) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Primary Key Root has not been found");
    }

    private List<Key> findKeysIdentifiedByPrimaryKeyLabel(SchemaCategory schemaCategory, String primaryKeyLabel) {
        List<Key> keys = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && !morphism.dom().key().equals(primaryKeyRoot)) {
                keys.add(morphism.dom().key());

                signaturesToDelete.add(morphism.signature());
                keysToDelete.add(morphism.cod().key());
                oldSignatureMap.put(morphism.dom().key(), morphism.signature());
            }
        }
        return keys;
    }

    private Map<Key, Signature> createNewMorphisms(SchemaCategory schemaCategory, SchemaObject dom) {
        Map<Key, Signature> signatureMap = new HashMap<>();
        for (Key key : keysIdentifiedByPrimary) {
            Signature newSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, schemaCategory.getObject(key));
            signatureMap.put(key, newSignature);
        }
        return signatureMap;
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        /*
         * Assumption: When we find object which is identified by the primary key,
         * we assume that the object is a root in its "part" of the schemaCategory
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
        ComplexProperty mergedComplexProperty = mergeComplexProperties(primaryKeyMapping, primaryKeyMappings);
        return InferenceEditorUtils.createNewMapping(newSchemaCategory, primaryKeyMapping, primaryKeyMappings, mergedComplexProperty);
    }

    private ComplexProperty mergeComplexProperties(Mapping primaryKeyMapping, List<Mapping> primaryKeyMappings) {
        List<AccessPath> combinedSubPaths = new ArrayList<>(cleanPrimaryKeySubpaths(primaryKeyMapping).subpaths());
        for (Mapping currentMapping : primaryKeyMappings) {
            String currentMappingLabel = currentMapping.rootObject().label();
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

    public static class Deserializer extends StdDeserializer<PrimaryKeyMergeInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public PrimaryKeyMergeInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Key primaryKey = parser.getCodec().treeToValue(node.get("primaryKey"), Key.class);

            return new PrimaryKeyMergeInferenceEdit(primaryKey);
        }
    }
}
