package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
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
import java.util.stream.Collectors;
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

    private Key clusterRootKey;
    private Key newClusterKey;
    private Signature newSignature;
    private List<Signature> oldSignatures = new ArrayList<>();
    //tohle nebude fungovat --> viz priklad video - _index, picture - _index
    private Map<String, Signature> mapOldNewSignature = new HashMap<>();
    private Key clusterKey; // maybe i dont need this one anymore?
    private String newClusterName;
    private Signature newTypeSignature;

    public ClusterInferenceEdit(List<Key> clusterKeys) {
        this.clusterKeys = clusterKeys;
        this.clusterKey = clusterKeys.get(0);
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: The startKey can have ingoing and outgoing edges,
         * however we assume that one ingoing edge is from the root cluster and
         * all the other ones belong to the cluster and we need them
         */
        setSchemaCategories(schemaCategory);

         System.out.println("Applying Cluster Edit on Schema Category...");

        // find the root of the cluster
        this.clusterRootKey = findClusterRootKey(newSchemaCategory);

        // Get new cluster name. TODO : deal with ids
        List<String> oldClusterNames = getOldClusterNames(newSchemaCategory);
        this.newClusterName = findRepeatingPattern(oldClusterNames);

        // traverse one of the members of cluster and create a new SK based on that + add that SK to the original one
        SchemaCategory newSchemaCategoryPart = traverseAndBuild(newSchemaCategory, clusterKey, clusterRootKey);
        addSchemaCategoryPart(newSchemaCategory, newSchemaCategoryPart, newClusterName, oldClusterNames, clusterRootKey);

        // traverse the SK and find the keys and morphisms to delete
        markItemsForDeletion(newSchemaCategory, clusterRootKey);

        // delete the cluster objects and morphisms
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        return newSchemaCategory;
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
        for (SchemaObject schemaObject : schemaCategoryPart.allObjects()) {
            Key newKey;
            if (oldClusterNames.contains(schemaObject.label())) {
                newKey = InferenceEditorUtils.createAndAddObject(schemaCategory, newClusterName, schemaObject.ids());
                this.newClusterKey = newKey;
            } else {
                newKey = InferenceEditorUtils.createAndAddObject(schemaCategory, schemaObject.label(), schemaObject.ids());
            }
            mapOldNewKey.put(schemaObject.key(), newKey);
        }
        // add extra object and morphism representing the type
        addTypeObjectAndMorphisms(schemaCategory, schemaCategoryPart, newClusterName, mapOldNewKey, clusterRootKey, newClusterKey);
    }
    // TODO: refactor this, it doesnt make sense
    private void addTypeObjectAndMorphisms(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, String newClusterName, Map<Key, Key> mapOldNewKey, Key clusterRootKey, Key newClusterKey) {
        Key typeKey = InferenceEditorUtils.createAndAddObject(schemaCategory, "_type", ObjectIds.createGenerated());
        this.newTypeSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(newClusterKey), schemaCategory.getObject(typeKey));

        for (SchemaMorphism morphism : schemaCategoryPart.allMorphisms()) {
            SchemaObject dom = schemaCategory.getObject(mapOldNewKey.get(morphism.dom().key()));
            Signature newSig = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, schemaCategory.getObject(mapOldNewKey.get(morphism.cod().key())));
            mapOldNewSignature.put(morphism.cod().label(), newSig);
        }
        this.newSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(clusterRootKey), schemaCategory.getObject(newClusterKey));
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


    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        System.out.println("Applying Cluster Edit on Mapping...");

        // find the cluster mapping
        Mapping clusterMapping = findClusterMapping(mappings);

        // add the new complex property
        // delete the accesspaths with a certain signatures (see primary key)
        ComplexProperty changedComplexProperty = changeComplexProperties(newSchemaCategory, clusterMapping.accessPath());
        Mapping changedMapping = new Mapping(newSchemaCategory, clusterMapping.rootObject().key(), clusterMapping.kindName(), changedComplexProperty, clusterMapping.primaryKey());

        List<Mapping> mappingToDelete = new ArrayList<>();
        mappingToDelete.add(clusterMapping);
        return InferenceEditorUtils.updateMappings(mappings, mappingToDelete, changedMapping);
    }

    private Mapping findClusterMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            // if it contains at least one of the cluster keys, we should be good (theoretically)
            // TODO: make sure these assumptions hold
            if (mapping.accessPath().getSubpathBySignature(oldSignatures.get(0)) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Cluster Mapping has not been found");
    }

    private ComplexProperty changeComplexProperties(SchemaCategory schemaCategory, ComplexProperty clusterComplexProperty) {
        // retrieve the complex Property of the first cluster key
        // TODO: make the first element of clusterKeys a constant somewhere
        AccessPath firstClusterAccessPath = clusterComplexProperty.getSubpathBySignature(oldSignatures.get(0));
        ComplexProperty newComplexProperty = getNewComplexProperty(firstClusterAccessPath, clusterComplexProperty);
        return newComplexProperty;
    }

    private ComplexProperty getNewComplexProperty(AccessPath firstClusterAccessPath, ComplexProperty clusterComplexProperty) {
        List<AccessPath> newSubpaths = new ArrayList<>();

        boolean complexChanged = false;
        for (AccessPath subpath : clusterComplexProperty.subpaths()) {
            if (subpath instanceof ComplexProperty complexProperty) {
                for (Signature oldSignature : oldSignatures) { // assuming the cluster elements are all under one complexProperty
                    AccessPath currentSubpath = complexProperty.getSubpathBySignature(oldSignature);
                    if (currentSubpath != null) {
                        complexProperty = complexProperty.minusSubpath(currentSubpath);
                        complexChanged = true;
                    }
                }
                if (complexChanged) {
                    List<AccessPath> currentAccessPaths = complexProperty.subpaths();
                    currentAccessPaths.add(createNewComplexProperty((ComplexProperty) firstClusterAccessPath));
                    ComplexProperty newComplexProperty = new ComplexProperty(complexProperty.name(), complexProperty.signature(), currentAccessPaths);
                    newSubpaths.add(newComplexProperty);
                } else {
                    newSubpaths.add(complexProperty);
                }
            } else {
                newSubpaths.add(subpath);
            }
        }
        return new ComplexProperty(clusterComplexProperty.name(), newSignature, newSubpaths);
    }

    private final MappingBuilder builder = new MappingBuilder();

    public ComplexProperty createNewComplexProperty(ComplexProperty original) {
        // Generate new subpaths by transforming each subpath of the original complex property
        List<AccessPath> newSubpaths = transformSubpaths(original.subpaths());

        // Create a new ComplexProperty with the new subpaths and a new signature
        String name;
        Signature complexPropertySignature;
        if (!mapOldNewSignature.containsKey(original.name().toString())) {
            complexPropertySignature = newSignature;
            name = newClusterName;
            // add the _type object
            newSubpaths.add(builder.simple("_type", newTypeSignature));
        } else {
            complexPropertySignature = mapOldNewSignature.get(original.name().toString());
            name = original.name().toString();
        }

        ComplexProperty newComplexProperty = builder.complex(name, complexPropertySignature, newSubpaths.toArray(new AccessPath[0]));

        return newComplexProperty;
    }

    private List<AccessPath> transformSubpaths(List<AccessPath> originalSubpaths) {
        // Transform each subpath in the list
        return originalSubpaths.stream()
                .map(this::transformSubpath) // Recursive call to transform each subpath
                .collect(Collectors.toList());
    }

    private AccessPath transformSubpath(AccessPath original) {
        if (original instanceof SimpleProperty) {
            return createNewSimpleProperty((SimpleProperty) original);
        } else if (original instanceof ComplexProperty) {
            return createNewComplexProperty((ComplexProperty) original);
        } else {
            throw new IllegalArgumentException("Unknown AccessPath type");
        }
    }

    private SimpleProperty createNewSimpleProperty(SimpleProperty original) {
        SimpleProperty newSimpleProperty = builder.simple(original.name().toString(), mapOldNewSignature.get(original.name().toString()));
        return newSimpleProperty;
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
