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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

// TODO: this class is a one big WIP
public class RecursionMerge extends InferenceEditAlgorithm {

    public record Data(
        List<PatternSegment> pattern
    ) implements InferenceEdit {

        @Override public RecursionMerge createAlgorithm() {
            return new RecursionMerge(this);
        }

    }

    private final Data data;

    private static final Logger LOGGER = Logger.getLogger(RecursionMerge.class.getName());
    private static final String RECURSIVE_MORPH_STRING = "@";

    private List<PatternSegment> adjustedPattern;
    private Map<PatternSegment, Set<SchemaObject>> mapPatternObjects = new HashMap<>();

    public RecursionMerge(Data data) {
        this.data = data;
    }

    /*
     * Assumptions: Morphisms in the pattern are only among the elemenets of the pattern
     * Pattern always starts and ends in the same object (w/ however the occurence does not have to end in this object)
     * For now we assume that the pattern needs to occur at least one in its full length to count it as a full match
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

    private void adjustPattern() {
        List<PatternSegment> newPattern = new ArrayList<>();
        int i = 0;
        boolean lastAdjusted = false;
        while (i < data.pattern.size() - 1) {
            PatternSegment currentSegment = data.pattern.get(i);
            PatternSegment nextSegment = data.pattern.get(i + 1);

            if (currentSegment.nodeName().equals(nextSegment.nodeName())) {
                newPattern.add(new PatternSegment(currentSegment.nodeName(), RECURSIVE_MORPH_STRING + nextSegment.direction()));
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
        System.out.println("adjusted pattern" + adjustedPattern);
    }

    private List<List<SchemaObject>> findAdjustedPatternOccurences() {
        List<List<SchemaObject>> result = new ArrayList<>();
        for (SchemaObject node : newSchema.allObjects())
            dfsFind(node, 0, new ArrayList<>(), result, false, false, new HashSet<>());

        System.out.println(result);
        result = cleanOccurences(result);
        System.out.println("clean result: " + result);
        return result;
    }

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
            // if (isValidPattern(currentPath, schema)) {
                fullMatch = true;
                patternIndex = 0;
                currentSegment = adjustedPattern.get(patternIndex);

                SchemaObject nextNode = findNextNode(newSchema, currentNode, currentSegment);

                if (nextNode != null)
                    dfsFind(nextNode, 1, currentPath, result, fullMatch, processing, recursiveNodes);
            //}
        } else {
            if (currentSegment.direction().equals("->") || currentSegment.direction().equals("@->")) {
                List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                for (SchemaMorphism morphism : newSchema.allMorphisms())
                    if (morphism.dom().equals(currentNode))
                        morphismsToProcess.add(morphism);

                if (currentSegment.direction().equals("@->")) {
                    if (!containsRecursiveAlready(recursiveNodes, currentNodeLabel))
                        recursiveNodes.add(currentNode);

                    processing = true;
                }

                for (SchemaMorphism m : morphismsToProcess) {
                    if (currentSegment.direction().equals("@->") && newMetadata.getObject(m.cod()).label.equals(currentNodeLabel)) {
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
            if (currentSegment.direction().equals("<-") || currentSegment.direction().equals("@<-")) {
                List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                for (SchemaMorphism morphism : newSchema.allMorphisms())
                    if (morphism.cod().equals(currentNode))
                        morphismsToProcess.add(morphism);

                for (SchemaMorphism m : morphismsToProcess) {
                    final var nextIndex = currentSegment.direction().equals("@<-") && newMetadata.getObject(m.dom()).label.equals(currentNodeLabel)
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

    public boolean containsRecursiveAlready(Set<SchemaObject> recursiveNodes, String currentNodeLabel) {
        for (final SchemaObject object : recursiveNodes)
            if (newMetadata.getObject(object).label.equals(currentNodeLabel))
                return true;

        return false;
    }

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

    private static boolean isSublist(List<SchemaObject> list, List<SchemaObject> sublist) {
        if (sublist.size() > list.size()) {
            return false;
        }

        for (int i = 0; i <= list.size() - sublist.size(); i++) {
            boolean foundSublist = true;

            for (int j = 0; j < sublist.size(); j++) {
                if (!list.get(i + j).equals(sublist.get(j))) {
                    foundSublist = false;
                    break;
                }
            }

            if (foundSublist) {
                return true;
            }
        }

        return false;
    }

    private static boolean canMerge(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = Math.min(list1.size(), list2.size());

        for (int i = 1; i <= overlapSize; i++) {
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i))) {
                return true;
            }
        }

        return false;
    }

    private static List<SchemaObject> mergeLists(List<SchemaObject> list1, List<SchemaObject> list2) {
        int overlapSize = 0;

        for (int i = 1; i <= Math.min(list1.size(), list2.size()); i++) {
            if (list1.subList(list1.size() - i, list1.size()).equals(list2.subList(0, i))) {
                overlapSize = i;
            }
        }

        List<SchemaObject> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2.subList(overlapSize, list2.size()));

        return mergedList;
    }

    private boolean foundFullMatch(List<SchemaObject> currentPath) {
        if (currentPath.size() < adjustedPattern.size())
            return false;

        return isSublist2(currentPath);
    }

    private boolean isSublist2(List<SchemaObject> list) {
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

    private boolean occurenceAlreadyFound(List<List<SchemaObject>> result, List<SchemaObject> currentPath) {
        if (result.size() == 0) {
            return false;
        }
        for (List<SchemaObject> r : result) {
            boolean diff = false;
            if (r.size() != currentPath.size()) {
                diff = true;
            } else {
                for (int i = 0; i < r.size(); i++) {
                    if (!r.get(i).equals(currentPath.get(i))) {
                        diff = true;
                    }
                }
            }
            if (!diff) {
                return true;
            }
        }
        return false;
    }

    private SchemaObject findNextNode(SchemaCategory schema, SchemaObject currentNode, PatternSegment currentSegment) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (currentSegment.direction().equals("->") && morphism.dom().equals(currentNode)) {
                return morphism.cod();
            } else if (currentSegment.direction().equals("<-") && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            } else if (currentSegment.direction().equals("@->") && morphism.dom().equals(currentNode)) {
                return morphism.cod();
            } else if (currentSegment.direction().equals("@<-") && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            }
        }
        return null;
    }

    private boolean isValidPattern(List<SchemaObject> path, SchemaCategory schema) {
        for (int i = 1; i < path.size() - 1; i++) {
            SchemaObject middleNode = path.get(i);
            if (hasOtherMorphisms(middleNode, path.get(i - 1), path.get(i + 1), schema)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOtherMorphisms(SchemaObject node, SchemaObject previous, SchemaObject next, SchemaCategory schema) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if ((morphism.dom().equals(node) && !morphism.cod().equals(next)) ||
                (morphism.cod().equals(node) && !morphism.dom().equals(previous))) {
                return true;
            }
        }
        return false;
    }

    private void bridgeOccurences(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastOccurence = occurence.get(occurence.size() - 2);
            SchemaObject lastOccurence = occurence.get(occurence.size() - 1);

            List<SchemaMorphism> otherMorphisms = findOtherMorphisms(lastOccurence, oneBeforeLastOccurence);
            createNewOtherMorphisms(lastOccurence, firstInPattern, otherMorphisms);
        }
    }

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

    private void findMorphismsAndObjectsToDelete(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            for (int i = adjustedPattern.size() - 1; i < occurence.size(); i++) {
                SchemaObject schemaObject = occurence.get(i);
                keysToDelete.add(schemaObject.key());
                signaturesToDelete.addAll(findSignaturesForObject(schemaObject));
            }
        }
    }

    private List<Signature> findSignaturesForObject(SchemaObject schemaObject) {
        List<Signature> signatures = new ArrayList<>();
        for (SchemaMorphism morphism : newSchema.allMorphisms()) {
            if (morphism.dom().equals(schemaObject) || morphism.cod().equals(schemaObject)) {
                signatures.add(morphism.signature());
            }
        }
        return signatures;
    }

    private void createRecursiveMorphisms(List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastInPattern = occurence.get(adjustedPattern.size() - 2);
            InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, oneBeforeLastInPattern, firstInPattern);
        }

        createRepetitiveMorphisms(occurences);
    }

    private void createRepetitiveMorphisms(List<List<SchemaObject>> occurences) {
        System.out.println("keysToDelete" + keysToDelete);
        System.out.println("map: " + mapPatternObjects);
        for (PatternSegment segment : adjustedPattern) {
            if (isRepetitive(segment)) {
                for (SchemaObject object : mapPatternObjects.get(segment)) {
                    if (!keysToDelete.contains(object.key()) && inAnyOccurence(occurences, object)) {
                        InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, object, object);
                    }
                }
            }
        }
    }

    private boolean isRepetitive(PatternSegment segment) {
        if (segment.direction().contains("@")) {
            return true;
        }
        return false;
    }

    private boolean inAnyOccurence(List<List<SchemaObject>> occurences, SchemaObject schemaObjectToFind) {
        for (List<SchemaObject> occurence : occurences) {
            for (SchemaObject schemaObject : occurence) {
                if (schemaObject.equals(schemaObjectToFind)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        // TODO: adjust the mapping
        return mappings;
    }

}
