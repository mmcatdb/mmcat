package cz.matfyz.inference.edit.algorithms;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.inference.edit.DfsFinder;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.InferenceEditorUtils;
import cz.matfyz.inference.edit.PatternSegment;
import cz.matfyz.inference.edit.InferenceEditorUtils.KeysAndSignatures;

import java.util.*;
import java.util.logging.Logger;

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
    public Map<PatternSegment, Set<SchemaObjex>> mapPatternObjexes = new HashMap<>();

    public RecursionMerge(Data data) {
        this.data = data;
    }

    /*
     * Applies the primary key merging algorithm to the schema category.
     * Assumptions: Morphisms in the pattern are only among the elements of the pattern.
     * Pattern always starts and ends in the same objex (however the occurrence does not have to end in this objex).
     * For now, we assume that the pattern needs to occur at least once in its full length to count as a full match.
     */
    @Override protected void innerCategoryEdit() {
        LOGGER.info("Applying Recursion Edit on Schema Category...");

        adjustPattern();

        List<List<SchemaObjex>> occurences = findAdjustedPatternOccurences();

        bridgeOccurences(occurences);

        final var toDelete = findToDelete(occurences);
        InferenceEditorUtils.removeMorphismsAndObjexes(newSchema, toDelete);

        createRecursiveMorphisms(toDelete, occurences);
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
            }
            else {
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

    private List<List<SchemaObjex>> findAdjustedPatternOccurences() {
        final DfsFinder dfsFinder = new DfsFinder(this);
        dfsFinder.findOccurrencesInAllNodes();
        return dfsFinder.getResult();
    }

    public boolean containsRecursiveAlready(Set<SchemaObjex> recursiveNodes, String currentNodeLabel) {
        for (final SchemaObjex objex : recursiveNodes)
            if (newMetadata.getObjex(objex).label.equals(currentNodeLabel))
                return true;

        return false;
    }

    public @Nullable SchemaObjex findNextNode(SchemaCategory schema, SchemaObjex currentNode, PatternSegment currentSegment) {
        for (final SchemaMorphism morphism : schema.allMorphisms()) {
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

    private void bridgeOccurences(List<List<SchemaObjex>> occurences) {
        for (List<SchemaObjex> occurence : occurences) {
            SchemaObjex firstInPattern = occurence.get(0);
            SchemaObjex oneBeforeLastOccurence = occurence.get(occurence.size() - 2);
            SchemaObjex lastOccurence = occurence.get(occurence.size() - 1);

            List<SchemaMorphism> otherMorphisms = findOtherMorphisms(lastOccurence, oneBeforeLastOccurence);
            createNewOtherMorphisms(lastOccurence, firstInPattern, otherMorphisms);
        }
    }

    private List<SchemaMorphism> findOtherMorphisms(SchemaObjex node, SchemaObjex previous) {
        List<SchemaMorphism> morphisms = new ArrayList<>();
        for (SchemaMorphism morphism : newSchema.allMorphisms()) {
            if ((morphism.cod().equals(node) && !morphism.dom().equals(previous)) ||
                (morphism.dom().equals(node) && !morphism.cod().equals(previous))) {
                morphisms.add(morphism);
            }
        }
        return morphisms;
    }

    private void createNewOtherMorphisms(SchemaObjex lastOccurence, SchemaObjex firstInPattern, List<SchemaMorphism> otherMorphisms) {
        if (otherMorphisms == null)
            return;

        for (SchemaMorphism morphism : otherMorphisms) {
            SchemaObjex dom = firstInPattern;
            SchemaObjex cod = morphism.cod();
            if (!morphism.dom().equals(lastOccurence)) {
                dom = morphism.dom();
                cod = firstInPattern;
            }
            InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, dom, cod);
        }
    }

    private KeysAndSignatures findToDelete(List<List<SchemaObjex>> occurences) {
        final var output = new KeysAndSignatures();

        for (List<SchemaObjex> occurence : occurences) {
            for (int i = adjustedPattern.size() - 1; i < occurence.size(); i++) {
                SchemaObjex objex = occurence.get(i);
                output.add(objex.key());
                output.signatures().addAll(findSignaturesForObjex(objex));
            }
        }

        return output;
    }

    private List<Signature> findSignaturesForObjex(SchemaObjex objex) {
        return newSchema.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(objex) || morphism.cod().equals(objex))
            .map(SchemaMorphism::signature)
            .toList();
    }

    private void createRecursiveMorphisms(KeysAndSignatures deleted, List<List<SchemaObjex>> occurences) {
        for (List<SchemaObjex> occurence : occurences) {
            SchemaObjex firstInPattern = occurence.get(0);
            SchemaObjex oneBeforeLastInPattern = occurence.get(adjustedPattern.size() - 2);
            InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, oneBeforeLastInPattern, firstInPattern);
        }

        createRepetitiveMorphisms(deleted, occurences);
    }

    private void createRepetitiveMorphisms(KeysAndSignatures deleted, List<List<SchemaObjex>> occurences) {
        for (PatternSegment segment : adjustedPattern) {
            if (!isRepetitive(segment))
                continue;

            for (SchemaObjex objex : mapPatternObjexes.get(segment))
                if (!deleted.keys().contains(objex.key()) && inAnyOccurence(occurences, objex))
                    InferenceEditorUtils.addMorphismWithMetadata(newSchema, newMetadata, objex, objex);
        }
    }

    private boolean isRepetitive(PatternSegment segment) {
        return segment.direction().contains(RECURSIVE_MARKER);
    }

    private boolean inAnyOccurence(List<List<SchemaObjex>> occurences, SchemaObjex objexToFind) {
        for (List<SchemaObjex> occurence : occurences)
            for (SchemaObjex objex : occurence)
                if (objex.equals(objexToFind))
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
