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
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
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
        List<Key> clusterKeys;

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

        /**
         * Default constructor initializing the data with default values.
         */
        public Data() {
            setId(null);
            setActive(false);
            this.clusterKeys = null;
        }

        /**
         * Gets the list of cluster keys.
         *
         * @return The list of keys representing clusters.
         */
        public List<Key> getClusterKeys() {
            return clusterKeys;
        }

        /**
         * Creates an instance of the {@code ClusterMerge} algorithm.
         *
         * @return A new instance of {@code ClusterMerge}.
         */
        @Override public ClusterMerge createAlgorithm() {
            return new ClusterMerge(this);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClusterMerge.class.getName());
    private static final String TYPE_LABEL = "_type";
    private static final int RND_CLUSTER_IDX = 0;

    private final Data data;

    private Key newClusterKey;
    private Signature newClusterSignature;
    private String newClusterName;
    private Signature newTypeSignature;

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
     * Assumes that the startKey can have ingoing and outgoing edges,
     * with one ingoing edge from the root cluster.
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

    /**
     * Finds the root key of the cluster within the schema. The cluster root is the object,
     * which has an outgoing morphism to all the cluster keys.
     *
     * @param schema The schema category in which to find the cluster root.
     * @return The key of the cluster root.
     */
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

    /**
     * Retrieves the names of the old clusters based on the keys provided in the data.
     *
     * @return A list of old cluster names.
     */
    private List<String> getOldClusterNames() {
        final List<String> oldClusterNames = new ArrayList<>();
        for (final Key key : data.clusterKeys)
            oldClusterNames.add(newMetadata.getObject(key).label);

        return oldClusterNames;
    }

    /**
     * Finds the repeating pattern among a list of strings.
     *
     * @param strings The list of strings to analyze.
     * @return The longest common substring found in the list.
     */
    private String findRepeatingPattern(List<String> strings) {
        if (strings == null || strings.isEmpty())
            return "";

        final List<String> normalizedStrings = new ArrayList<>();
        for (final String str : strings)
            normalizedStrings.add(normalizeString(str));

        return findLongestCommonSubstring(normalizedStrings);
    }

    /**
     * Normalizes a string by removing specific special characters.
     *
     * @param str The string to normalize.
     * @return The normalized string.
     */
    private String normalizeString(String str) {
        return str.replaceAll("[._:,-]", "");
    }

    /**
     * Finds the longest common substring among a list of strings.
     *
     * @param strings The list of strings to analyze.
     * @return The longest common substring.
     */
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

    /**
     * Traverses the schema objects and morphisms starting from a given key
     * and applies the specified consumers to each schema object and morphism.
     *
     * @param startKey The key to start traversal.
     * @param clusterRootKey The key of the cluster root.
     * @param objectConsumer A consumer to apply to each schema object.
     * @param morphismConsumer A consumer to apply to each schema morphism.
     */
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

    /**
     * Traverses the schema starting from a given key and builds a new schema part.
     *
     * @param startKey The key to start traversal.
     * @param clusterRootKey The key of the cluster root.
     */
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

    /**
     * Traverses the schema and identifies objects and morphisms to delete.
     *
     * @param startKey The key to start traversal.
     * @param clusterRootKey The key of the cluster root.
     */
    private void traverseAndFind(Key startKey, Key clusterRootKey) {
        traverseAndPerform(startKey, clusterRootKey,
            object -> keysToDelete.add(object.key()),
            morphism -> signaturesToDelete.add(morphism.signature())
        );
    }

    /**
     * Adds a new schema part to the current schema, mapping old and new keys and morphisms.
     *
     * @param oldClusterNames A list of old cluster names.
     * @param clusterRootKey The key of the cluster root.
     */
    private void addSchemaPart(List<String> oldClusterNames, Key clusterRootKey) {
        final Map<Key, Key> mapOldNewKey = addObjects(oldClusterNames);
        addMorphisms(mapOldNewKey, clusterRootKey);
        //addTypeObjectAndMorphism();
    }

    /**
     * Adds objects to the schema based on old cluster names.
     *
     * @param oldClusterNames A list of old cluster names.
     * @return A map of old keys to new keys.
     */
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

    /**
     * Adds morphisms to the schema based on the mapping of old and new keys.
     *
     * @param mapOldNewKey A map of old keys to new keys.
     * @param clusterRootKey The key of the cluster root.
     */
    private void addMorphisms(Map<Key, Key> mapOldNewKey, Key clusterRootKey) {
        for (final SchemaMorphism morphism : newSchemaPart.allMorphisms()) {
            final SchemaObject dom = newSchema.getObject(mapOldNewKey.get(morphism.dom().key()));
            final Signature newSig = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, newSchema.getObject(mapOldNewKey.get(morphism.cod().key())));
            mapOldNewSignature.put(morphism.signature(), newSig);
        }
        newClusterSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(clusterRootKey), newSchema.getObject(newClusterKey));
    }

    /**
     * Adds a new type object and morphism to the schema.
     */
    private void addTypeObjectAndMorphism() {
        final Key typeKey = InferenceEditorUtils.createAndAddObject(newSchema, newMetadata, ObjectIds.createValue(), TYPE_LABEL);
        newTypeSignature = InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, newSchema.getObject(newClusterKey), newSchema.getObject(typeKey));
    }

    /**
     * Finds morphisms and objects to delete based on the provided cluster root key.
     *
     * @param clusterRootKey The key of the cluster root.
     */
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

    /**
     * Applies the mapping edit to a list of mappings.
     *
     * @param mappings The list of mappings to edit.
     * @return The updated list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        LOGGER.info("Applying Cluster Edit on Mapping...");

        Mapping clusterMapping = findClusterMapping(mappings);

        Mapping mergedMapping = createMergedMapping(clusterMapping);

        //return InferenceEditorUtils.updateMappings(mappings, Arrays.asList(clusterMapping), mergedMapping);
        return mappings;
    }

    /**
     * Finds the cluster mapping from the provided list of mappings.
     *
     * @param mappings The list of mappings to search.
     * @return The mapping corresponding to the cluster.
     * @throws NotFoundException if the cluster mapping is not found.
     */
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

    /**
     * Creates a new merged mapping based on the cluster mapping provided.
     *
     * @param clusterMapping The cluster mapping to merge.
     * @return The new merged mapping.
     */
    private Mapping createMergedMapping(Mapping clusterMapping) {
        ComplexProperty changedComplexProperty = changeComplexProperties(clusterMapping.accessPath());
        return new Mapping(newSchema, clusterMapping.rootObject().key(), clusterMapping.kindName(), changedComplexProperty, clusterMapping.primaryKey());
    }

    /**
     * Changes complex properties within a cluster complex property.
     *
     * @param clusterComplexProperty The complex property of the cluster to change.
     * @return The updated complex property.
     */
    private ComplexProperty changeComplexProperties(ComplexProperty clusterComplexProperty) {
        final var randomClusterObject = oldMetadata.getObject(data.clusterKeys.get(RND_CLUSTER_IDX));
        final var signature = mapOldClusterNameSignature.get(randomClusterObject.label);
        final AccessPath firstClusterAccessPath = clusterComplexProperty.getSubpathBySignature(signature);

        return getNewComplexProperty(firstClusterAccessPath, clusterComplexProperty);
    }

    /**
     * Gets a new complex property based on the provided access path and complex property.
     *
     * @param firstClusterAccessPath The first cluster access path.
     * @param clusterComplexProperty The cluster complex property.
     * @return The new complex property.
     */
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
                List<AccessPath> updatedSubpaths = complexProperty != null ? new ArrayList<>(complexProperty.subpaths()) : new ArrayList<>();
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

    /**
     * Creates a new complex property based on the original complex property.
     *
     * @param original The original complex property.
     * @return The new complex property.
     */
    public ComplexProperty createNewComplexProperty(ComplexProperty original) {
        final List<AccessPath> newSubpaths = transformSubpaths(original.subpaths());
        Name name;
        Signature complexPropertySignature;

        if (!mapOldNewSignature.containsKey(original.signature()) && !mapOldNewSignature.containsKey(original.signature().dual())) {
            complexPropertySignature = newClusterSignature;
            name = new DynamicName(complexPropertySignature);
        } else {
            complexPropertySignature = mapOldNewSignature.get(original.signature());
            if (complexPropertySignature == null) { // meaning the original was an array object and so the signature was dual
                complexPropertySignature = mapOldNewSignature.get(original.signature().dual()).dual();
            }
            name = original.name();
        }
        return new ComplexProperty(name, complexPropertySignature, newSubpaths);
    }

    /**
     * Transforms the subpaths of a complex property into new subpaths.
     *
     * @param originalSubpaths The original subpaths to transform.
     * @return A list of transformed subpaths.
     */
    private List<AccessPath> transformSubpaths(Collection<AccessPath> originalSubpaths) {
        return originalSubpaths.stream()
            .map(this::transformSubpath)
            .collect(Collectors.toList());
    }

    /**
     * Transforms a single access path into a new access path.
     *
     * @param original The original access path to transform.
     * @return The transformed access path.
     */
    private AccessPath transformSubpath(AccessPath original) {
        if (original instanceof SimpleProperty) {
            return createNewSimpleProperty((SimpleProperty) original);
        } else if (original instanceof ComplexProperty) {
            return createNewComplexProperty((ComplexProperty) original);
        } else {
            throw new IllegalArgumentException("Unknown AccessPath type");
        }
    }

    /**
     * Creates a new simple property based on the original simple property.
     *
     * @param original The original simple property.
     * @return The new simple property.
     */
    private SimpleProperty createNewSimpleProperty(SimpleProperty original) {
        return new SimpleProperty(original.name(), mapOldNewSignature.get(original.signature()));
    }
}
