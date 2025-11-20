package cz.matfyz.inference;

import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
import cz.matfyz.inference.algorithms.pba.PropertyBasedAlgorithm;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalSeqFunction;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.Hashing;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(MMInferOneInAll.class);

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

        List<AbstractInferenceWrapper> wrappers = provider.getDatasources().stream()
            .flatMap(datasource -> {
                final var control = provider.getControlWrapper(datasource);
                final var kindNames = control.getPullWrapper().getKindNames();
                // We need a separate wrapper instance for each kind name.
                return kindNames.stream().map(kindName -> control.getInferenceWrapper(kindName));
            })
            .toList();

        final Candidates candidates = executeCandidateMiner(wrappers);
        candidates.sortPkCandidates();

        return new InferenceResult(pairs, candidates);
    }

    private List<CategoryMappingsPair> processDatasource(Datasource datasource) {
        final var control = provider.getControlWrapper(datasource);
        final var kindNames = control.getPullWrapper().getKindNames();
        return kindNames.stream()
            .map(kindName -> processKind(control, datasource, kindName))
            .toList();
    }

    private CategoryMappingsPair processKind(AbstractControlWrapper control, Datasource datasource, String kindName) {
        final var inference = control.getInferenceWrapper(kindName);
        final var rsd = executeRBA(inference);
        //final var rsd = executePBA(inference);
        return schemaConverter.convert(rsd, datasource, kindName);
    }

    private static RecordSchemaDescription executeRBA(AbstractInferenceWrapper wrapper) {
        final var rba = new RecordBasedAlgorithm();
        final var merge = new DefaultLocalReductionFunction();

        final long start = System.nanoTime();
        final var rsd = rba.process(wrapper, merge);
        final long end = System.nanoTime();

        final var total = end - start;
        LOGGER.debug("executeRBA:\n- total time: {} ms\n- RSD: {}", total, rsd);

        return rsd;
    }

    public static RecordSchemaDescription executePBA(AbstractInferenceWrapper wrapper) {
        final var pba = new PropertyBasedAlgorithm();

        final var seqFunction = new DefaultLocalSeqFunction();
        final var combFunction = new DefaultLocalCombFunction();

        final long start = System.nanoTime();
        final var rsd = pba.process(wrapper, seqFunction, combFunction);
        final long end = System.nanoTime();

        System.out.println("RESULT_TIME_PROPERTY_BA TOTAL: " + (end - start) + "ms");
        System.out.println("RSD: " + rsd == null ? "NULL" : rsd);

        return rsd;
    }

    public static Candidates executeCandidateMiner(List<AbstractInferenceWrapper> wrappers) throws Exception {
        BloomFilter.setParams(BLOOM_FILTER_SIZE, Hashing::basicHash);
        StartingEndingFilter.setParams(BLOOM_FILTER_SIZE);
        final var candidateMiner = new CandidateMinerAlgorithm();

        return candidateMiner.process(wrappers, false);
    }

}
