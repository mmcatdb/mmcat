package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code ClusterMerge} class implements an algorithm to merge clusters within a schema.
 * It extends the {@link InferenceEditAlgorithm} and operates on schema categories to modify
 * the structure and metadata according to the specified cluster merging rules.
 */
public class ClusterMerge extends InferenceEditAlgorithm {

    /**
     * Represents the data model used by the ClusterMerge algorithm, including cluster keys
     * and activity status.
     */
    public static class Data extends InferenceEdit {

        @JsonProperty("clusterKeys")
        @Nullable public List<Key> clusterKeys;

        /**
         * Constructor for deserialization with JSON properties.
         *
         * @param id The ID of the edit.
         * @param isActive The active status of the edit.
         * @param clusterKeys The list of keys representing clusters to merge.
         */
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

        /**
         * Creates an instance of the {@code ClusterMerge} algorithm.
         */
        @Override public ClusterMerge createAlgorithm() {
            return new ClusterMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClusterMerge.class.getName());
    private static final String TYPE_LABEL = "_type";
    private static final String VALUE_LABEL = "_value";
    private static final int RND_CLUSTER_IDX = 0;

    private final Data data;

    private Key newClusterKey;
    private Signature newClusterSignature;
    private String newClusterName;
    private Signature newTypeSignature;
    private Signature newValueSignature;

    private Map<String, Signature> mapOldClusterNameSignature = new HashMap<>(); // this maps the original cluster names to their original signatures
    private Map<Signature, Signature> mapOldNewSignature = new HashMap<>(); // this maps the original signatures to the new signatures

    private SchemaCategory newSchemaPart;
    private MetadataCategory newMetadataPart;

    /**
     * Constructs a {@code ClusterMerge} instance with the specified data.
     *
     * @param data The data model containing cluster information and merge settings.
     */
    public ClusterMerge(Data data) {
        this.data = data;
    }

    /**
     * Applies the cluster merging algorithm to the schema category.
     * Assumes that the startKey can have ingoing and outgoing edges, with one ingoing edge from the root cluster.
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

        // finally add the _type and _value object
        addTypeObjectAndMorphism();
        addValueObjectAndMorphism();
    }

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
        return data.clusterKeys.stream()
            .map(key -> newMetadata.getObject(key).label)
            .toList();
    }

    private String findRepeatingPattern(List<String> strings) {
        if (strings.isEmpty())
            return "";

        return findLongestCommonSubstring(strings);
    }

    private String findLongestCommonSubstring(List<String> strings) {
        if (strings.isEmpty())
            return "";

        final StringBuilder combinedBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            combinedBuilder.append(strings.get(i)).append((char) (i + 'a'));
        }

        final String combined = combinedBuilder.toString();
        final Integer[] suffixArray = buildSuffixArray(combined);
        final int[] lcp = buildLCP(combined, suffixArray);

        int maxLength = 0;
        int index = 0;

        for (int i = 1; i < lcp.length; i++) {
            if (
                lcp[i] > maxLength &&
                getStringIndex(suffixArray[i], strings) != getStringIndex(suffixArray[i - 1], strings)
            ) {
                String candidate = combined.substring(suffixArray[i], suffixArray[i] + lcp[i]);
                if (isCommonSubstring(candidate, strings)) {
                    maxLength = lcp[i];
                    index = suffixArray[i];
                }
            }
        }

        return combined.substring(index, index + maxLength);
    }

    private boolean isCommonSubstring(String substring, List<String> strings) {
        for (String string : strings) {
            if (!string.contains(substring)) {
                return false;
            }
        }
        return true;
    }

    private int getStringIndex(int suffixIndex, List<String> strings) {
        int length = 0;
        for (int i = 0; i < strings.size(); i++) {
            length += strings.get(i).length() + 1;
            if (suffixIndex < length)
                return i;
        }
        return -1;
    }

    private Integer[] buildSuffixArray(String s) {
        final int n = s.length();
        final Integer[] suffixArray = new Integer[n];
        for (int i = 0; i < n; i++)
            suffixArray[i] = i;

        Arrays.sort(suffixArray, (a, b) -> s.substring(a).compareTo(s.substring(b)));
        return suffixArray;
    }

    private int[] buildLCP(String s, Integer[] suffixArray) {
        final int n = s.length();
        final int[] rank = new int[n];
        for (int i = 0; i < n; i++)
            rank[suffixArray[i]] = i;

        final int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = suffixArray[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h))
                    h++;
                lcp[rank[i]] = h;
                if (h > 0)
                    h--;
            }
        }
        return lcp;
    }

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
        this.newClusterSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(newClusterKey), newSchema.getObject(clusterRootKey), false, null).dual();
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

    private void addTypeObjectAndMorphism() {
        final Key typeKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, ObjectIds.createValue(), TYPE_LABEL);
        this.newTypeSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(newClusterKey), newSchema.getObject(typeKey));
    }

    private void addValueObjectAndMorphism() {
        final Key valueKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, ObjectIds.createValue(), VALUE_LABEL);
        this.newValueSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(newClusterKey), newSchema.getObject(valueKey));
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Cluster Edit on Mapping...");

        Mapping clusterMapping = findClusterMapping(mappings);

        Mapping mergedMapping = createMergedMapping(clusterMapping);

        return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(clusterMapping), mergedMapping);
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
        return clusterMapping.withSchema(newSchema, changedComplexProperty, clusterMapping.primaryKey());
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
            @Nullable ComplexProperty complexProperty = null;

            if (subpath instanceof ComplexProperty tempComplexProperty) {
                complexProperty = tempComplexProperty;
            }

            if (!(mapOldClusterNameSignature.values().contains(subpath.signature()))) {
                newSubpaths.add(subpath);
                continue;
            } else {
                complexChanged = true;
            }

            if (!complexChanged || complexIsCluster) {
                for (Signature oldSignature : mapOldClusterNameSignature.values()) {
                    if (complexProperty != null && complexProperty.signature().equals(oldSignature)) {
                        complexProperty = null;
                        complexChanged = true;
                        complexIsCluster = true;
                        break;
                    } else if (complexProperty != null) { // remove the old cluster members
                        AccessPath currentSubpath = complexProperty.getSubpathBySignature(oldSignature);
                        if (currentSubpath != null) {
                            complexProperty = complexProperty.minusSubpath(currentSubpath);
                            complexChanged = true;
                        }
                    }
                }
            }

            if (complexChanged) {
                List<AccessPath> updatedSubpaths = complexProperty != null ? new ArrayList<>(complexProperty.subpaths()) : new ArrayList<>();
                AccessPath newAccessPath = createNewComplexProperty(firstClusterAccessPath);
                updatedSubpaths.add(newAccessPath);

                AccessPath resultAccessPath = complexProperty != null
                    ? new ComplexProperty(complexProperty.name(), complexProperty.signature(), updatedSubpaths)
                    : newAccessPath;

                newSubpaths.add(resultAccessPath);
            } else if (complexProperty != null) {
                newSubpaths.add(complexProperty);
            }
        }

        final AccessPathBuilder builder = new AccessPathBuilder();
        return builder.root(newSubpaths.toArray(AccessPath[]::new));
    }

    private AccessPath createNewComplexProperty(AccessPath original) {
        if (original instanceof SimpleProperty) {
            Signature dynamicNameSignature = this.newClusterSignature.concatenate(this.newTypeSignature);
            Signature valueSignature = this.newClusterSignature.concatenate(this.newValueSignature);
            return new SimpleProperty(new DynamicName(dynamicNameSignature, newClusterName + "*"), valueSignature);
        }

        final var newSubpaths = transformSubpaths(((ComplexProperty) original).subpaths());

        if (!mapOldNewSignature.containsKey(original.signature()) && !mapOldNewSignature.containsKey(original.signature().dual())) {
            return new ComplexProperty(
                new DynamicName(newClusterSignature, newClusterName + "*"),
                newClusterSignature,
                newSubpaths
            );
        }

        Signature complexPropertySignature = mapOldNewSignature.get(original.signature());
        if (complexPropertySignature == null)
            // Meaning the original was an array object and so the signature was dual.
            complexPropertySignature = mapOldNewSignature.get(original.signature().dual()).dual();

        return new ComplexProperty(original.name(), complexPropertySignature, newSubpaths);
    }

    private List<AccessPath> transformSubpaths(Collection<AccessPath> originalSubpaths) {
        return originalSubpaths.stream()
            .map(this::transformSubpath)
            .toList();
    }

    private AccessPath transformSubpath(AccessPath original) {
        if (original instanceof SimpleProperty)
            return createNewSimpleProperty((SimpleProperty) original);
        if (original instanceof ComplexProperty)
            return createNewComplexProperty((ComplexProperty) original);

        throw new IllegalArgumentException("Unknown AccessPath type");
    }

    private SimpleProperty createNewSimpleProperty(SimpleProperty original) {
        return new SimpleProperty(original.name(), mapOldNewSignature.get(original.signature()));
    }
}
