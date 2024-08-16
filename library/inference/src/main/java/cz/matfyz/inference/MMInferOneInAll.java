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

public class MMInferOneInAll {

    private AbstractInferenceWrapper wrapper;

    public MMInferOneInAll input(AbstractInferenceWrapper wrapper) {
        this.wrapper = wrapper;

        return this;
    }

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

        List<CategoryMappingPair> pairs = new ArrayList<CategoryMappingPair>();

        for (String kindName : rsds.keySet()) {
            schemaConverter.setNewRSD(rsds.get(kindName), kindName);
            pairs.add(schemaConverter.convertToSchemaCategoryAndMapping());
        }
        return new InferenceResult(pairs, candidates);
    }

    private static Map<String, AbstractInferenceWrapper> prepareWrappers(AbstractInferenceWrapper inputWrapper) throws IllegalArgumentException {
        Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();

        System.out.println("getKindNames from the db: " + inputWrapper.getKindNames());

        inputWrapper.getKindNames().forEach(kindName -> {
            System.out.println(kindName);
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

        return rsd;
    }

    public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
        BloomFilter.setParams(10000, new BasicHashFunction());
        StartingEndingFilter.setParams(10000);
        CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
        Candidates candidates = candidateMiner.process(wrapper, kinds);

        return candidates;
    }

}
