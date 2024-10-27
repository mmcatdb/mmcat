package cz.matfyz.inference;

import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;

/**
 * The {@code MMInferOneInAll} class is responsible for running the inference process.
 * It prepares the required wrappers, executes candidate mining, and converts the inferred schema
 * descriptions into schema categories and mappings.
 */
public class MMInferOneInAll {

    private AbstractInferenceWrapper wrapper;

    /**
     * Sets the input wrapper for the inference process.
     *
     * @param wrapper The {@link AbstractInferenceWrapper} to use as input.
     * @return The current instance of {@code MMInferOneInAll} for method chaining.
     */
    public MMInferOneInAll input(AbstractInferenceWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    /**
     * Runs the inference process and returns the result.
     *
     * @return The {@link InferenceResult} containing the schema conversion results and candidates.
     * @throws OtherException if an exception occurs during the process.
     */
    public InferenceResult run() {
        try {
            return innerRun();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    /**
     * The core logic for running the inference process. It prepares wrappers, retrieves schema descriptions,
     * executes candidate mining, and converts the inferred results into schema categories and mappings.
     *
     * @return The {@link InferenceResult} containing the schema conversion results and candidates.
     * @throws Exception if an error occurs during the inference process.
     */
    private InferenceResult innerRun() throws Exception {
        Map<String, AbstractInferenceWrapper> wrappers = prepareWrappers(wrapper);
        Map<String, RecordSchemaDescription> rsds = getRecordSchemaDescriptions(wrappers);

        Candidates candidates = executeCandidateMiner(wrapper, wrapper.getKindNames());

        SchemaConverter schemaConverter = new SchemaConverter();
        List<CategoryMappingPair> pairs = new ArrayList<>();

        for (String kindName : rsds.keySet()) {
            schemaConverter.setNewRSD(rsds.get(kindName), kindName);
            pairs.add(schemaConverter.convertToSchemaCategoryAndMapping());
        }
        return new InferenceResult(pairs, candidates);
    }

    /**
     * Prepares wrappers for each kind name available in the input wrapper.
     *
     * @param inputWrapper The input {@link AbstractInferenceWrapper}.
     * @return A {@link Map} of kind names to their corresponding {@link AbstractInferenceWrapper}.
     * @throws IllegalArgumentException if the input wrapper is invalid.
     */
    private static Map<String, AbstractInferenceWrapper> prepareWrappers(AbstractInferenceWrapper inputWrapper) throws IllegalArgumentException {
        Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();
        System.out.println("getKindNames from the db: " + inputWrapper.getKindNames());

        inputWrapper.getKindNames().forEach(kindName -> {
            final var wrapper = inputWrapper.copyForKind(kindName);
            wrappers.put(kindName, wrapper);
        });
        return wrappers;
    }

    /**
     * Retrieves the record schema descriptions for each wrapper.
     *
     * @param wrappers A {@link Map} of kind names to their corresponding {@link AbstractInferenceWrapper}.
     * @return A {@link Map} of kind names to their corresponding {@link RecordSchemaDescription}.
     */
    private Map<String, RecordSchemaDescription> getRecordSchemaDescriptions(Map<String, AbstractInferenceWrapper> wrappers) {
        return wrappers.entrySet().stream()
            .peek(entry -> System.out.println("Processing element: " + entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> executeRBA(entry.getValue(), true)));
    }

    /**
     * Executes the Record-Based Algorithm (RBA) for a given wrapper.
     *
     * @param wrapper The {@link AbstractInferenceWrapper} to process.
     * @param printSchema {@code true} if the schema should be printed; {@code false} otherwise.
     * @return The resulting {@link RecordSchemaDescription}.
     */
    private static RecordSchemaDescription executeRBA(AbstractInferenceWrapper wrapper, boolean printSchema) {
        RecordBasedAlgorithm rba = new RecordBasedAlgorithm();
        AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();

        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = rba.process(wrapper, merge);
        long end = System.currentTimeMillis();

        System.out.println("RESULT_TIME_RECORD_BA TOTAL: " + (end - start) + "ms");
        System.out.println("RSD: " + rsd);

        return rsd;
    }

    /**
     * Executes the Candidate Miner Algorithm to find potential candidates.
     *
     * @param wrapper The {@link AbstractInferenceWrapper} to use for candidate mining.
     * @param kinds A {@link List} of kind names to process.
     * @return The resulting {@link Candidates}.
     * @throws Exception if an error occurs during the candidate mining process.
     */
    public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
        // FIXME This "100000" literal should be extracted to a constant. Also, why we use this exact value?
        BloomFilter.setParams(100000, new BasicHashFunction());
        StartingEndingFilter.setParams(100000);
        CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
        Candidates candidates = candidateMiner.process(wrapper, kinds);

        return candidates;
    }

}
