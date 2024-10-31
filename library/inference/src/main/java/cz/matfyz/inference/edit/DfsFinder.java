package cz.matfyz.inference.edit;

import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.core.schema.SchemaMorphism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code DfsFinder} class performs depth-first search (DFS) to find occurrences
 * of a specified pattern within a schema. It manages the traversal state and
 * encapsulates the logic for processing both forward and backward directions in the schema.
 */
public class DfsFinder {

    private RecursionMerge recursionMerge;
    private List<SchemaObject> currentPath;
    // FIXME Not great
    public List<List<SchemaObject>> result;
    private final Set<SchemaObject> recursiveNodes;

    public DfsFinder(RecursionMerge recursionMerge) {
        this.recursionMerge = recursionMerge;
        this.currentPath = new ArrayList<>();
        this.result = new ArrayList<>();
        this.recursiveNodes = new HashSet<>();
    }

    /**
     * Initiates the search for occurrences starting from the specified schema object.
     */
    public List<List<SchemaObject>> findOccurrences(SchemaObject startNode) {
        dfsFind(startNode, 0);
        // FIXME Not terrible
        return cleanOccurrences(result);
    }

    private void dfsFind(SchemaObject currentNode, int patternIndex) {
        if (currentNode == null || patternIndex >= recursionMerge.adjustedPattern.size())
            return;

        PatternSegment currentSegment = recursionMerge.adjustedPattern.get(patternIndex);
        final var currentNodeLabel = recursionMerge.newMetadata.getObject(currentNode).label;

        if (!currentNodeLabel.equals(currentSegment.nodeName())) {
            if (foundFullMatch()) {
                result.add(new ArrayList<>(currentPath));
                dfsFind(currentNode, 0); // Restart DFS from the current node
            }
            return;
        }

        currentPath.add(currentNode);
        updatePatternObjects(currentSegment, currentNode);

        if (patternIndex == recursionMerge.adjustedPattern.size() - 1)
            handleFullMatch(currentNode);
        else
            processDirection(currentNode, patternIndex, currentSegment);

        if (!foundFullMatch() && !recursiveNodes.contains(currentNode))
            currentPath.remove(currentPath.size() - 1);
    }

    private void updatePatternObjects(PatternSegment currentSegment, SchemaObject currentNode) {
        final Set<SchemaObject> objects = recursionMerge.mapPatternObjects
            .getOrDefault(currentSegment, new HashSet<>());
        objects.add(currentNode);
        recursionMerge.mapPatternObjects.put(currentSegment, objects);
    }

    private void handleFullMatch(SchemaObject currentNode) {
        final @Nullable SchemaObject nextNode = recursionMerge.findNextNode(recursionMerge.newSchema, currentNode, recursionMerge.adjustedPattern.get(0));
        if (nextNode != null)
            dfsFind(nextNode, 1); // Continue DFS
    }

    private void processDirection(SchemaObject currentNode, int patternIndex, PatternSegment currentSegment) {
        if (currentSegment.direction().equals(RecursionMerge.FORWARD) || currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD))
            processForward(currentNode, patternIndex, currentSegment);
        else if (currentSegment.direction().equals(RecursionMerge.BACKWARD) || currentSegment.direction().equals(RecursionMerge.RECURSIVE_BACKWARD))
            processBackward(currentNode, patternIndex, currentSegment);
    }

    private void processForward(SchemaObject currentNode, int patternIndex, PatternSegment currentSegment) {
        List<SchemaMorphism> morphismsToProcess = findOutgoingMorphisms(currentNode);
        final var currentNodeLabel = recursionMerge.newMetadata.getObject(currentNode).label;

        if (currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD))
            recursiveNodes.add(currentNode);

        for (final SchemaMorphism morphism : morphismsToProcess) {
            final int nextIndex = currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD) && recursionMerge.newMetadata.getObject(morphism.cod()).label.equals(currentNodeLabel)
                ? patternIndex
                : patternIndex + 1;

            dfsFind(morphism.cod(), nextIndex);
        }
    }

    private void processBackward(SchemaObject currentNode, int patternIndex, PatternSegment currentSegment) {
        List<SchemaMorphism> morphismsToProcess = findIncomingMorphisms(currentNode);
        final var currentNodeLabel = recursionMerge.newMetadata.getObject(currentNode).label;

        for (SchemaMorphism morphism : morphismsToProcess) {
            final int nextIndex = currentSegment.direction().equals(RecursionMerge.RECURSIVE_BACKWARD) && recursionMerge.newMetadata.getObject(morphism.dom()).label.equals(currentNodeLabel)
                ? patternIndex
                : patternIndex + 1;

            dfsFind(morphism.dom(), nextIndex);
        }
    }

    private boolean foundFullMatch() {
        return currentPath.size() >= recursionMerge.adjustedPattern.size() && containsPatternSequence(currentPath);
    }

    private boolean containsPatternSequence(List<SchemaObject> list) {
        if (recursionMerge.adjustedPattern.size() > list.size())
            return false;

        for (int i = 0; i <= list.size() - recursionMerge.adjustedPattern.size(); i++) {
            boolean foundSublist = true;

            for (int j = 0; j < recursionMerge.adjustedPattern.size(); j++) {
                if (!recursionMerge.newMetadata.getObject(list.get(i + j)).label.equals(recursionMerge.adjustedPattern.get(j).nodeName())) {
                    foundSublist = false;
                    break;
                }
            }
            if (foundSublist)
                return true;
        }

        return false;
    }

    private List<SchemaMorphism> findOutgoingMorphisms(SchemaObject node) {
        return recursionMerge.newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(node))
            .toList();
    }

    private List<SchemaMorphism> findIncomingMorphisms(SchemaObject node) {
        return recursionMerge.newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.cod().equals(node))
            .toList();
    }

    private static List<List<SchemaObject>> cleanOccurrences(List<List<SchemaObject>> listOfLists) {
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

            if (!isMergedOrSubsequence)
                result.add(currentList);
        }

        return result;
    }

    private static boolean isSublist(List<SchemaObject> list, List<SchemaObject> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }

    private static boolean canMerge(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = Math.min(list1.size(), list2.size());

        for (int i = 1; i <= overlapSize; i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                return true;

        return false;
    }

    private static List<SchemaObject> mergeLists(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = 0;

        for (int i = 1; i <= Math.min(list1.size(), list2.size()); i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                overlapSize = i;

        List<SchemaObject> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2.subList(overlapSize, list2.size()));

        return mergedList;
    }

}

