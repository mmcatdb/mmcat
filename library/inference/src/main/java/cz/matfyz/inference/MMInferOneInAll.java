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

    private static final int BLOOM_FILTER_SIZE = 100000;

    private AbstractInferenceWrapper wrapper;

    /**
     * Sets the input wrapper for the inference process.
     */
    public MMInferOneInAll input(AbstractInferenceWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    /**
     * Runs the inference process and returns the result.
     */
    public InferenceResult run() {
        try {
            return innerRun();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private InferenceResult innerRun() throws Exception {
        Map<String, AbstractInferenceWrapper> wrappers = prepareWrappers(wrapper);
        Map<String, RecordSchemaDescription> rsds = getRecordSchemaDescriptions(wrappers);

        Candidates candidates = executeCandidateMiner(wrapper, wrapper.getKindNames());

        SchemaConverter schemaConverter = new SchemaConverter();
        List<CategoryMappingPair> pairs = new ArrayList<>();

        for (final var entry : rsds.entrySet()) {
            schemaConverter.setNewRSD(entry.getValue(), entry.getKey());
            pairs.add(schemaConverter.convertToSchemaCategoryAndMapping());
        }
        return new InferenceResult(pairs, candidates);
    }

    private static Map<String, AbstractInferenceWrapper> prepareWrappers(AbstractInferenceWrapper inputWrapper) throws IllegalArgumentException {
        Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();

        inputWrapper.getKindNames().forEach(kindName -> {
            final var wrapper = inputWrapper.copyForKind(kindName);
            wrappers.put(kindName, wrapper);
        });
        return wrappers;
    }

    private Map<String, RecordSchemaDescription> getRecordSchemaDescriptions(Map<String, AbstractInferenceWrapper> wrappers) {
        return wrappers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> executeRBA(entry.getValue(), true)));
    }

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
     */
    public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
        BloomFilter.setParams(BLOOM_FILTER_SIZE, new BasicHashFunction());
        StartingEndingFilter.setParams(BLOOM_FILTER_SIZE);
        CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();

        return candidateMiner.process(wrapper, kinds);
    }

}
