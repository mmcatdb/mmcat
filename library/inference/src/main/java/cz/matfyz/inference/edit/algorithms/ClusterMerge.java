package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.ArrayDeque;
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

import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;
import cz.matfyz.inference.edit.InferenceEditorUtils.KeysAndSignatures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
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

    private final AccessPathBuilder b = new AccessPathBuilder();

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
        final var newPart = findNewPart(data.clusterKeys.get(RND_CLUSTER_IDX), clusterRootKey);
        final var mapOldNewKey = addObjexes(newPart.keys(), oldClusterNames);
        addMorphisms(newPart.signatures(), mapOldNewKey, clusterRootKey);

        final var toDelete = findToDelete(clusterRootKey);
        InferenceEditorUtils.removeMorphismsAndObjexes(newSchema, toDelete);

        // finally add the _type and _value objexes
        addTypeObjexAndMorphism();
        addValueObjexAndMorphism();
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
            .map(key -> newMetadata.getObjex(key).label)
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

        final var combinedSb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            combinedSb.append(strings.get(i)).append((char) (i + 'a'));
        }

        final String combined = combinedSb.toString();
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

    private void traverseAndPerform(Key startKey, Key clusterRootKey, Consumer<SchemaObjex> objexConsumer, Consumer<SchemaMorphism> morphismConsumer) {
        Set<Key> visited = new HashSet<>();
        Queue<SchemaObjex> queue = new ArrayDeque<>();
        SchemaObjex startObjex = newSchema.getObjex(startKey);

        queue.add(startObjex);
        visited.add(startKey);
        objexConsumer.accept(startObjex);

        while (!queue.isEmpty()) {
            final SchemaObjex currentObjex = queue.poll();

            for (final SchemaMorphism morphism : newSchema.allMorphisms()) {
                if (morphism.dom().equals(currentObjex)) {
                    final SchemaObjex targetObjex = morphism.cod();
                    if (visited.add(targetObjex.key())) {
                        queue.add(targetObjex);
                        objexConsumer.accept(targetObjex);
                    }
                    morphismConsumer.accept(morphism);
                }

                if (
                    morphism.cod().equals(currentObjex) &&
                    !morphism.cod().key().equals(startKey) &&
                    !morphism.dom().key().equals(clusterRootKey)
                ) {
                    final SchemaObjex sourceObjex = morphism.dom();
                    if (visited.add(sourceObjex.key())) {
                        queue.add(sourceObjex);
                        objexConsumer.accept(sourceObjex);
                    }
                    morphismConsumer.accept(morphism);
                }
            }
        }
    }

    private KeysAndSignatures findNewPart(Key startKey, Key clusterRootKey) {
        final var output = new KeysAndSignatures();

        traverseAndPerform(startKey, clusterRootKey,
            objex -> output.add(objex.key()),
            morphism -> output.add(morphism.signature())
        );

        return output;
    }

    private Map<Key, Key> addObjexes(Set<Key> keys, List<String> oldClusterNames) {
        final Map<Key, Key> mapOldNewKey = new HashMap<>();

        for (final var key : keys) {
            final var objex = newSchema.getObjex(key);
            final var mo = newMetadata.getObjex(objex);

            Key newKey;
            if (oldClusterNames.contains(mo.label)) {
                newKey = InferenceEditorUtils.addObjexWithMetadata(newSchema, newMetadata, ObjexIds.empty(), newClusterName);
                newClusterKey = newKey;
            }
            else {
                newKey = InferenceEditorUtils.addObjexWithMetadata(newSchema, newMetadata, objex.ids(), mo.label);
            }

            mapOldNewKey.put(objex.key(), newKey);
        }

        return mapOldNewKey;
    }

    private void addMorphisms(Set<BaseSignature> signatures, Map<Key, Key> mapOldNewKey, Key clusterRootKey) {
        for (final var signature : signatures) {
            final var morphism = newSchema.getMorphism(signature);
            final SchemaObjex dom = newSchema.getObjex(mapOldNewKey.get(morphism.dom().key()));
            final BaseSignature newSignature = InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, dom, newSchema.getObjex(mapOldNewKey.get(morphism.cod().key())));
            mapOldNewSignature.put(signature, newSignature);
        }

        this.newClusterSignature = InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, newSchema.getObjex(newClusterKey), newSchema.getObjex(clusterRootKey)).dual();
    }

    private KeysAndSignatures findToDelete(Key clusterRootKey) {
        final var output = new KeysAndSignatures();

        for (final Key key : data.clusterKeys) {
            traverseAndPerform(key, clusterRootKey,
                objex -> output.add(objex.key()),
                morphism -> output.add(morphism.signature())
            );
        }

        for (final SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (morphism.dom().key().equals(clusterRootKey) && data.clusterKeys.contains(morphism.cod().key())) {
                final var codMetadata = newMetadata.getObjex(morphism.cod());
                mapOldClusterNameSignature.put(codMetadata.label, morphism.signature());
                output.add(morphism.signature());
            }
        }

        return output;
    }

    private void addTypeObjexAndMorphism() {
        final Key typeKey = InferenceEditorUtils.addObjexWithMetadata(newSchema, newMetadata, ObjexIds.empty(), TYPE_LABEL);
        this.newTypeSignature = InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, newSchema.getObjex(newClusterKey), newSchema.getObjex(typeKey));
    }

    private void addValueObjexAndMorphism() {
        final Key valueKey = InferenceEditorUtils.addObjexWithMetadata(newSchema, newMetadata, ObjexIds.empty(), VALUE_LABEL);
        this.newValueSignature = InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, newSchema.getObjex(newClusterKey), newSchema.getObjex(valueKey));
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
            final var randomClusterObjex = oldMetadata.getObjex(data.clusterKeys.get(RND_CLUSTER_IDX));
            final var signature = mapOldClusterNameSignature.get(randomClusterObjex.label);
            if (mapping.accessPath().getSubpathBySignature(signature) != null)
                return mapping;
        }

        throw MorphismNotFoundException.withMessage("Cluster Mapping has not been found");
    }

    private Mapping createMergedMapping(Mapping clusterMapping) {
        final ComplexProperty changedComplexProperty = changeComplexProperties(clusterMapping.accessPath());
        return clusterMapping.withSchemaAndPath(newSchema, changedComplexProperty);
    }

    private ComplexProperty changeComplexProperties(ComplexProperty clusterComplexProperty) {
        final var randomClusterObjex = oldMetadata.getObjex(data.clusterKeys.get(RND_CLUSTER_IDX));
        final var signature = mapOldClusterNameSignature.get(randomClusterObjex.label);
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
            }
            else {
                complexChanged = true;
            }

            if (!complexChanged || complexIsCluster) {
                for (Signature oldSignature : mapOldClusterNameSignature.values()) {
                    if (complexProperty != null && complexProperty.signature().equals(oldSignature)) {
                        complexProperty = null;
                        complexChanged = true;
                        complexIsCluster = true;
                        break;
                    }
                    else if (complexProperty != null) { // remove the old cluster members
                        AccessPath currentSubpath = complexProperty.getSubpathBySignature(oldSignature);
                        if (currentSubpath != null) {
                            complexProperty = complexProperty.minusSubpath(currentSubpath);
                            complexChanged = true;
                        }
                    }
                }
            }

            if (complexChanged) {
                final List<AccessPath> updatedSubpaths = complexProperty != null ? new ArrayList<>(complexProperty.subpaths()) : new ArrayList<>();
                final AccessPath newAccessPath = createNewComplexProperty(firstClusterAccessPath);
                updatedSubpaths.add(newAccessPath);

                final AccessPath resultAccessPath = complexProperty != null
                    ? b.complex(complexProperty.name(), complexProperty.signature(), updatedSubpaths.toArray(AccessPath[]::new))
                    : newAccessPath;

                newSubpaths.add(resultAccessPath);
            }
            else if (complexProperty != null) {
                newSubpaths.add(complexProperty);
            }
        }

        final AccessPathBuilder builder = new AccessPathBuilder();
        return builder.root(newSubpaths.toArray(AccessPath[]::new));
    }

    private AccessPath createNewComplexProperty(AccessPath original) {
        if (original instanceof SimpleProperty)
            return b.dynamic(this.newClusterSignature, newClusterName + "*", this.newTypeSignature, this.newValueSignature);

        final var newSubpaths = transformSubpaths(((ComplexProperty) original).subpaths());

        if (!mapOldNewSignature.containsKey(original.signature()) && !mapOldNewSignature.containsKey(original.signature().dual())) {
            // return b.complex(
            //     new DynamicName(newClusterSignature, newClusterName + "*"),
            //     newClusterSignature,
            //     newSubpaths
            // );
            // FIXME This is definitely wrong. The original code doesn't make sense (why is newClusterSignature repeated?), However, I don't have time to fix it now.
            throw new IllegalStateException("Signature mapping not found for complex property.");
        }

        Signature complexPropertySignature = mapOldNewSignature.get(original.signature());
        if (complexPropertySignature == null)
            // Meaning the original was an array and so the signature was dual.
            complexPropertySignature = mapOldNewSignature.get(original.signature().dual()).dual();

        return b.complex(original.name(), complexPropertySignature, newSubpaths);
    }

    private AccessPath[] transformSubpaths(Collection<AccessPath> originalSubpaths) {
        return originalSubpaths.stream()
            .map(this::transformSubpath)
            .toArray(AccessPath[]::new);
    }

    private AccessPath transformSubpath(AccessPath original) {
        if (original instanceof final SimpleProperty simpleProperty)
            return createNewSimpleProperty((SimpleProperty) simpleProperty);
        if (original instanceof final ComplexProperty complexProperty)
            return createNewComplexProperty((ComplexProperty) complexProperty);

        throw new IllegalArgumentException("Unknown AccessPath type");
    }

    private SimpleProperty createNewSimpleProperty(SimpleProperty original) {
        return b.simple(original.name(),mapOldNewSignature.get(original.signature()));
    }
}
