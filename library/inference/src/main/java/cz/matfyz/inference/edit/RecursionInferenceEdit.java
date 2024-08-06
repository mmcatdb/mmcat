package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;
import cz.matfyz.inference.edit.utils.PatternSegment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

// TODO: this class is a one big WIP
@JsonDeserialize(using = RecursionInferenceEdit.Deserializer.class)
public class RecursionInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(RecursionInferenceEdit.class.getName());
    private static final String RECURSIVE_MORPH_STRING = "@";

    @JsonProperty("type")
    private final String type = "recursion";

    public final List<PatternSegment> pattern;
    public static List<PatternSegment> adjustedPattern;
    public Map<PatternSegment, Set<SchemaObject>> mapPatternObjects = new HashMap<>();

    public RecursionInferenceEdit(List<PatternSegment> pattern) {
        this.pattern = pattern;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        /*
         * Assumptions: Morphisms in the pattern are only among the elemenets of the pattern
         * Pattern always starts and ends in the same object (w/ however the occurence does not have to end in this object)
         * For now we assume that the pattern needs to occur at least one in its full length to count it as a full match
         */
        LOGGER.info("Applying Recursion Edit on Schema Category...");

        setSchemaCategories(schemaCategory);

        adjustPattern();

        List<List<SchemaObject>> occurences = findAdjustedPatternOccurences(newSchemaCategory);

        bridgeOccurences(newSchemaCategory, occurences);

        findMorphismsAndObjectsToDelete(newSchemaCategory, occurences);
        InferenceEditorUtils.removeMorphismsAndObjects(newSchemaCategory, signaturesToDelete, keysToDelete);

        createRecursiveMorphisms(newSchemaCategory, occurences);

        return newSchemaCategory;
    }

    private void adjustPattern() {
        List<PatternSegment> newPattern = new ArrayList<>();
        int i = 0;
        boolean lastAdjusted = false;
        while (i < pattern.size() - 1) {
            PatternSegment currentSegment = pattern.get(i);
            PatternSegment nextSegment = pattern.get(i + 1);

            if (currentSegment.nodeName.equals(nextSegment.nodeName)) {
                newPattern.add(new PatternSegment(currentSegment.nodeName, RECURSIVE_MORPH_STRING + nextSegment.direction));
                i = i + 2;
                lastAdjusted = true;
            } else {
                newPattern.add(currentSegment);
                i++;
                lastAdjusted = false;
            }
        }
        if (!lastAdjusted) {
            newPattern.add(pattern.get(pattern.size() - 1));
        }

        this.adjustedPattern = newPattern;
        System.out.println("adjusted pattern" + adjustedPattern);
    }

    private List<List<SchemaObject>> findAdjustedPatternOccurences(SchemaCategory schemaCategory) {
        List<List<SchemaObject>> result = new ArrayList<>();
        for (SchemaObject node : schemaCategory.allObjects()) {
            dfsFind(schemaCategory, node, 0, new ArrayList<>(), result, false, false, new HashSet<>());
        }
        System.out.println(result);
        result = cleanOccurences(result);
        System.out.println("clean result: " + result);
        return result;
    }

    private void dfsFind(SchemaCategory schemaCategory, SchemaObject currentNode, int patternIndex,
                     List<SchemaObject> currentPath, List<List<SchemaObject>> result, boolean fullMatch, boolean processing,
                     Set<SchemaObject> recursiveNodes) {
        if (currentNode == null || patternIndex >= adjustedPattern.size()) return;

        PatternSegment currentSegment = adjustedPattern.get(patternIndex);

        if (currentNode.label().equals(currentSegment.nodeName)) {
            currentPath.add(currentNode);
            if (mapPatternObjects.isEmpty() || mapPatternObjects.get(currentSegment) == null) {
                Set<SchemaObject> objects = new HashSet<>();
                objects.add(currentNode);
                mapPatternObjects.put(currentSegment, objects);
            } else {
                Set<SchemaObject> objects = mapPatternObjects.get(currentSegment);
                objects.add(currentNode);
                mapPatternObjects.put(currentSegment, objects);
            }

            if (patternIndex == adjustedPattern.size() - 1) {
               // if (isValidPattern(currentPath, schemaCategory)) {
                    fullMatch = true;
                    patternIndex = 0;
                    currentSegment = adjustedPattern.get(patternIndex);

                    SchemaObject nextNode = findNextNode(schemaCategory, currentNode, currentSegment);

                    if (nextNode != null) {
                        dfsFind(schemaCategory, nextNode, 1, currentPath, result, fullMatch, processing, recursiveNodes);
                    }
                //}
            } else {
                if (currentSegment.direction.equals("->") || currentSegment.direction.equals("@->")) {
                    List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.dom().equals(currentNode)) {
                            morphismsToProcess.add(morphism);
                        }
                    }
                    if (currentSegment.direction.equals("@->")) {
                        if (!containsRecursiveAlready(recursiveNodes, currentNode)) {
                            recursiveNodes.add(currentNode);
                        }
                        processing = true;
                    }
                    for (SchemaMorphism m : morphismsToProcess) {
                        if (currentSegment.direction.equals("@->") && m.cod().label().equals(currentNode.label())) {
                            dfsFind(schemaCategory, m.cod(), patternIndex, currentPath, result, fullMatch, processing, recursiveNodes);
                            if (recursiveNodes.contains(currentNode)) {
                                processing = false;
                            }
                        } else {
                            dfsFind(schemaCategory, m.cod(), patternIndex + 1, currentPath, result, fullMatch, processing, recursiveNodes);
                            if (recursiveNodes.contains(currentNode)) {
                                processing = false;
                            } else {
                                break;
                            }
                        }
                    }
                }
                if (currentSegment.direction.equals("<-") || currentSegment.direction.equals("@<-")) {
                    List<SchemaMorphism> morphismsToProcess = new ArrayList<>();
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.cod().equals(currentNode)) {
                            morphismsToProcess.add(morphism);
                        }
                    }
                    for (SchemaMorphism m : morphismsToProcess) {
                        if (currentSegment.direction.equals("@<-") && m.dom().label().equals(currentNode.label())) {
                            dfsFind(schemaCategory, m.dom(), patternIndex, currentPath, result, fullMatch, processing, recursiveNodes);
                            processing = false;
                        } else {
                            dfsFind(schemaCategory, m.dom(), patternIndex + 1, currentPath, result, fullMatch, processing, recursiveNodes);
                            processing = false;
                        }
                    }

                } else if (foundFullMatch(currentPath)) {
                    if (!processing && !occurenceAlreadyFound(result, currentPath)) {
                        result.add(new ArrayList<>(currentPath));
                        dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false, false, recursiveNodes);
                    }
                }
            }
            if (!foundFullMatch(currentPath) && !processing) {
                currentPath.remove(currentPath.size() - 1);
            }
        } else if (foundFullMatch(currentPath)) {
            result.add(new ArrayList<>(currentPath));
            dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false, false, recursiveNodes);
        }
    }

    public boolean containsRecursiveAlready(Set<SchemaObject> recursiveNodes, SchemaObject currentNode) {
        for (SchemaObject schemaObject : recursiveNodes) {
            if (schemaObject.label().equals(currentNode.label())) {
                return true;
            }
        }
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
        if (currentPath.size() < adjustedPattern.size()) {
            return false;
        }
        if (isSublist2(currentPath)) {
            return true;
        }

        return false;
    }

    private static boolean isSublist2(List<SchemaObject> list) {
        if (adjustedPattern.size() > list.size()) {
            return false;
        }

        for (int i = 0; i <= list.size() - adjustedPattern.size(); i++) {
            boolean foundSublist = true;

            for (int j = 0; j < adjustedPattern.size(); j++) {
                if (!list.get(i + j).label().equals(adjustedPattern.get(j).nodeName)) {
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

    private SchemaObject findNextNode(SchemaCategory schemaCategory, SchemaObject currentNode, PatternSegment currentSegment) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (currentSegment.direction.equals("->") && morphism.dom().equals(currentNode)) {
                return morphism.cod();
            } else if (currentSegment.direction.equals("<-") && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            } else if (currentSegment.direction.equals("@->") && morphism.dom().equals(currentNode)) {
                return morphism.cod();
            } else if (currentSegment.direction.equals("@<-") && morphism.cod().equals(currentNode)) {
                return morphism.dom();
            }
        }
        return null;
    }

    private boolean isValidPattern(List<SchemaObject> path, SchemaCategory schemaCategory) {
        for (int i = 1; i < path.size() - 1; i++) {
            SchemaObject middleNode = path.get(i);
            if (hasOtherMorphisms(middleNode, path.get(i - 1), path.get(i + 1), schemaCategory)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOtherMorphisms(SchemaObject node, SchemaObject previous, SchemaObject next, SchemaCategory schemaCategory) {
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if ((morphism.dom().equals(node) && !morphism.cod().equals(next)) ||
                (morphism.cod().equals(node) && !morphism.dom().equals(previous))) {
                return true;
            }
        }
        return false;
    }

    private void bridgeOccurences(SchemaCategory schemaCategory, List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastOccurence = occurence.get(occurence.size() - 2);
            SchemaObject lastOccurence = occurence.get(occurence.size() - 1);

            List<SchemaMorphism> otherMorphisms = findOtherMorphisms(lastOccurence, oneBeforeLastOccurence, schemaCategory);
            createNewOtherMorphisms(schemaCategory, lastOccurence, firstInPattern, otherMorphisms);
        }
    }

    private List<SchemaMorphism> findOtherMorphisms(SchemaObject node, SchemaObject previous, SchemaCategory schemaCategory) {
        List<SchemaMorphism> morphisms = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if ((morphism.cod().equals(node) && !morphism.dom().equals(previous)) ||
                (morphism.dom().equals(node) && !morphism.cod().equals(previous))) {
                morphisms.add(morphism);
            }
        }
        return morphisms;
    }

    private void createNewOtherMorphisms(SchemaCategory schemaCategory, SchemaObject lastOccurence, SchemaObject firstInPattern, List<SchemaMorphism> otherMorphisms) {
        if (otherMorphisms != null) {
            for (SchemaMorphism morphism : otherMorphisms) {
                SchemaObject dom = firstInPattern;
                SchemaObject cod = morphism.cod();
                if (!morphism.dom().equals(lastOccurence)) {
                    dom = morphism.dom();
                    cod = firstInPattern;
                }
                InferenceEditorUtils.createAndAddMorphism(schemaCategory, dom, cod);
            }
        }
    }

    private void findMorphismsAndObjectsToDelete(SchemaCategory schemaCategory, List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            for (int i = adjustedPattern.size() - 1; i < occurence.size(); i++) {
                SchemaObject schemaObject = occurence.get(i);
                keysToDelete.add(schemaObject.key());
                signaturesToDelete.addAll(findSignaturesForObject(schemaCategory, schemaObject));
            }
        }
    }

    private List<Signature> findSignaturesForObject(SchemaCategory schemaCategory, SchemaObject schemaObject) {
        List<Signature> signatures = new ArrayList<>();
        for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
            if (morphism.dom().equals(schemaObject) || morphism.cod().equals(schemaObject)) {
                signatures.add(morphism.signature());
            }
        }
        return signatures;
    }

    private void createRecursiveMorphisms(SchemaCategory schemaCategory, List<List<SchemaObject>> occurences) {
        for (List<SchemaObject> occurence : occurences) {
            SchemaObject firstInPattern = occurence.get(0);
            SchemaObject oneBeforeLastInPattern = occurence.get(adjustedPattern.size() - 2);
            InferenceEditorUtils.createAndAddMorphism(schemaCategory, oneBeforeLastInPattern, firstInPattern);
        }

        createRepetitiveMorphisms(schemaCategory, occurences);
    }

    private void createRepetitiveMorphisms(SchemaCategory schemaCategory, List<List<SchemaObject>> occurences) {
        System.out.println("keysToDelete" + keysToDelete);
        System.out.println("map: " + mapPatternObjects);
        for (PatternSegment segment : adjustedPattern) {
            if (isRepetitive(segment)) {
                for (SchemaObject object : mapPatternObjects.get(segment)) {
                    if (!keysToDelete.contains(object.key()) && inAnyOccurence(occurences, object)) {
                        InferenceEditorUtils.createAndAddMorphism(schemaCategory, object, object);
                    }
                }
            }
        }
    }

    private boolean isRepetitive(PatternSegment segment) {
        if (segment.direction.contains("@")) {
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

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        // TODO: adjust the mapping
        return mappings;
    }

    public static class Deserializer extends StdDeserializer<RecursionInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public RecursionInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final List<PatternSegment> pattern = new ArrayList<>();
            final JsonNode patternNode = node.get("pattern");

            if (pattern != null) {
                for (JsonNode patternSegmentNode : patternNode) {
                    PatternSegment patternSegment = parser.getCodec().treeToValue(patternSegmentNode, PatternSegment.class);
                    pattern.add(patternSegment);
                }
            }
            return new RecursionInferenceEdit(pattern);
        }
    }
}
