package cz.matfyz.inference;

import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
import cz.matfyz.inference.algorithms.pba.PropertyBasedAlgorithm;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalSeqFunction;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingsPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;

/**
 * The {@code MMInferOneInAll} class is responsible for running the inference process.
 * It prepares the required wrappers, executes candidate mining, and converts the inferred schema
 * descriptions into schema categories and mappings.
 */
public class MMInferOneInAll {

    private static final int BLOOM_FILTER_SIZE = 100000;

    private ControlWrapperProvider provider;

    /**
     * Sets the input wrapper for the inference process.
     */
    public MMInferOneInAll input(ControlWrapperProvider provider) {
        this.provider = provider;
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

    private final SchemaConverter schemaConverter = new SchemaConverter();

    private InferenceResult innerRun() throws Exception {
        final List<CategoryMappingsPair> pairs = provider.getDatasources().stream()
            .map(this::processDatasource)
            .flatMap(list -> list.stream()).toList();

        // TODO This is just a temporary solution
        List<AbstractInferenceWrapper> wrappers = provider.getDatasources().stream()
            .map(d -> provider.getControlWrapper(d).getInferenceWrapper())
            .toList();

        final Candidates candidates = executeCandidateMiner(wrappers);
        candidates.sortPkCandidates();

        return new InferenceResult(pairs, candidates);
    }

    private List<CategoryMappingsPair> processDatasource(Datasource datasource) {
        final var wrapper = this.provider.getControlWrapper(datasource).getInferenceWrapper();
        return wrapper.getKindNames().stream()
            .map(kindName -> processKind(wrapper, datasource, kindName))
            .toList();
    }

    private CategoryMappingsPair processKind(AbstractInferenceWrapper wrapper, Datasource datasource, String kindName) {
        final var wrapperCopy = wrapper.copyForKind(kindName);
        final var rsd = executeRBA(wrapperCopy);
        return schemaConverter.convert(rsd, datasource, kindName);
    }

    private static RecordSchemaDescription executeRBA(AbstractInferenceWrapper wrapper) {
        RecordBasedAlgorithm rba = new RecordBasedAlgorithm();
        AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();

        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = rba.process(wrapper, merge);
        long end = System.currentTimeMillis();

        System.out.println("RESULT_TIME_RECORD_BA TOTAL: " + (end - start) + "ms");
        System.out.println("RSD: " + rsd);

        return rsd;
    }

    public static RecordSchemaDescription executePBA(AbstractInferenceWrapper wrapper) {
        PropertyBasedAlgorithm pba = new PropertyBasedAlgorithm();

        DefaultLocalSeqFunction seqFunction = new DefaultLocalSeqFunction();
        DefaultLocalCombFunction combFunction = new DefaultLocalCombFunction();

        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = pba.process(wrapper, seqFunction, combFunction);
        long end = System.currentTimeMillis();

        System.out.println("RESULT_TIME_PROPERTY_BA TOTAL: " + (end - start) + "ms");
        System.out.println(rsd == null ? "NULL" : rsd);

        return rsd;
    }

    public static Candidates executeCandidateMiner(List<AbstractInferenceWrapper> wrappers) throws Exception {
            BloomFilter.setParams(BLOOM_FILTER_SIZE, new BasicHashFunction());
            StartingEndingFilter.setParams(BLOOM_FILTER_SIZE);
            CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();

            return candidateMiner.process(wrappers);
    }

}
