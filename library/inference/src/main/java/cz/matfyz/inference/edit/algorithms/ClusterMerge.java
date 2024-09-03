package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

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

import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.hadoop.yarn.webapp.NotFoundException;

public class ClusterMerge extends InferenceEditAlgorithm {

    public static class Data extends InferenceEdit {

        @JsonProperty("clusterKeys")
        List<Key> clusterKeys;

        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("clusterKeys") List<Key> clusterKeys) {
            setId(id);
            setActive(isActive);
            this.clusterKeys = clusterKeys;
        }

        public Data() {
            setId(null);
            setActive(false);
            this.clusterKeys = null;
        }

        public List<Key> getClusterKeys() {
            return clusterKeys;
        }

        @Override public ClusterMerge createAlgorithm() {
            return new ClusterMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClusterMerge.class.getName());
    private static final String TYPE_LABEL = "_type";
    private static final int RND_CLUSTER_IDX = 0;

    private final Data data;

    public ClusterMerge(Data data) {
        this.data = data;
    }

    private Key newClusterKey;
    private Signature newClusterSignature;
    private String newClusterName;
    private Signature newTypeSignature;

    private Map<String, Signature> mapOldClusterNameSignature = new HashMap<>(); // this maps the original cluster names to their original signatures
    private Map<Signature, Signature> mapOldNewSignature = new HashMap<>(); // this maps the original signatures to the new signatures

    private SchemaCategory newSchemaPart;
    private MetadataCategory newMetadataPart;

    /*
     * Assumption: The startKey can have ingoing and outgoing edges,
     * however we assume that one ingoing edge is from the root cluster
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Cluster Edit on Schema Category...");

        final Key clusterRootKey = findClusterRootKey(newSchema);

        final List<String> oldClusterNames = getOldClusterNames();
        newClusterName = findRepeatingPattern(oldClusterNames);

        // traverse one of the members of cluster and create a new SK based on that + add that SK to the original one
        traverseAndBuild(data.clusterKeys.get(RND_CLUSTER_IDX), clusterRootKey);
        addSchemaPart(oldClusterNames, clusterRootKey);

        findMorphismsAndObjectsToDelete(clusterRootKey);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);
    }

    // The cluster root is the object, which has an outgoing morphisms to all of the clusterKeys
    // we assume there is only one such object
    private Key findClusterRootKey(SchemaCategory schema) {
        final Multiset<Key> rootCandidates = HashMultiset.create();
        for (final SchemaMorphism morphism : schema.allMorphisms())
            if (data.clusterKeys.contains(morphism.cod().key()))
                rootCandidates.add(morphism.dom().key());

        return rootCandidates.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
            .map(Map.Entry::getKey).orElse(null);
}

    private List<String> getOldClusterNames() {
        final List<String> oldClusterNames = new ArrayList<>();
        for (final Key key : data.clusterKeys)
            oldClusterNames.add(newMetadata.getObject(key).label);

        return oldClusterNames;
    }

    private String findRepeatingPattern(List<String> strings) {
        if (strings == null || strings.isEmpty())
            return "";

        final List<String> normalizedStrings = new ArrayList<>();
        for (final String str : strings)
            normalizedStrings.add(normalizeString(str));

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
    private void traverseAndPerform(Key startKey, Key clusterRootKey, Consumer<SchemaObject> objectConsumer, Consumer<SchemaMorphism> morphismConsumer) {
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObject> queue = new LinkedList<>();
        SchemaObject startObject = newSchema.getObject(startKey);

        queue.add(startObject);
        visited.add(startKey);
        objectConsumer.accept(startObject);

        while (!queue.isEmpty()) {
            SchemaObject currentObject = queue.poll();
            for (SchemaMorphism morphism : newSchema.allMorphisms()) {
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

    private void traverseAndBuild(Key startKey, Key clusterRootKey) {
        newSchemaPart = new SchemaCategory();
        newMetadataPart = MetadataCategory.createEmpty(newSchemaPart);

        traverseAndPerform(startKey, clusterRootKey,
            newObject -> {
                newSchemaPart.addObject(newObject);
                newMetadataPart.setObject(newObject, newMetadata.getObject(newObject));
            },
            newMorphism -> {
                newSchemaPart.addMorphism(newMorphism);
                newMetadataPart.setMorphism(newMorphism, newMetadata.getMorphism(newMorphism));
            }
        );
    }

    private void traverseAndFind(Key startKey, Key clusterRootKey) {
        traverseAndPerform(startKey, clusterRootKey,
            object -> keysToDelete.add(object.key()),
            morphism -> signaturesToDelete.add(morphism.signature())
        );
    }

    private void addSchemaPart(List<String> oldClusterNames, Key clusterRootKey) {
        final Map<Key, Key> mapOldNewKey = addObjects(oldClusterNames);
        addMorphisms(mapOldNewKey, clusterRootKey);
        //addTypeObjectAndMorphism();
    }

    private Map<Key, Key> addObjects(List<String> oldClusterNames) {
        final Map<Key, Key> mapOldNewKey = new HashMap<>();
        for (final SchemaObject object : newSchemaPart.allObjects()) {
            final var mo = newMetadataPart.getObject(object);
            Key newKey;
            if (oldClusterNames.contains(mo.label)) {
                newKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, object.ids(), newClusterName);
                newClusterKey = newKey;
            } else {
                newKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, object.ids(), mo.label);
            }

            mapOldNewKey.put(object.key(), newKey);
        }

        return mapOldNewKey;
    }

    private void addMorphisms(Map<Key, Key> mapOldNewKey, Key clusterRootKey) {
        for (final SchemaMorphism morphism : newSchemaPart.allMorphisms()) {
            final SchemaObject dom = newSchema.getObject(mapOldNewKey.get(morphism.dom().key()));
            final Signature newSig = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, newSchema.getObject(mapOldNewKey.get(morphism.cod().key())));
            mapOldNewSignature.put(morphism.signature(), newSig);
        }
        newClusterSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(clusterRootKey), newSchema.getObject(newClusterKey));
    }

    private void addTypeObjectAndMorphism() {
        final Key typeKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, ObjectIds.createValue(), TYPE_LABEL);
        newTypeSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(newClusterKey), newSchema.getObject(typeKey));
    }

    private void findMorphismsAndObjectsToDelete(Key clusterRootKey) {
        for (final Key key : data.clusterKeys)
            traverseAndFind(key, clusterRootKey);

        for (final SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (morphism.dom().key().equals(clusterRootKey) && data.clusterKeys.contains(morphism.cod().key())) {
                final var codMetadata = newMetadata.getObject(morphism.cod());
                mapOldClusterNameSignature.put(codMetadata.label, morphism.signature());
                signaturesToDelete.add(morphism.signature());
            }
        }
    }

    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Cluster Edit on Mapping...");

        Mapping clusterMapping = findClusterMapping(mappings);

        Mapping mergedMapping = createMergedMapping(clusterMapping);

        //return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(clusterMapping), mergedMapping);
        return mappings;
    }

    private Mapping findClusterMapping(List<Mapping> mappings) {
        for (final Mapping mapping : mappings) {
            // just try if any of the old signatures is in the mapping, then all of them should be there
            final var randomClusterObject = oldMetadata.getObject(data.clusterKeys.get(RND_CLUSTER_IDX));
            final var signature = mapOldClusterNameSignature.get(randomClusterObject.label);
            if (mapping.accessPath().getSubpathBySignature(signature) != null)
                return mapping;
        }

        throw new NotFoundException("Cluster Mapping has not been found");
    }

    private Mapping createMergedMapping(Mapping clusterMapping) {
        ComplexProperty changedComplexProperty = changeComplexProperties(clusterMapping.accessPath());
        return new Mapping(newSchema, clusterMapping.rootObject().key(), clusterMapping.kindName(), changedComplexProperty, clusterMapping.primaryKey());
    }

    private ComplexProperty changeComplexProperties(ComplexProperty clusterComplexProperty) {
        final var randomClusterObject = oldMetadata.getObject(data.clusterKeys.get(RND_CLUSTER_IDX));
        final var signature = mapOldClusterNameSignature.get(randomClusterObject.label);
        final AccessPath firstClusterAccessPath = clusterComplexProperty.getSubpathBySignature(signature);

        return getNewComplexProperty(firstClusterAccessPath, clusterComplexProperty);
    }

    private ComplexProperty getNewComplexProperty(AccessPath firstClusterAccessPath, ComplexProperty clusterComplexProperty) {
        List<AccessPath> newSubpaths = new ArrayList<>();
        boolean complexChanged = false;
        boolean complexIsCluster = false;

        for (AccessPath subpath : clusterComplexProperty.subpaths()) {
            if (!(subpath instanceof ComplexProperty complexProperty)) {
                newSubpaths.add(subpath);
                continue;
            }

            if (!complexChanged || complexIsCluster) {
                for (Signature oldSignature : mapOldClusterNameSignature.values()) {
                    if (complexProperty.signature().equals(oldSignature)) {
                        complexProperty = null;
                        complexChanged = true;
                        complexIsCluster = true;
                        break;
                    } else if (complexProperty != null) {
                        AccessPath currentSubpath = complexProperty.getSubpathBySignature(oldSignature);
                        if (currentSubpath != null) {
                            complexProperty = complexProperty.minusSubpath(currentSubpath);
                            complexChanged = true;
                        }
                    }
                }
            }

            if (complexChanged) {
                List<AccessPath> updatedSubpaths = complexProperty != null ? complexProperty.subpaths() : new ArrayList<>();
                ComplexProperty newComplexProperty = createNewComplexProperty((ComplexProperty) firstClusterAccessPath);
                updatedSubpaths.add(newComplexProperty);

                ComplexProperty resultProperty = complexProperty != null
                    ? new ComplexProperty(complexProperty.name(), complexProperty.signature(), updatedSubpaths)
                    : newComplexProperty;

                newSubpaths.add(resultProperty);

            } else if (complexProperty != null) {
                newSubpaths.add(complexProperty);
            }
        }
        //TODO: use the mapping builder everywhere
        MappingBuilder mappingBuilder = new MappingBuilder();
        return mappingBuilder.root(newSubpaths.toArray(new AccessPath[0]));
    }

    public ComplexProperty createNewComplexProperty(ComplexProperty original) {
        List<AccessPath> newSubpaths = transformSubpaths(original.subpaths());
        Name name;
        Signature complexPropertySignature;

        if (!mapOldNewSignature.containsKey(original.signature()) && !mapOldNewSignature.containsKey(original.signature().dual())) {
            complexPropertySignature = newClusterSignature;
            //name = newClusterName;
            name = new DynamicName(complexPropertySignature);

            // add the _type object
            //newSubpaths.add(new SimpleProperty(new DynamicName(newTypeSignature), newTypeSignature));
        } else {
            complexPropertySignature = mapOldNewSignature.get(original.signature());
            if (complexPropertySignature == null) { //meaning the original was an array object and so the signature was dual
                complexPropertySignature = mapOldNewSignature.get(original.signature().dual()).dual();
            }
            name = original.name();
        }
        return new ComplexProperty(name, complexPropertySignature, newSubpaths);
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

}
