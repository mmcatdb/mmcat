package cz.matfyz.inference.algorithms.miner;

import cz.matfyz.inference.algorithms.Footprinter;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import cz.matfyz.core.rsd.SubsetType;
import scala.Tuple2;
import cz.matfyz.inference.algorithms.miner.functions.ReferenceTupleToPairWithSubsetTypeMapFunction;

public class CandidateMinerAlgorithm implements Serializable {

    // TODO: CandidateMiner needs optimalization. When datasets have too many attributes the algorithm runs out of memory quickly looking for the Reference Candidates
    public Candidates process(List<AbstractInferenceWrapper> wrappers, boolean mineReferences) {
        Candidates candidates = new Candidates();
        List<AbstractInferenceWrapper> sessionWrappers = new ArrayList<>();

        try {
            JavaRDD<PropertyHeuristics> allHeuristics = collectAllHeuristics(wrappers, sessionWrappers);
            if (allHeuristics == null) {
                return candidates;
            }

            JavaRDD<PropertyHeuristics> primaryKeyCandidates = filterPrimaryKeyCandidates(allHeuristics);
            JavaRDD<PropertyHeuristics> suitableProperties = allHeuristics.filter(heuristics -> heuristics.getMin() != null);
            JavaRDD<PropertyHeuristics> suitablePrimaryKeys = primaryKeyCandidates.filter(heuristics -> heuristics.getMin() != null);
            collectPrimaryKeyCandidates(primaryKeyCandidates, candidates);

            if (mineReferences) {
                List<Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType>> referenceCandidateList = collectReferenceCandidates(suitablePrimaryKeys, suitableProperties);
                collectReferenceCandidates(referenceCandidateList, candidates);
            }

            return candidates;
        } finally {
            stopAllSessions(sessionWrappers);
        }
    }

    private JavaRDD<PropertyHeuristics> collectAllHeuristics(List<AbstractInferenceWrapper> wrappers, List<AbstractInferenceWrapper> sessionWrappers) {
        JavaRDD<PropertyHeuristics> all = null;

        for (final AbstractInferenceWrapper wrapper : wrappers) {
            for (final String kindName : wrapper.getKindNames()) {
                AbstractInferenceWrapper w = wrapper.copyForKind(kindName);
                sessionWrappers.add(w);
                w.startSession();
                JavaRDD<PropertyHeuristics> heuristics = Footprinter.INSTANCE.process(w);
                all = (all == null) ? heuristics : all.union(heuristics);
            }
        }

        return all;
    }

    private JavaRDD<PropertyHeuristics> filterPrimaryKeyCandidates(JavaRDD<PropertyHeuristics> allHeuristics) {
        return allHeuristics.filter(heuristics -> {
            boolean maxNotArray = heuristics.getMax() == null || !(heuristics.getMax() instanceof java.util.ArrayList);
            boolean hierarchicalNameValid = heuristics.getHierarchicalName() == null || !heuristics.getHierarchicalName().endsWith(RecordSchemaDescription.ROOT_SYMBOL);
            return heuristics.isRequired() && heuristics.isUnique() && maxNotArray && hierarchicalNameValid;
        });
    }

    private List<Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType>> collectReferenceCandidates(JavaRDD<PropertyHeuristics> suitablePrimaryKeys, JavaRDD<PropertyHeuristics> suitableProperties) {
        return suitablePrimaryKeys.cartesian(suitableProperties)
            .map(pair -> new Tuple2<>(pair._1, pair._2))
            .mapToPair(new ReferenceTupleToPairWithSubsetTypeMapFunction())
            .filter(pair -> pair._2 != SubsetType.EMPTY)
            .collect();
    }

    private void collectPrimaryKeyCandidates(JavaRDD<PropertyHeuristics> primaryKeyCandidates, Candidates candidates) {
        primaryKeyCandidates.collect().forEach(heuristics -> {
            PrimaryKeyCandidate pkCandidate = toPrimaryKeyCandidate(heuristics);
            candidates.pkCandidates.add(pkCandidate);
        });
    }

    private void collectReferenceCandidates(List<Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType>> referenceCandidateList, Candidates candidates) {
        referenceCandidateList.forEach(tuple -> {
            PropertyHeuristics referencing = tuple._1._1;
            PropertyHeuristics referred = tuple._1._2;
            SubsetType subsetType = tuple._2;
            ReferenceCandidate refCandidate = toReferenceCandidate(referencing, referred, subsetType);
            candidates.refCandidates.add(refCandidate);
        });
    }

    private void stopAllSessions(List<AbstractInferenceWrapper> sessionWrappers) {
        for (AbstractInferenceWrapper wrap : sessionWrappers)
            wrap.stopSession();
    }

    private PrimaryKeyCandidate toPrimaryKeyCandidate(PropertyHeuristics heuristics) {
        return new PrimaryKeyCandidate(heuristics, heuristics.getHierarchicalName(), false);
    }

    private ReferenceCandidate toReferenceCandidate(PropertyHeuristics referencing, PropertyHeuristics referred, SubsetType subsetType) {
        return new ReferenceCandidate(subsetType, referred, referencing, referred.getHierarchicalName(), referencing.getHierarchicalName(), false, false);
    }
}
