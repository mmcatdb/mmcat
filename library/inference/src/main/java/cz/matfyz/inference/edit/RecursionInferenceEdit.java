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
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = RecursionInferenceEdit.Deserializer.class)
public class RecursionInferenceEdit extends AbstractInferenceEdit {

    private static final Logger LOGGER = Logger.getLogger(RecursionInferenceEdit.class.getName());
    private static final String RECURSIVE_MORPH_STRING = "@";

    @JsonProperty("type")
    private final String type = "recursion";

    public final List<PatternSegment> pattern;
    public List<PatternSegment> adjustedPattern;

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

        // if pattern in the middle of a seq
        // we need to make sure to create morphs from first element of pattern to the first after occurence
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
            dfsFind(schemaCategory, node, 0, new ArrayList<>(), result, false, false);
        }
        System.out.println(result);
        return result;
    }

    // TODO: refactor this method!
/*
    private void dfsFind(SchemaCategory schemaCategory, SchemaObject currentNode, int patternIndex, List<SchemaObject> currentPath, List<List<SchemaObject>> result, boolean fullMatch) {
        if (currentNode == null || patternIndex >= adjustedPattern.size()) return;

        PatternSegment currentSegment = adjustedPattern.get(patternIndex);
        if (currentNode.label().equals(currentSegment.nodeName)) {
            currentPath.add(currentNode);

            if (patternIndex == adjustedPattern.size() - 1) {
                // Check if the middle nodes have no other morphisms connected
                if (isValidPattern(currentPath, schemaCategory)) {
                    // found full match
                    fullMatch = true;
                    patternIndex = 0;
                    currentSegment = adjustedPattern.get(patternIndex);

                    // Continue searching from the current position for partial matches
                    SchemaObject nextNode = null;
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (currentSegment.direction.equals("->") && morphism.dom().equals(currentNode)) {
                            nextNode = morphism.cod();
                            break;
                        } else if (currentSegment.direction.equals("<-") && morphism.cod().equals(currentNode)) {
                            nextNode = morphism.dom();
                            break;
                        }
                    }
                    if (nextNode != null) {
                        dfsFind(schemaCategory, nextNode, 1, new ArrayList<>(currentPath), result, fullMatch);
                    }
                }
            } else {
                if (currentSegment.direction.equals("->")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.dom().equals(currentNode)) {
                            dfsFind(schemaCategory, morphism.cod(), patternIndex + 1, currentPath, result, fullMatch);
                        }
                    }
                } if (currentSegment.direction.equals("<-")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.cod().equals(currentNode)) {
                            dfsFind(schemaCategory, morphism.dom(), patternIndex + 1, currentPath, result, fullMatch);
                        }
                    }
                } else if (fullMatch) {
                    result.add(new ArrayList<>(currentPath));
                    dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false);
                }
            }
            if (!fullMatch) {
                currentPath.remove(currentPath.size() - 1);
            }
        } else if (fullMatch) {
            result.add(new ArrayList<>(currentPath));
            dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false);
        }
    }*/

    private void dfsFind(SchemaCategory schemaCategory, SchemaObject currentNode, int patternIndex,
                     List<SchemaObject> currentPath, List<List<SchemaObject>> result, boolean fullMatch, boolean processing) {
        if (currentNode == null || patternIndex >= adjustedPattern.size()) return;

        PatternSegment currentSegment = adjustedPattern.get(patternIndex);

        if (currentNode.label().equals(currentSegment.nodeName)) {
            currentPath.add(currentNode);

            if (patternIndex == adjustedPattern.size() - 1) {
                if (isValidPattern(currentPath, schemaCategory)) {
                    fullMatch = true;
                    patternIndex = 0;
                    currentSegment = adjustedPattern.get(patternIndex);

                    SchemaObject nextNode = findNextNode(schemaCategory, currentNode, currentSegment);

                    if (nextNode != null) {
                        dfsFind(schemaCategory, nextNode, 1, new ArrayList<>(currentPath), result, fullMatch, processing);
                    }
                }
            } else {
                if (currentSegment.direction.equals("->") || currentSegment.direction.equals("@->")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.dom().equals(currentNode)) {
                            if (currentSegment.direction.equals("@->")) {
                                processing = true;
                                dfsFind(schemaCategory, morphism.cod(), patternIndex, currentPath, result, fullMatch, processing);
                                dfsFind(schemaCategory, morphism.cod(), patternIndex + 1, currentPath, result, fullMatch, processing);
                            } else {
                                dfsFind(schemaCategory, morphism.cod(), patternIndex + 1, currentPath, result, fullMatch, processing);
                            }
                        }
                    }
                }
                if (currentSegment.direction.equals("<-") || currentSegment.direction.equals("@<-")) {
                    for (SchemaMorphism morphism : schemaCategory.allMorphisms()) {
                        if (morphism.cod().equals(currentNode)) {
                            // For "@<-", explore both continuing to the next pattern element and staying on the current one
                            if (currentSegment.direction.equals("@<-")) {
                                processing = true;
                                dfsFind(schemaCategory, morphism.dom(), patternIndex, currentPath, result, fullMatch, processing);
                                dfsFind(schemaCategory, morphism.dom(), patternIndex + 1, currentPath, result, fullMatch, processing);
                            } else {
                                dfsFind(schemaCategory, morphism.dom(), patternIndex + 1, currentPath, result, fullMatch, processing);
                            }
                        }
                    }
                } else if (fullMatch) {
                    if (!processing) {
                        result.add(new ArrayList<>(currentPath));
                        dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false, false);
                    }
                }
            }

            if (!fullMatch) {
                currentPath.remove(currentPath.size() - 1);
            }
        } else if (fullMatch) {
            result.add(new ArrayList<>(currentPath));
            dfsFind(schemaCategory, currentNode, 0, new ArrayList<>(), result, false, false);
        }
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
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        // TODO: how do I adjust the mapping here?
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
