package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
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
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.hadoop.yarn.webapp.NotFoundException;

@JsonDeserialize(using = ClusterInferenceEdit.Deserializer.class)
public class ClusterInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(ClusterInferenceEdit.class.getName());
    private static final String TYPE_LABEL = "_type";
    private static final int RND_CLUSTER_IDX = 0;

    @JsonProperty("type")
    private final String type = "cluster";

    public final List<Key> clusterKeys;

    private Key newClusterKey;
    private Signature newClusterSignature;
    private String newClusterName;
    private Signature newTypeSignature;

    private Map<String, Signature> mapOldClusterNameSignature = new HashMap<>(); // this maps the original cluster names to their original signatures
    private Map<Signature, Signature> mapOldNewSignature = new HashMap<>(); // this maps the original signatures to the new signatures

    public ClusterInferenceEdit(List<Key> clusterKeys) {
        this.clusterKeys = clusterKeys;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumption: The startKey can have ingoing and outgoing edges,
         * however we assume that one ingoing edge is from the root cluster
         */
        LOGGER.info("Applying Cluster Edit on Schema Category...");

        setSchemaCategories(schemaCategory);

        Key clusterRootKey = findClusterRootKey(newSchemaCategory);

        List<String> oldClusterNames = getOldClusterNames(newSchemaCategory);
        this.newClusterName = findRepeatingPattern(oldClusterNames);

        // traverse one of the members of cluster and create a new SK based on that + add that SK to the original one
        SchemaCategory newSchemaCategoryPart = traverseAndBuild(newSchemaCategory, clusterKeys.get(RND_CLUSTER_IDX), clusterRootKey);
        addSchemaCategoryPart(newSchemaCategory, newSchemaCategoryPart, oldClusterNames, clusterRootKey);

        findMorphismsAndObjectsToDelete(newSchemaCategory, clusterRootKey);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        return newSchemaCategory;
    }

    // The cluster root is the object, which has an outgoing morphisms to all of the clusterKeys
    // we assume there is only one such object
    private Key findClusterRootKey(SchemaCategory schemaCategory) {
        Multiset<Key> rootCandidates = HashMultiset.create();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (clusterKeys.contains(morphism.cod().key())) {
                rootCandidates.add(morphism.dom().key());
            }
        }
        return rootCandidates.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
                .map(Map.Entry::getKey).orElse(null);
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

    // Note: Consumers (+ other functional interfaces) are pretty cool
    private void traverseAndPerform(SchemaCategory category, Key startKey, Key clusterRootKey,
                                    Consumer<SchemaObject> objectConsumer, Consumer<SchemaMorphism> morphismConsumer) {
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObject> queue = new LinkedList<>();
        SchemaObject startObject = category.getObject(startKey);

        queue.add(startObject);
        visited.add(startKey);
        objectConsumer.accept(startObject);

        while (!queue.isEmpty()) {
            SchemaObject currentObject = queue.poll();
            for (SchemaMorphism morphism : category.allMorphisms()) {
                if (morphism.dom().equals(currentObject)) {
                    SchemaObject targetObject = morphism.cod();
                    if (visited.add(targetObject.key())) {
                        queue.add(targetObject);
                        objectConsumer.accept(targetObject);
                    }
                    morphismConsumer.accept(morphism);
                }
                if (morphism.cod().equals(currentObject) &&
                    !morphism.cod().key().equals(startKey) &&
                    !morphism.dom().key().equals(clusterRootKey)) {
                    SchemaObject sourceObject = morphism.dom();
                    if (visited.add(sourceObject.key())) {
                        queue.add(sourceObject);
                        objectConsumer.accept(sourceObject);
                    }
                    morphismConsumer.accept(morphism);
                }
            }
        }
    }

    private SchemaCategory traverseAndBuild(SchemaCategory category, Key startKey, Key clusterRootKey) {
        SchemaCategory resultCategory = new SchemaCategory(category.label + "_traversed");
        traverseAndPerform(category, startKey, clusterRootKey,
            resultCategory::addObject,
            resultCategory::addMorphism
        );
        return resultCategory;
    }

    private void traverseAndFind(SchemaCategory category, Key startKey, Key clusterRootKey) {
        traverseAndPerform(category, startKey, clusterRootKey,
            schemaObject -> keysToDelete.add(schemaObject.key()),
            morphism -> signaturesToDelete.add(morphism.signature())
        );
    }

    private void addSchemaCategoryPart(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, List<String> oldClusterNames, Key clusterRootKey) {
        Map<Key, Key> mapOldNewKey = addObjects(schemaCategory, schemaCategoryPart, oldClusterNames);
        addMorphisms(schemaCategory, schemaCategoryPart, mapOldNewKey, clusterRootKey);
        addTypeObjectAndMorphism(schemaCategory);
    }

    private Map<Key, Key> addObjects(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, List<String> oldClusterNames) {
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
        return mapOldNewKey;
    }

    private void addMorphisms(SchemaCategory schemaCategory, SchemaCategory schemaCategoryPart, Map<Key, Key> mapOldNewKey, Key clusterRootKey) {
        for (SchemaMorphism morphism : schemaCategoryPart.allMorphisms()) {
            SchemaObject dom = schemaCategory.getObject(mapOldNewKey.get(morphism.dom().key()));
            Signature newSig = InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, schemaCategory.getObject(mapOldNewKey.get(morphism.cod().key())));
            mapOldNewSignature.put(morphism.signature(), newSig);
        }
        this.newClusterSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(clusterRootKey), schemaCategory.getObject(newClusterKey));
    }

    private void addTypeObjectAndMorphism(SchemaCategory schemaCategory) {
        Key typeKey = InferenceEditorUtils.createAndAddObject(schemaCategory, TYPE_LABEL, ObjectIds.createValue());
        this.newTypeSignature = InferenceEditorUtils.createAndAddMorphism(schemaCategory, schemaCategory.getObject(newClusterKey), schemaCategory.getObject(typeKey));
    }

    private void findMorphismsAndObjectsToDelete(SchemaCategory schemaCategory, Key clusterRootKey) {
        for (Key key : clusterKeys) {
            traverseAndFind(schemaCategory, key, clusterRootKey);
        }
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().key().equals(clusterRootKey) && clusterKeys.contains(morphism.cod().key())) {
                mapOldClusterNameSignature.put(morphism.cod().label(), morphism.signature());
                signaturesToDelete.add(morphism.signature());
            }
        }
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Cluster Edit on Mapping...");

        Mapping clusterMapping = findClusterMapping(mappings);

        Mapping mergedMapping = createMergedMapping(clusterMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(clusterMapping), mergedMapping);
    }

    private Mapping findClusterMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            // just try if any of the old signatures is in the mapping, then all of them should be there
            SchemaObject randomClusterObject = oldSchemaCategory.getObject(clusterKeys.get(RND_CLUSTER_IDX));
            String randomClusterName = randomClusterObject.label();
            if (mapping.accessPath().getSubpathBySignature(mapOldClusterNameSignature.get(randomClusterName)) != null) {
                return mapping;
            }
        }
        throw new NotFoundException("Cluster Mapping has not been found");
    }

    private Mapping createMergedMapping(Mapping clusterMapping) {
        ComplexProperty changedComplexProperty = changeComplexProperties(clusterMapping.accessPath());
        return new Mapping(newSchemaCategory, clusterMapping.rootObject().key(), clusterMapping.kindName(), changedComplexProperty, clusterMapping.primaryKey());
    }

    private ComplexProperty changeComplexProperties(ComplexProperty clusterComplexProperty) {
        SchemaObject randomClusterObject = oldSchemaCategory.getObject(clusterKeys.get(RND_CLUSTER_IDX));
        String randomClusterName = randomClusterObject.label();
        AccessPath firstClusterAccessPath = clusterComplexProperty.getSubpathBySignature(mapOldClusterNameSignature.get(randomClusterName));
        return getNewComplexProperty(firstClusterAccessPath, clusterComplexProperty);
    }

    private ComplexProperty getNewComplexProperty(AccessPath firstClusterAccessPath, ComplexProperty clusterComplexProperty) {
        List<AccessPath> newSubpaths = new ArrayList<>();
        boolean complexChanged = false;

        for (AccessPath subpath : clusterComplexProperty.subpaths()) {
            if (subpath instanceof ComplexProperty complexProperty) {
                if (!complexChanged) {
                    for (Signature oldSignature : mapOldClusterNameSignature.values()) {
                        AccessPath currentSubpath = complexProperty.getSubpathBySignature(oldSignature);
                        if (currentSubpath != null) {
                            complexProperty = complexProperty.minusSubpath(currentSubpath);
                            complexChanged = true;
                        }
                    }
                }
                if (complexChanged) {
                    List<AccessPath> currentAccessPaths = complexProperty.subpaths();
                    currentAccessPaths.add(createNewComplexProperty((ComplexProperty) firstClusterAccessPath));
                    newSubpaths.add(new ComplexProperty(complexProperty.name(), complexProperty.signature(), currentAccessPaths));
                } else {
                    newSubpaths.add(complexProperty);
                }
            } else {
                newSubpaths.add(subpath);
            }
        }
        return new ComplexProperty(clusterComplexProperty.name(), newClusterSignature, newSubpaths);
    }

    public ComplexProperty createNewComplexProperty(ComplexProperty original) {
        List<AccessPath> newSubpaths = transformSubpaths(original.subpaths());
        String name;
        Signature complexPropertySignature;

        if (!mapOldNewSignature.containsKey(original.signature()) && !mapOldNewSignature.containsKey(original.signature().dual())) {
            complexPropertySignature = newClusterSignature;
            name = newClusterName;
            // add the _type object
            newSubpaths.add(new SimpleProperty(new DynamicName(newTypeSignature), newTypeSignature));
        } else {
            complexPropertySignature = mapOldNewSignature.get(original.signature());
            if (complexPropertySignature == null) { //meaning the original was an array object and so the signature was dual
                complexPropertySignature = mapOldNewSignature.get(original.signature().dual()).dual();
            }
            name = original.name().toString();
        }
        return new ComplexProperty(new StaticName(name), complexPropertySignature, newSubpaths);
    }

    private List<AccessPath> transformSubpaths(List<AccessPath> originalSubpaths) {
        return originalSubpaths.stream()
                .map(this::transformSubpath)
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
        return new SimpleProperty(original.name(), mapOldNewSignature.get(original.signature()));
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
