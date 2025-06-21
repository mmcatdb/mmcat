package cz.matfyz.inference.edit;

import cz.matfyz.core.schema.SchemaObjex;
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
    private List<SchemaObjex> currentPath;
    private List<List<SchemaObjex>> result;
    private final Set<SchemaObjex> recursiveNodes;
    private final Set<SchemaObjex> visitedNodes = new HashSet<>();

    public DfsFinder(RecursionMerge recursionMerge) {
        this.recursionMerge = recursionMerge;
        this.currentPath = new ArrayList<>();
        this.result = new ArrayList<>();
        this.recursiveNodes = new HashSet<>();
    }

    public List<List<SchemaObjex>> getResult() {
        return result;
    }

    /**
     * Initiates the search for occurrences starting from all objexes in the schema.
     */
    public void findOccurrencesInAllNodes() {
        for (SchemaObjex node : recursionMerge.newSchema.allObjexes()) {
            findOccurrences(node);
        }
        result = cleanOccurrences(result);
    }

    /**
     * Initiates the search for occurrences starting from the specified objexes.
     */
    public void findOccurrences(SchemaObjex startNode) {
        dfsFind(startNode, 0);
    }

    private void dfsFind(SchemaObjex currentNode, int patternIndex) {
        if (currentNode == null || patternIndex >= recursionMerge.adjustedPattern.size() || visitedNodes.contains(currentNode))
            return;
        visitedNodes.add(currentNode);

        PatternSegment currentSegment = recursionMerge.adjustedPattern.get(patternIndex);
        final var currentNodeLabel = recursionMerge.newMetadata.getObjex(currentNode).label;

        if (!currentNodeLabel.equals(currentSegment.nodeName())) {
            if (foundFullMatch()) {
                result.add(new ArrayList<>(currentPath));
                dfsFind(currentNode, 0); // Restart DFS from the current node
            }
            return;
        }

        currentPath.add(currentNode);
        updatePatternObjexes(currentSegment, currentNode);

        if (patternIndex == recursionMerge.adjustedPattern.size() - 1) {
            handleFullMatch(currentNode);
            result.add(new ArrayList<>(currentPath));
        }
        else
            processDirection(currentNode, patternIndex, currentSegment);

        if (!foundFullMatch() && !recursiveNodes.contains(currentNode))
            currentPath.remove(currentPath.size() - 1);
    }

    private void updatePatternObjexes(PatternSegment currentSegment, SchemaObjex currentNode) {
        final Set<SchemaObjex> objexes = recursionMerge.mapPatternObjexes
            .getOrDefault(currentSegment, new HashSet<>());
        objexes.add(currentNode);
        recursionMerge.mapPatternObjexes.put(currentSegment, objexes);
    }

    private void handleFullMatch(SchemaObjex currentNode) {
        final @Nullable SchemaObjex nextNode = recursionMerge.findNextNode(recursionMerge.newSchema, currentNode, recursionMerge.adjustedPattern.get(0));
        if (nextNode != null)
            dfsFind(nextNode, 1); // Continue DFS
    }

    private void processDirection(SchemaObjex currentNode, int patternIndex, PatternSegment currentSegment) {
        if (currentSegment.direction().equals(RecursionMerge.FORWARD) || currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD))
            processForward(currentNode, patternIndex, currentSegment);
        else if (currentSegment.direction().equals(RecursionMerge.BACKWARD) || currentSegment.direction().equals(RecursionMerge.RECURSIVE_BACKWARD))
            processBackward(currentNode, patternIndex, currentSegment);
    }

    private void processForward(SchemaObjex currentNode, int patternIndex, PatternSegment currentSegment) {
        List<SchemaMorphism> morphismsToProcess = findOutgoingMorphisms(currentNode);
        final var currentNodeLabel = recursionMerge.newMetadata.getObjex(currentNode).label;

        if (currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD))
            recursiveNodes.add(currentNode);

        for (final SchemaMorphism morphism : morphismsToProcess) {
            final int nextIndex = currentSegment.direction().equals(RecursionMerge.RECURSIVE_FORWARD) && recursionMerge.newMetadata.getObjex(morphism.cod()).label.equals(currentNodeLabel)
                ? patternIndex
                : patternIndex + 1;

            dfsFind(morphism.cod(), nextIndex);
        }
    }

    private void processBackward(SchemaObjex currentNode, int patternIndex, PatternSegment currentSegment) {
        List<SchemaMorphism> morphismsToProcess = findIncomingMorphisms(currentNode);
        final var currentNodeLabel = recursionMerge.newMetadata.getObjex(currentNode).label;

        for (SchemaMorphism morphism : morphismsToProcess) {
            final int nextIndex = currentSegment.direction().equals(RecursionMerge.RECURSIVE_BACKWARD) && recursionMerge.newMetadata.getObjex(morphism.dom()).label.equals(currentNodeLabel)
                ? patternIndex
                : patternIndex + 1;

            dfsFind(morphism.dom(), nextIndex);
        }
    }

    private boolean foundFullMatch() {
        return currentPath.size() >= recursionMerge.adjustedPattern.size() && containsPatternSequence(currentPath);
    }

    private boolean containsPatternSequence(List<SchemaObjex> list) {
        if (recursionMerge.adjustedPattern.size() > list.size())
            return false;

        for (int i = 0; i <= list.size() - recursionMerge.adjustedPattern.size(); i++) {
            boolean foundSublist = true;

            for (int j = 0; j < recursionMerge.adjustedPattern.size(); j++) {
                if (!recursionMerge.newMetadata.getObjex(list.get(i + j)).label.equals(recursionMerge.adjustedPattern.get(j).nodeName())) {
                    foundSublist = false;
                    break;
                }
            }
            if (foundSublist)
                return true;
        }

        return false;
    }

    private List<SchemaMorphism> findOutgoingMorphisms(SchemaObjex node) {
        return recursionMerge.newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(node))
            .toList();
    }

    private List<SchemaMorphism> findIncomingMorphisms(SchemaObjex node) {
        return recursionMerge.newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.cod().equals(node))
            .toList();
    }

    private static List<List<SchemaObjex>> cleanOccurrences(List<List<SchemaObjex>> listOfLists) {
        final List<List<SchemaObjex>> result = new ArrayList<>();

        for (int i = 0; i < listOfLists.size(); i++) {
            final List<SchemaObjex> currentList = listOfLists.get(i);
            boolean isMergedOrSubsequence = false;

            for (int j = 0; j < result.size(); j++) {
                final List<SchemaObjex> existingList = result.get(j);

                if (isSublist(existingList, currentList)) {
                    isMergedOrSubsequence = true;
                    break;
                } else if (isSublist(currentList, existingList)) {
                    result.set(j, currentList);
                    isMergedOrSubsequence = true;
                    break;
                } else if (canMerge(existingList, currentList)) {
                    List<SchemaObjex> mergedList = mergeLists(existingList, currentList);
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

    private static boolean isSublist(List<SchemaObjex> list, List<SchemaObjex> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }

    private static boolean canMerge(List<SchemaObjex> list1, List<SchemaObjex> list2) {
        int overlapSize = Math.min(list1.size(), list2.size());

        for (int i = 1; i <= overlapSize; i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                return true;

        return false;
    }

    private static List<SchemaObjex> mergeLists(List<SchemaObjex> list1, List<SchemaObjex> list2) {
        int overlapSize = 0;

        for (int i = 1; i <= Math.min(list1.size(), list2.size()); i++)
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i)))
                overlapSize = i;

        List<SchemaObjex> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2.subList(overlapSize, list2.size()));

        return mergedList;
    }

}

