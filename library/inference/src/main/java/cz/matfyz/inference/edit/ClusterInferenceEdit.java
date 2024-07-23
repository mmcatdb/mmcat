package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = ClusterInferenceEdit.Deserializer.class)
public class ClusterInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "cluster";

    public final List<Key> clusterKeys;

    private List<Signature> oldSignatures = new ArrayList<>();
    private Signature newSignature;
    private List<Key> keysToDelete = new ArrayList<>();
    private List<Signature> signaturesToDelete = new ArrayList<>();

    public ClusterInferenceEdit(List<Key> clusterKeys) {
        this.clusterKeys = clusterKeys;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: The startKey can have ingoing and outgoing edges,
         * however we assume that one ingoing edge is from the root cluster and
         * all the other ones belong to the cluster and we need them
         */
        System.out.println("Applying Cluster Edit on Schema Category...");

        // find the root of the cluster
        Key clusterRootKey = findClusterRootKey(schemaCategory);
        System.out.println("clusterRootKey: " + clusterRootKey);

        // Get new cluster name. TODO : deal with ids
        List<String> oldClusterNames = getOldClusterNames(schemaCategory);
        String newClusterName = findRepeatingPattern(oldClusterNames);
        System.out.println("newClusterName: " + newClusterName);

        // traverse one of the members of cluster and create a new SK based on that + add that SK to the original one
        Key clusterKey = clusterKeys.get(0);
        SchemaCategory newSchemaCategoryPart = traverseAndBuild(schemaCategory, clusterKey, clusterRootKey);
        addSchemaCategoryPart(schemaCategory, newSchemaCategoryPart, newClusterName, oldClusterNames, clusterRootKey);

        // traverse the SK and find the keys and morphisms to delete
        markItemsForDeletion(schemaCategory, clusterRootKey);

        // delete the cluster objects and morphisms
        removeClusterMorphismsAndObjects(schemaCategory);

        return schemaCategory;
    }

    private Key findClusterRootKey(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().key().equals(clusterKeys.get(0))) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Root of the cluster has not been found");
    }

    private List<String> getOldClusterNames(SchemaCategory schemaCategory) {
        List<String> oldClusterNames = new ArrayList<>();
        for (Key key : clusterKeys) {
            oldClusterNames.add(schemaCategory.getObject(key).label());
        }
        return oldClusterNames;
    }

    private String findRepeatingPattern(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return "";
        }
        List<String> normalizedStrings = new ArrayList<>();
        for (String str : strings) {
            normalizedStrings.add(normalizeString(str));
        }
        return findLongestCommonSubstring(normalizedStrings);
    }

    private String normalizeString(String str) {
        return str.replaceAll("[._:,-]", "");
    }

    private String findLongestCommonSubstring(List<String> strings) {
        if (strings.size() == 1) {
            return strings.get(0);
        }
        String reference = strings.get(0);
        String longestCommonSubstring = "";
        for (int length = reference.length(); length > 0; length--) {
            for (int start = 0; start <= reference.length() - length; start++) {
                String candidate = reference.substring(start, start + length);
                boolean isCommon = true;
                for (int i = 1; i < strings.size(); i++) {
                    if (!strings.get(i).contains(candidate)) {
                        isCommon = false;
                        break;
                    }
                }
                if (isCommon && candidate.length() > longestCommonSubstring.length()) {
                    longestCommonSubstring = candidate;
                }
            }
        }
        return longestCommonSubstring;
    }

    // TODO: needs refactoring
    private SchemaCategory traverseAndBuild(SchemaCategory category, Key startKey, Key clusterRootKey) {
        SchemaCategory resultCategory = new SchemaCategory(category.label + "_traversed");
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObject> queue = new LinkedList<>();
        SchemaObject startObject = category.getObject(startKey);

        queue.add(startObject);
        visited.add(startKey);
        resultCategory.addObject(startObject);

        while (!queue.isEmpty()) {
            SchemaObject currentObject = queue.poll();
            for (SchemaMorphism morphism : category.allMorphisms()) {
                // Traverse outgoing edges (dom to cod)
                if (morphism.dom().equals(currentObject)) {
                    SchemaObject targetObject = morphism.cod();
                    if (!visited.contains(targetObject.key())) {
                        visited.add(targetObject.key());
                        queue.add(targetObject);
                        if (!resultCategory.hasObject(targetObject.key())) {
                            resultCategory.addObject(targetObject);
                        }
                    }
                    if (!resultCategory.hasMorphism(morphism.signature())) {
                        resultCategory.addMorphism(morphism);
                    }
                }
                // Traverse incoming edges (cod to dom)
                if (morphism.cod().equals(currentObject)) {
                    if (!morphism.cod().key().equals(startKey) && !morphism.dom().key().equals(clusterRootKey)) {
                        SchemaObject sourceObject = morphism.dom();
                        if (!visited.contains(sourceObject.key())) {
                            visited.add(sourceObject.key());
                            queue.add(sourceObject);
                            if (!resultCategory.hasObject(sourceObject.key())) {
                                resultCategory.addObject(sourceObject);
                            }
                        }
                        if (!resultCategory.hasMorphism(morphism.signature())) {
                            resultCategory.addMorphism(morphism);
                        }
                    }
                }
            }
        }
        return resultCategory;
    }

    private void addSchemaCategoryPart(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, String newClusterName, List<String> oldClusterNames, Key clusterRootKey) {
        Map<Key, Key> mapOldNewKey = new HashMap<>();
        Key newClusterKey = null;
        for (SchemaObject schemaObject : schemaCategoryPart.allObjects()) {
            Key newKey;
            if (oldClusterNames.contains(schemaObject.label())) {
                newKey = InferenceEditorUtils.createAndAddObject(schemaCategory, newClusterName, schemaObject.ids());
                newClusterKey = newKey;
            } else {
                newKey = InferenceEditorUtils.createAndAddObject(schemaCategory, schemaObject.label(), schemaObject.ids());
            }
            mapOldNewKey.put(schemaObject.key(), newKey);
        }
        // add extra object and morphism representing the type
        addTypeObjectAndMorphisms(schemaCategory, schemaCategoryPart, newClusterName, mapOldNewKey, clusterRootKey, newClusterKey);
    }

    private void addTypeObjectAndMorphisms(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, String newClusterName, Map<Key, Key> mapOldNewKey, Key clusterRootKey, Key newClusterKey) {
        Key typeKey = InferenceEditorUtils.createAndAddObject(schemaCategory, "_type", ObjectIds.createGenerated());
        InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(newClusterKey), typeKey);

        for (SchemaMorphism morphism : schemaCategoryPart.allMorphisms()) {
            SchemaObject dom = schemaCategory.getObject(mapOldNewKey.get(morphism.dom().key()));
            Signature newSig = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, mapOldNewKey.get(morphism.cod().key()));
            if (dom.label().equals(newClusterName)) {
                this.newSignature = newSig;
            }
        }
        InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(clusterRootKey), newClusterKey);
    }

    private void markItemsForDeletion(SchemaCategory schemaCategory, Key clusterRootKey) {
        for (Key key : clusterKeys) {
            traverseAndFind(schemaCategory, key, clusterRootKey);
        }
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(clusterRootKey) && clusterKeys.contains(morphism.cod().key())) {
                oldSignatures.add(morphism.signature());
                signaturesToDelete.add(morphism.signature());
            }
        }
        System.out.println(keysToDelete);
        System.out.println(signaturesToDelete);
    }

    private void traverseAndFind(SchemaCategory category, Key startKey, Key clusterRootKey) {
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObject> queue = new LinkedList<>();
        SchemaObject startObject = category.getObject(startKey);

        queue.add(startObject);
        visited.add(startKey);
        this.keysToDelete.add(startObject.key());

        while (!queue.isEmpty()) {
            SchemaObject currentObject = queue.poll();
            for (SchemaMorphism morphism : category.allMorphisms()) {
                if (morphism.dom().equals(currentObject)) {
                    SchemaObject targetObject = morphism.cod();
                    if (!visited.contains(targetObject.key())) {
                        visited.add(targetObject.key());
                        queue.add(targetObject);
                        if (!this.keysToDelete.contains(targetObject.key())) {
                            this.keysToDelete.add(targetObject.key());
                        }
                    }
                    if (!this.signaturesToDelete.contains(morphism.signature())) {
                        this.signaturesToDelete.add(morphism.signature());
                    }
                }
                if (morphism.cod().equals(currentObject)) {
                    if (!morphism.cod().key().equals(startKey) && !morphism.dom().key().equals(clusterRootKey)) {
                        SchemaObject sourceObject = morphism.dom();
                        if (!visited.contains(sourceObject.key())) {
                            visited.add(sourceObject.key());
                            queue.add(sourceObject);
                            if (!this.keysToDelete.contains(sourceObject.key())) {
                                this.keysToDelete.add(sourceObject.key());
                            }
                        }
                        if (!this.signaturesToDelete.contains(morphism.signature())) {
                            this.signaturesToDelete.add(morphism.signature());
                        }
                    }
                }
            }
        }
    }

    private void removeClusterMorphismsAndObjects(SchemaCategory schemaCategory) {
        for (Signature sig : signaturesToDelete) {
            SchemaMorphism morphism = schemaCategory.getMorphism(sig);
            schemaCategory.removeMorphism(morphism);
        }
        InferenceEditorUtils.SchemaCategoryEditor editor = new InferenceEditorUtils.SchemaCategoryEditor(schemaCategory);
        editor.deleteObjects(keysToDelete);
    }


    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // find the cluster mapping

        // delete the accesspaths with a certain signatures - look at primary key for inspo

        // add the new complex property

        throw new UnsupportedOperationException("method not implemented");
    }

    public static class Deserializer extends StdDeserializer<ClusterInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ClusterInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final List<Key> clusterKeys = new ArrayList<>();
            final JsonNode keysNode = node.get("clusterKeys");

            if (keysNode != null && keysNode.isArray()) {
                for (JsonNode keyNode : keysNode) {
                    Key key = parser.getCodec().treeToValue(keyNode, Key.class);
                    clusterKeys.add(key);
                }
            }
            return new ClusterInferenceEdit(clusterKeys);
        }
    }
}