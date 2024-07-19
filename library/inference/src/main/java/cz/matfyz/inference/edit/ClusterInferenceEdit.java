package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
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
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = ClusterInferenceEdit.Deserializer.class)
public class ClusterInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "cluster";

    public final List<Key> clusterKeys;

    private List<Signature> oldSignatures;
    private Signature newSignature;

    public ClusterInferenceEdit(List<Key> clusterKeys) {
        this.clusterKeys = clusterKeys;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: The objects in the cluster only have outgoing morphisms 
         * which have the same structure in all of the objects, and then one ingoing
         * from the root.
         * Some notes need to be made on how the user should choose the cluster,
         * otherwise it wont work accordingly, for example if they are not the same 
         * there should pop up a warning
         */
        System.out.println("Applying Cluster Edit on Schema Category...");

        // find the root of the cluster
        Key clusterRootKey = findClusterRootKey(schemaCategory);

        List<Key> keysToDelete = new ArrayList<>();
        List<SchemaMorphism> morphismsToDelete = new ArrayList<>();

        // create a new object and morphism representing the cluster, TODO : deal with ids
        String newClusterName = extractNewClusterName(schemaCategory);
        Key newClusterObjectKey = InferenceEditorUtils.createAndAddObject(schemaCategory, newClusterName, ObjectIds.createGenerated());
        SchemaObject newClusterObject = schemaCategory.get(newClusterObjectKey);
        this.newSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(clusterRootKey), newClusterObjectKey);

        // add the new identification object
        Key newIdentificationKey = InferenceEditorUtils.createAndAddObject(schemaCategory, "type", ObjectIds.createGenerated());
        InferenceEditor.createAndAddMorphism(schemaCategory, newClusterObject, newIdentificationKey);

        // traverse one of the clusteKeys and at the same time create the objects and the morphisms
        Key clusterKey = clusterKeys.get(0);


        // delete the cluster objects and morphisms
       
        
        return traverseAndBuild(schemaCategory, clusterKey);
    }

    private Key findClusterRootKey(SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.cod().key().equals(clusterKeys.get(0))) {
                return morphism.dom().key();
            }
        }
        throw new NotFoundException("Root of the cluster has not been found");
    }

    private String extractNewClusterName(SchemaCategory schemaCategory) {
        List<String> oldClusterNames = getOldClusterNames(schemaCategory);
        return findRepeatingPattern(oldClusterNames);
    }

    private List<String> getOldClusterNames(SchemaCategory schemaCategory) {
        List<String> oldClusterNames = new ArrayList<>();
        for (Key key : clusterKeys) {
            clusterNames.add(schemaCategory.getObject(key).label());
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
        
        String pattern = findLongestCommonSubstring(normalizedStrings);
        return pattern;
    }
    
    private String normalizeString(String str) {
        return str.replaceAll("[._-:,]", "");
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

    public static SchemaCategory traverseAndBuild(SchemaCategory category, Key startKey) {
        SchemaCategory resultCategory = new SchemaCategory(category.label + "_traversed");
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObject> queue = new LinkedList<>();
        
        SchemaObject startObject = category.getObject(startKey);
        if (startObject == null) {
            throw new IllegalArgumentException("Start object not found in the category.");
        }
        
        queue.add(startObject);
        visited.add(startKey);
        resultCategory.addObject(startObject);
        
        while (!queue.isEmpty()) {
            SchemaObject currentObject = queue.poll();
            for (SchemaMorphism morphism : category.allMorphisms()) {
                if (morphism.dom().equals(currentObject)) {
                    SchemaObject targetObject = morphism.cod();
                    if (!visited.contains(targetObject.key())) {
                        visited.add(targetObject.key());
                        queue.add(targetObject);
                        resultCategory.addObject(targetObject);
                    }
                    resultCategory.addMorphism(morphism);
                }
            }
        }
        
        return resultCategory;
    }

    

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // find the root mapping

        // delete the accesspaths with a certain signature - look at primary key for inspo

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
