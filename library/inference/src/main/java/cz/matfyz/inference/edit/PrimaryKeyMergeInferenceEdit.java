package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = PrimaryKeyMergeInferenceEdit.Deserializer.class)
public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "primaryKey";

    public final Key primaryKeyRoot;
    public final Key primaryKey;

    private List<Key> keysIdentifiedByPrimary;
    private Map<String, Signature> newSignatureMap; // the key is the root of the mapping
    private Map<String, Signature> oldSignatureMap;

    private List<Signature> signaturesToDelete = new ArrayList<>();
    private List<Key> keysToDelete = new ArrayList<>();

    public PrimaryKeyMergeInferenceEdit(Key primaryKeyRoot, Key primaryKey) {
        this.primaryKeyRoot = primaryKeyRoot;
        this.primaryKey = primaryKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: the primary key has a unique name. All the objects w/ this
         * name are the same primary keys.
         */
        System.out.println("Applying Primary Key Merge Edit on Schema Category...");
        System.out.println("Primary key root: " + primaryKeyRoot);
        System.out.println("Primary key: " + primaryKey);

        SchemaObject dom = schemaCategory.getObject(primaryKeyRoot);

        String primaryKeyLabel = schemaCategory.getObject(primaryKey).label();

        this.keysIdentifiedByPrimary = findKeysIdentifiedByPrimaryKeyLabel(schemaCategory, primaryKeyLabel);
        System.out.println("keysIdentifiedByPrimary: " + keysIdentifiedByPrimary);

        this.newSignatureMap = createMorphisms(schemaCategory, dom);
        this.oldSignatureMap = findMorphismsAndObjectsToDelete(schemaCategory, primaryKeyLabel);

        InferenceEditorUtils.removeMorphismsAndObjects(schemaCategory, signaturesToDelete, keysToDelete);

        return schemaCategory;
    }

    private List<Key> findKeysIdentifiedByPrimaryKeyLabel(SchemaCategory schemaCategory, String primaryKeyLabel) {
        List<Key> keys = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && !morphism.dom().key().equals(primaryKeyRoot)) {
                keys.add(morphism.dom().key());
            }
        }
        return keys;
    }

    private Map<String, Signature> createMorphisms(SchemaCategory schemaCategory, SchemaObject dom) {
        Map<String, Signature> signatureMap = new HashMap<>();
        for (Key key : keysIdentifiedByPrimary) {
            Signature newSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, key);
            signatureMap.put(schemaCategory.getObject(key).label(), newSignature);
        }
        return signatureMap;
    }

    private Map<String, Signature> findMorphismsAndObjectsToDelete(SchemaCategory schemaCategory, String primaryKeyLabel) {
        Map<String, Signature> signatureMap = new HashMap<>();

        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().label().equals(primaryKeyLabel) && keysIdentifiedByPrimary.contains(morphism.dom().key())) {
                signaturesToDelete.add(morphism.signature());
                keysToDelete.add(morphism.cod().key());
                signatureMap.put(morphism.dom().label(), morphism.signature());
            }
        }
        return signatureMap;
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        /*
         * Assumption: When we find object which is identified by the primary key,
         * we assume that the object is a root in its "part" of the schemaCategory
         */
        System.out.println("Applying Primary Key Merge Edit on Mapping...");

        // find primary key mapping and mappings where objects have been identifed by primary key keysIdentifiedByPrimary)
        Mapping primaryKeyMapping = findPrimaryKeyMapping(mappings);
        List<Mapping> primaryKeyMappings = findMappingsWithPrimaryKey(mappings);

        System.out.println("primaryKeyMapping: " + primaryKeyMapping.accessPath());
        System.out.println("accesspaths which had primary key identification:");
        for (Mapping m : primaryKeyMappings) {
            System.out.println(m.accessPath());
        }
        // add them as complex properties to the primary key mapping
        ComplexProperty mergedComplexProperty = mergeComplexProperties(primaryKeyMapping.accessPath(), primaryKeyMappings);
        Mapping mergedMapping = new Mapping(schemaCategory, primaryKeyMapping.rootObject().key(), primaryKeyMapping.kindName(), mergedComplexProperty, primaryKeyMapping.primaryKey());
        System.out.println("mergedMapping: " + mergedMapping.accessPath());

        // adding before deleting
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

    private ComplexProperty mergeComplexProperties(ComplexProperty primaryKeyComplexProperty, List<Mapping> primaryKeyMappings) {

        List<AccessPath> combinedSubPaths = new ArrayList<>(primaryKeyComplexProperty.subpaths());
        for (Mapping currentMapping : primaryKeyMappings) {
            String currentMappingLabel = currentMapping.rootObject().label();
            System.out.println("currentmappinglabel: " + currentMappingLabel);
            System.out.println(oldSignatureMap);

            AccessPath accessPathToDelete = currentMapping.accessPath().getSubpathBySignature(oldSignatureMap.get(currentMappingLabel));
            System.out.println("accesspathtodelete: " + accessPathToDelete);

            ComplexProperty cleanedComplexProperty = currentMapping.accessPath().minusSubpath(accessPathToDelete);
            System.out.println("cleanedComplexProperty: " + cleanedComplexProperty.subpaths());

            ComplexProperty newComplexProperty = new ComplexProperty(new StaticName(currentMappingLabel), newSignatureMap.get(currentMappingLabel), cleanedComplexProperty.subpaths());
            combinedSubPaths.add(newComplexProperty);
        }
        return new ComplexProperty(primaryKeyComplexProperty.name(), primaryKeyComplexProperty.signature(), combinedSubPaths);
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

            final Key primaryKeyRoot = parser.getCodec().treeToValue(node.get("primaryKeyRoot"), Key.class);
            final Key primaryKey = parser.getCodec().treeToValue(node.get("primaryKey"), Key.class);

            return new PrimaryKeyMergeInferenceEdit(primaryKeyRoot, primaryKey);
        }
    }
}
