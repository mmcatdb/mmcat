package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.DfsFinder;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;
import cz.matfyz.inference.edit.PatternSegment;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code RecursionMerge} class implements an algorithm for merging recursive patterns
 * within a schema. It extends the {@link InferenceEditAlgorithm} and provides functionality
 * to detect and merge recursive structures.
 */
public class RecursionMerge extends InferenceEditAlgorithm {

    public static class Data extends InferenceEdit {

        @JsonProperty("pattern")
        @Nullable public List<PatternSegment> pattern;

        @JsonCreator
        public Data(
                @JsonProperty("id") Integer id,
                @JsonProperty("isActive") boolean isActive,
                @JsonProperty("pattern") List<PatternSegment> pattern) {
            setId(id);
            setActive(isActive);
            this.pattern = pattern;
        }

        public Data() {
            setId(null);
            setActive(false);
            this.pattern = null;
        }

        /**
         * Creates an instance of the {@code RecursionMerge} algorithm.
         */
        @Override public RecursionMerge createAlgorithm() {
            return new RecursionMerge(this);
        }
    }

    private final Data data;

    public static final Logger LOGGER = Logger.getLogger(RecursionMerge.class.getName());
    public static final String FORWARD = "->";
    public static final String BACKWARD = "<-";
    public static final String RECURSIVE_FORWARD = "@->";
    public static final String RECURSIVE_BACKWARD = "@<-";
    public static final String RECURSIVE_MARKER = "@";

    public List<PatternSegment> adjustedPattern;
    public Map<PatternSegment, Set<SchemaObject>> mapPatternObjects = new HashMap<>();

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

    private List<List<SchemaObject>> findAdjustedPatternOccurences() {
        DfsFinder dfsFinder = new DfsFinder(this);
        for (SchemaObject node : newSchema.allObjects()) {
            dfsFinder.findOccurrences(node);
        }
        return dfsFinder.result;
    }

    public boolean containsRecursiveAlready(Set<SchemaObject> recursiveNodes, String currentNodeLabel) {
        for (final SchemaObject object : recursiveNodes)
            if (newMetadata.getObject(object).label.equals(currentNodeLabel))
                return true;

        return false;
    }

    public SchemaObject findNextNode(SchemaCategory schema, SchemaObject currentNode, PatternSegment currentSegment) {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            if (currentSegment.direction().equals(FORWARD) && morphism.dom().equals(currentNode))
                return morphism.cod();
            if (currentSegment.direction().equals(BACKWARD) && morphism.cod().equals(currentNode))
                return morphism.dom();
            if (currentSegment.direction().equals(RECURSIVE_FORWARD) && morphism.dom().equals(currentNode))
                return morphism.cod();
            if (currentSegment.direction().equals(RECURSIVE_BACKWARD) && morphism.cod().equals(currentNode))
                return morphism.dom();
        }
        return null;
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
        return newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(schemaObject) || morphism.cod().equals(schemaObject))
            .map(SchemaMorphism::signature)
            .collect(Collectors.toList());
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
        for (PatternSegment segment : adjustedPattern) {
            if (!isRepetitive(segment))
                continue;

            for (SchemaObject object : mapPatternObjects.get(segment))
                if (!keysToDelete.contains(object.key()) && inAnyOccurence(occurences, object))
                    InferenceEditorUtils.createAndAddMorphism(newSchema, newMetadata, object, object);
        }
    }

    private boolean isRepetitive(PatternSegment segment) {
        return segment.direction().contains(RECURSIVE_MARKER);
    }

    private boolean inAnyOccurence(List<List<SchemaObject>> occurences, SchemaObject schemaObjectToFind) {
        for (List<SchemaObject> occurence : occurences)
            for (SchemaObject schemaObject : occurence)
                if (schemaObject.equals(schemaObjectToFind))
                    return true;

        return false;
    }

    /**
     * Applies the mapping edit to a list of mappings.
     */
    @Override public List<Mapping> applyMappingEdit(List<Mapping> mappings) {
        // TODO: adjust the mapping
        return mappings;
    }

}
