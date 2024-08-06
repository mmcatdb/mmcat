package cz.matfyz.inference;

import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.inference.common.RecordSchemaDescriptionMerger;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

//// from the old inference version ///

import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;

public class MMInferOneInAll {

    private AbstractInferenceWrapper wrapper;
    private String kindName;
    private String categoryLabel;

    public MMInferOneInAll input(AbstractInferenceWrapper wrapper, String kindName, String categoryLabel) {
        this.wrapper = wrapper;
        this.kindName = kindName;
        this.categoryLabel = categoryLabel;

        return this;
    }

    public List<CategoryMappingPair> run() {
        try {
            return innerRun();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private List<CategoryMappingPair> innerRun() throws Exception {
        System.out.println("RESULT_TIME ----- ----- ----- ----- -----");

        Map<String, AbstractInferenceWrapper> wrappers = prepareWrappers(wrapper);

        Map<String, RecordSchemaDescription> rsds = getRecordSchemaDescriptions(wrappers);

        //RecordSchemaDescription rsd = mergeRecordSchemaDescriptions(rsds);

        SchemaConverter schemaConverter = new SchemaConverter(categoryLabel);

        List<CategoryMappingPair> pairs = new ArrayList<CategoryMappingPair>();

        for (String kindName : rsds.keySet()) {
            schemaConverter.setNewRSD(rsds.get(kindName), kindName);
            pairs.add(schemaConverter.convertToSchemaCategoryAndMapping());
        }
        return pairs;
    }

    private static Map<String, AbstractInferenceWrapper> prepareWrappers(AbstractInferenceWrapper inputWrapper) throws IllegalArgumentException {
        Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();

        System.out.println("getKindNames from the db: " + inputWrapper.getKindNames());

        inputWrapper.getKindNames().forEach(kindName -> {
            System.out.println(kindName);
            final AbstractInferenceWrapper copy = inputWrapper.copy();
            copy.kindName = kindName;

            wrappers.put(kindName, copy);
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

        if (printSchema) {
            System.out.print("RESULT_RECORD_BA: ");
            System.out.println(rsd);
        }

        System.out.println("RESULT_TIME_RECORD_BA TOTAL: " + (end - start) + "ms");

        return rsd;
    }

    private RecordSchemaDescription mergeRecordSchemaDescriptions(Map<String, RecordSchemaDescription> rsds) throws Exception {
        // There are two types of candidates available at the moment: primary key and reference
        // If there are any primary key candidates, then the RSDs can be merged into one (but be aware of the _id default identifier in mongo, this shouldn't count)
        // If there are sany reference candidates, then these RSDs can be merged hierarchically, based on who is referencing who

        // TODO: Check better the output of the candidate miner, do the references and pks always make sense?

        // If I merge based on primary key, then no other merging will be necessary,
        // because primary key is present in all RSDs and so all can be merged together
        // So it is either merging based on primary key or reference, or none

        if (rsds.size() <= 1) { // Do not run the candidates, if there is only one rsd
            return rsds.values().iterator().next();
        }
        Candidates candidates = executeCandidateMiner(wrapper, wrapper.getKindNames());
        System.out.println("Candidates: " + candidates);

        PrimaryKeyCandidate pkCandidate = Candidates.firstPkCandidatesThatNotEndWith(candidates, "/_id");
        // for Yelp; delete afterwards!
        // PrimaryKeyCandidate pkCandidate = new PrimaryKeyCandidate(new Object(), "review/business_id", false);
        // for IMDb; delete afterwards!
        // PrimaryKeyCandidate pkCandidate = new PrimaryKeyCandidate(new Object(), "title_basics/tconst", false);
        if (!candidates.pkCandidates.isEmpty() && pkCandidate != null) { // but what if the db isn't mongo and there is an actual property named "_id"?
            // get the first PK candidate which will be in the candidate array and wont end with "/_id" and merge all based on that
            return RecordSchemaDescriptionMerger.mergeBasedOnPrimaryKey(rsds, pkCandidate);
        } else if (!candidates.refCandidates.isEmpty()) { // there could be multiple reference candidates and I need to process them all
            for (ReferenceCandidate refCandidate : candidates.refCandidates) { // very very wonky :o
                rsds = RecordSchemaDescriptionMerger.mergeBasedOnReference(rsds, refCandidate);
            }
            return rsds.values().iterator().next(); // now there should be just one key-value pair, because the algo merged it all
        } else { // multiple RSDs, but no candidates
            throw new IllegalStateException("No candidates for merging found.");
        }
    }

    public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
        BloomFilter.setParams(10000, new BasicHashFunction());
        StartingEndingFilter.setParams(10000);
        CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
        Candidates candidates = candidateMiner.process(wrapper, kinds);

        return candidates;
    }

}
