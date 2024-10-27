package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;
import cz.matfyz.inference.edit.PatternSegment;

import java.util.*;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: this class is a one big WIP + needs refactoring

/**
 * The {@code RecursionMerge} class implements an algorithm for merging recursive patterns
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides functionality
 * to detect and merge recursive structures.
 */
public class RecursionMerge extends InferenceEditAlgorithm {

    /**
     * Data class representing the pattern and state needed for recursion merging.
     */
    public static class Data extends InferenceEdit {

        @JsonProperty("pattern")
        List<PatternSegment> pattern;

        /**
         * Constructs a {@code Data} instance with specified parameters.
         *
         * @param id The ID of the edit.
         * @param isActive The active status of the edit.
         * @param pattern The pattern segments to be used for recursion detection and merging.
         */
        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("pattern") List<PatternSegment> pattern) {
            setId(id);
            setActive(isActive);
            this.pattern = pattern;
        }

        /**
         * Default constructor initializing data with default values.
         */
        public Data() {
            setId(null);
            setActive(false);
            this.pattern = null;
        }

        /**
         * Gets the pattern segments used for recursion detection and merging.
         *
         * @return The list of pattern segments.
         */
        public List<PatternSegment> getPattern() {
            return pattern;
        }

        /**
         * Creates an instance of the {@code RecursionMerge} algorithm.
         *
         * @return A new instance of {@code RecursionMerge}.
         */
        @Override public RecursionMerge createAlgorithm() {
            return new RecursionMerge(this);
        }
    }

    private final Data data;

    private static final Logger LOGGER = Logger.getLogger(RecursionMerge.class.getName());
    private static final String FORWARD = "->";
    private static final String BACKWARD = "<-";
    private static final String RECURSIVE_FORWARD = "@->";
    private static final String RECURSIVE_BACKWARD = "@<-";
    private static final String RECURSIVE_MARKER = "@";

    private List<PatternSegment> adjustedPattern;
    private Map<PatternSegment, Set<SchemaObject>> mapPatternObjects = new HashMap<>();

    /**
     * Constructs a {@code RecursionMerge} instance with the specified data.
     *
     * @param data The data model containing pattern information and merge settings.
     */
    public RecursionMerge(Data data) {
        this.data = data;
    }

    /*
     * Applies the primary key merging algorithm to the schema category.
     * Assumptions: Morphisms in the pattern are only among the elements of the pattern.
     * Pattern always starts and ends in the same object (however the occurrence does not have to end in this object).
     * For now, we assume that the pattern needs to occur at least once in its full length to count as a full match.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Recursion Edit on Schema Category...");

        adjustPattern();

        List<List<SchemaObject>> occurences = findAdjustedPatternOccurences();

        bridgeOccurences(occurences);

        findMorphismsAndObjectsToDelete(occurences);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchema, signaturesToDelete, keysToDelete);

        createRecursiveMorphisms(occurences);
    }

    /**
     * Adjusts the pattern to account for recursive segments.
     */
    private void adjustPattern() {
        List<PatternSegment> newPattern = new ArrayList<>();
        int i = 0;
        boolean lastAdjusted = false;
        while (i < data.pattern.size() - 1) {
            PatternSegment currentSegment = data.pattern.get(i);
            PatternSegment nextSegment = data.pattern.get(i + 1);

            if (currentSegment.nodeName().equals(nextSegment.nodeName())) {
                newPattern.add(new PatternSegment(currentSegment.nodeName(), RECURSIVE_MARKER + nextSegment.direction()));
                i = i + 2;
                lastAdjusted = true;
            } else {
                newPattern.add(currentSegment);
                i++;
                lastAdjusted = false;
            }
        }
        if (!lastAdjusted) {
            newPattern.add(data.pattern.get(data.pattern.size() - 1));
        }

        adjustedPattern = newPattern;
    }

    /**
     * Finds occurrences of the adjusted pattern in the schema.
     *
     * @return A list of lists, where each inner list represents a sequence of schema objects matching the pattern.
     */
    private List<List<SchemaObject>> findAdjustedPatternOccurences() {
        List<List<SchemaObject>> result = new ArrayList<>();
        for (SchemaObject node : newSchema.allObjects())
            dfsFind(node, 0, new ArrayList<>(), result, false, false, new HashSet<>());

        return cleanOccurences(result);
    }

    /**
     * Performs a depth-first search to find matches of the adjusted pattern.
     *
     * @param currentNode The current node being explored.
     * @param patternIndex The current index in the pattern being matched.
     * @param currentPath The current path of schema objects being explored.
     * @param result The list of all matching paths found.
     * @param fullMatch Flag indicating if a full match has been found.
     * @param processing Flag indicating if the current path is still being processed.
     * @param recursiveNodes Set of schema objects that are part of recursive segments.
     */
    private void dfsFind(
        SchemaObject currentNode,
        int patternIndex,
        List<SchemaObject> currentPath,
        List<List<SchemaObject>> result,
        boolean fullMatch,
        boolean processing,
        Set<SchemaObject> recursiveNodes
    ) {
        if (currentNode == null || patternIndex >= adjustedPattern.size())
            return;

        PatternSegment currentSegment = adjustedPattern.get(patternIndex);
        final var currentNodeLabel = newMetadata.getObject(currentNode).label;

        if (!currentNodeLabel.equals(currentSegment.nodeName())) {
            if (foundFullMatch(currentPath)) {
                result.add(new ArrayList<>(currentPath));
                dfsFind(currentNode, 0, new ArrayList<>(), result, false, false, recursiveNodes);
            }
            return;
        }

        currentPath.add(currentNode);

        final Set<SchemaObject> objects = mapPatternObjects.isEmpty() || mapPatternObjects.get(currentSegment) == null
            ? new HashSet<>()
            : mapPatternObjects.get(currentSegment);

        objects.add(currentNode);
        mapPatternObjects.put(currentSegment, objects);

        if (patternIndex == adjustedPattern.size() - 1) {
            fullMatch = true;
            patternIndex = 0;
            currentSegment = adjustedPattern.get(patternIndex);
            SchemaObject nextNode = findNextNode(newSchema, currentNode, currentSegment);

            if (nextNode != null)
                dfsFind(nextNode, 1, currentPath, result, fullMatch, processing, recursiveNodes);
        } else {
            if (currentSegment.direction().equals(FORWARD) || currentSegment.direction().equals(RECURSIVE_FORWARD)) {
                List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                for (SchemaMorphism morphism : newSchema.allMorphisms())
                    if (morphism.dom().equals(currentNode))
                        morphismsToProcess.add(morphism);

                if (currentSegment.direction().equals(RECURSIVE_FORWARD)) {
                    if (!containsRecursiveAlready(recursiveNodes, currentNodeLabel))
                        recursiveNodes.add(currentNode);

                    processing = true;
                }

                // FIXME This method is way too long and nested. It should be decomposed into smaller methods. If it needs to track a lot of variables, it might be better to refactor it into a class and use private variables instead of passing them as arguments.

                for (SchemaMorphism m : morphismsToProcess) {
                    if (currentSegment.direction().equals(RECURSIVE_FORWARD) && newMetadata.getObject(m.cod()).label.equals(currentNodeLabel)) {
                        dfsFind(m.cod(), patternIndex, currentPath, result, fullMatch, processing, recursiveNodes);
                        if (recursiveNodes.contains(currentNode))
                            processing = false;
                    }
                    else {
                        dfsFind(m.cod(), patternIndex + 1, currentPath, result, fullMatch, processing, recursiveNodes);
                        if (!recursiveNodes.contains(currentNode))
                            break;

                        processing = false;
                    }
                }
            }
            if (currentSegment.direction().equals(BACKWARD) || currentSegment.direction().equals(RECURSIVE_BACKWARD)) {
                List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                for (SchemaMorphism morphism : newSchema.allMorphisms())
                    if (morphism.cod().equals(currentNode))
                        morphismsToProcess.add(morphism);

                for (SchemaMorphism m : morphismsToProcess) {
                    final var nextIndex = currentSegment.direction().equals(RECURSIVE_BACKWARD) && newMetadata.getObject(m.dom()).label.equals(currentNodeLabel)
                        ? patternIndex
                        : patternIndex + 1;

                    dfsFind(m.dom(), nextIndex, currentPath, result, fullMatch, processing, recursiveNodes);

                    processing = false;
                }

            } else if (foundFullMatch(currentPath) && !processing && !occurenceAlreadyFound(result, currentPath)) {
                result.add(new ArrayList<>(currentPath));
                dfsFind(currentNode, 0, new ArrayList<>(), result, false, false, recursiveNodes);
            }
        }

        if (!foundFullMatch(currentPath) && !processing)
            currentPath.remove(currentPath.size() - 1);
    }

    /**
     * Checks if a recursive node already exists in the set.
     *
     * @param recursiveNodes The set of recursive nodes.
     * @param currentNodeLabel The label of the current node to check.
     * @return {@code true} if the recursive node already exists; {@code false} otherwise.
     */
    public boolean containsRecursiveAlready(Set<SchemaObject> recursiveNodes, String currentNodeLabel) {
        for (final SchemaObject object : recursiveNodes)
            if (newMetadata.getObject(object).label.equals(currentNodeLabel))
                return true;

        return false;
    }

    /**
     * Cleans the list of occurrences by removing duplicates and merging subsequences.
     *
     * @param listOfLists The list of occurrences to clean.
     * @return A cleaned list of occurrences.
     */
    public static List<List<SchemaObject>> cleanOccurences(List<List<SchemaObject>> listOfLists) {
        List<List<SchemaObject>> result = new ArrayList<>();

        for (int i = 0; i < listOfLists.size(); i++) {
            List<SchemaObject> currentList = listOfLists.get(i);
            boolean isMergedOrSubsequence = false;

            for (int j = 0; j < result.size(); j++) {
                List<SchemaObject> existingList = result.get(j);

                if (isSublist(existingList, currentList)) {
                    isMergedOrSubsequence = true;
                    break;
                } else if (isSublist(currentList, existingList)) {
                    result.set(j, currentList);
                    isMergedOrSubsequence = true;
                    break;
                } else if (canMerge(existingList, currentList)) {
                    List<SchemaObject> mergedList = mergeLists(existingList, currentList);
                    result.set(j, mergedList);
                    isMergedOrSubsequence = true;
                    break;
                }
            }

            if (!isMergedOrSubsequence) {
                result.add(currentList);
            }
        }

        return result;
    }

    /**
     * Checks if one list is a sublist of another list.
     *
     * @param list The main list to check against.
     * @param sublist The potential sublist.
     * @return {@code true} if the sublist is found in the list; {@code false} otherwise.
     */
    private static boolean isSublist(List<SchemaObject> list, List<SchemaObject> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }

    /**
     * Checks if two lists can be merged based on overlapping elements.
     *
     * @param list1 The first list to check.
     * @param list2 The second list to check.
     * @return {@code true} if the lists can be merged; {@code false} otherwise.
     */
    private static boolean canMerge(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = Math.min(list1.size(), list2.size());

        for (int i = 1; i <= overlapSize; i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                return true;

        return false;
    }

    /**
     * Merges two lists based on overlapping elements.
     *
     * @param list1 The first list.
     * @param list2 The second list.
     * @return The merged list.
     */
    private static List<SchemaObject> mergeLists(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = 0;

        for (int i = 1; i <= Math.min(list1.size(), list2.size()); i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                overlapSize = i;

        List<SchemaObject> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2.subList(overlapSize, list2.size()));

        return mergedList;
    }

    /**
     * Checks if the current path represents a full match of the adjusted pattern.
     *
     * @param currentPath The current path of schema objects being checked.
     * @return {@code true} if a full match is found; {@code false} otherwise.
     */
    private boolean foundFullMatch(List<SchemaObject> currentPath) {
        return currentPath.size() >= adjustedPattern.size() && containsPatternSequence(currentPath);
    }

    /**
     * Checks if the given list contains the adjusted pattern sequence.
     *
     * @param list The list to check.
     * @return {@code true} if the pattern sequence is found; {@code false} otherwise.
     */
    private boolean containsPatternSequence(List<SchemaObject> list) {
        if (adjustedPattern.size() > list.size())
            return false;

        for (int i = 0; i <= list.size() - adjustedPattern.size(); i++) {
            boolean foundSublist = true;

            for (int j = 0; j < adjustedPattern.size(); j++) {
                if (!newMetadata.getObject(list.get(i + j)).label.equals(adjustedPattern.get(j).nodeName())) {
                    foundSublist = false;
                    break;
                }
            }

            if (foundSublist)
                return true;
        }

        return false;
    }

    /**
     * Checks if a given occurrence has already been found.
     *
     * @param result The list of all found occurrences.
     * @param currentPath The current path to check.
     * @return {@code true} if the occurrence has already been found; {@code false} otherwise.
     */
    private boolean occurenceAlreadyFound(List<List<SchemaObject>> result, List<SchemaObject> currentPath) {
        if (result.isEmpty())
            return false;

        for (final List<SchemaObject> r : result) {
            if (r.size() != currentPath.size())
                continue;

            boolean diff = false;
            for (int i = 0; i < r.size(); i++)
                if (!r.get(i).equals(currentPath.get(i)))
                    diff = true;

            if (!diff)
                return true;
        }

        return false;
    }

    /**
     * Finds the next node to traverse to based on the current segment direction.
     *
     * @param schema The schema category containing the nodes and morphisms.
     * @param currentNode The current node being explored.
     * @param currentSegment The current pattern segment.
     * @return The next schema object to traverse to, or {@code null} if none is found.
     */
    private SchemaObject findNextNode(SchemaCategory schema, SchemaObject currentNode, PatternSegment currentSegment) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (currentSegment.direction().equals(FORWARD) && morphism.dom().equals(currentNode)) {
                return morphism.cod();

            // FIXME There is no need for `else` if the `if` branch contains a `return` statement - because if it's true, there is no need to check the `else` branch.
            } else if (currentSegment.direction().equals(BACKWARD) && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            } else if (currentSegment.direction().equals(RECURSIVE_FORWARD) && morphism.dom().equals(currentNode)) {
                return morphism.cod();
            } else if (currentSegment.direction().equals(RECURSIVE_BACKWARD) && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            }
        }
        return null;
    }

    /**
     * Bridges the occurrences by creating new morphisms between pattern start and end nodes.
     *
     * @param occurences The list of pattern occurrences to bridge.
     */
    private void bridgeOccurences(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastOccurence = occurence.get(occurence.size() - 2);
            SchemaObject lastOccurence = occurence.get(occurence.size() - 1);

            List<SchemaMorphism> otherMorphisms = findOtherMorphisms(lastOccurence, oneBeforeLastOccurence);
            createNewOtherMorphisms(lastOccurence, firstInPattern, otherMorphisms);
        }
    }

    /**
     * Finds morphisms connected to a specific node that are not part of a given path.
     *
     * @param node The node to find morphisms for.
     * @param previous The previous node in the path to exclude from the search.
     * @return A list of morphisms that connect to the node but are not part of the specified path.
     */
    private List<SchemaMorphism> findOtherMorphisms(SchemaObject node, SchemaObject previous) {
        List<SchemaMorphism> morphisms = new ArrayList<>();
        for (SchemaMorphism morphism : newSchema.allMorphisms()) {
            if ((morphism.cod().equals(node) && !morphism.dom().equals(previous)) ||
                (morphism.dom().equals(node) && !morphism.cod().equals(previous))) {
                morphisms.add(morphism);
            }
        }
        return morphisms;
    }

    /**
     * Creates new morphisms connecting the last occurrence to the first in the pattern.
     *
     * @param lastOccurence The last occurrence in the pattern.
     * @param firstInPattern The first occurrence in the pattern.
     * @param otherMorphisms The list of other morphisms to create new connections for.
     */
    private void createNewOtherMorphisms(SchemaObject lastOccurence, SchemaObject firstInPattern, List<SchemaMorphism> otherMorphisms) {
        if (otherMorphisms == null)
            return;

        for (SchemaMorphism morphism : otherMorphisms) {
            SchemaObject dom = firstInPattern;
            SchemaObject cod = morphism.cod();
            if (!morphism.dom().equals(lastOccurence)) {
                dom = morphism.dom();
                cod = firstInPattern;
            }
            InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, dom, cod);
        }
    }

    /**
     * Finds morphisms and objects to delete based on pattern occurrences.
     *
     * @param occurences The list of pattern occurrences to analyze.
     */
    private void findMorphismsAndObjectsToDelete(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            for (int i = adjustedPattern.size() - 1; i < occurence.size(); i++) {
                SchemaObject schemaObject = occurence.get(i);
                keysToDelete.add(schemaObject.key());
                signaturesToDelete.addAll(findSignaturesForObject(schemaObject));
            }
        }
    }

    /**
     * Finds all signatures associated with a schema object.
     *
     * @param schemaObject The schema object to find signatures for.
     * @return A list of signatures associated with the schema object.
     */
    private List<Signature> findSignaturesForObject(SchemaObject schemaObject) {
        List<Signature> signatures = new ArrayList<>();
        for (SchemaMorphism morphism : newSchema.allMorphisms())
            if (morphism.dom().equals(schemaObject) || morphism.cod().equals(schemaObject))
                signatures.add(morphism.signature());

        // FIXME This can be done much more clearly by the filter method.

        return signatures;
    }

    /**
     * Creates recursive morphisms for pattern occurrences.
     *
     * @param occurences The list of pattern occurrences to create recursive morphisms for.
     */
    private void createRecursiveMorphisms(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastInPattern = occurence.get(adjustedPattern.size() - 2);
            InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, oneBeforeLastInPattern, firstInPattern);
        }

        createRepetitiveMorphisms(occurences);
    }

    /**
     * Creates repetitive morphisms for segments marked as recursive.
     *
     * @param occurences The list of pattern occurrences to analyze.
     */
    private void createRepetitiveMorphisms(List<List<SchemaObject>> occurences) {
        for (PatternSegment segment : adjustedPattern) {
            if (!isRepetitive(segment))
                continue;

            for (SchemaObject object : mapPatternObjects.get(segment))
                if (!keysToDelete.contains(object.key()) && inAnyOccurence(occurences, object))
                    InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, object, object);
        }
    }

    /**
     * Checks if a pattern segment is marked as recursive.
     *
     * @param segment The pattern segment to check.
     * @return {@code true} if the segment is recursive; {@code false} otherwise.
     */
    private boolean isRepetitive(PatternSegment segment) {
        return segment.direction().contains(RECURSIVE_MARKER);
    }

    /**
     * Checks if a schema object is part of any pattern occurrence.
     *
     * @param occurences The list of pattern occurrences.
     * @param schemaObjectToFind The schema object to search for.
     * @return {@code true} if the schema object is part of any occurrence; {@code false} otherwise.
     */
    private boolean inAnyOccurence(List<List<SchemaObject>> occurences, SchemaObject schemaObjectToFind) {
        for (List<SchemaObject> occurence : occurences)
            for (SchemaObject schemaObject : occurence)
                if (schemaObject.equals(schemaObjectToFind))
                    return true;

        return false;
    }

    /**
     * Applies the mapping edit to a list of mappings.
     *
     * @param mappings The list of mappings to edit.
     * @return The updated list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        // TODO: adjust the mapping
        return mappings;
    }

}
