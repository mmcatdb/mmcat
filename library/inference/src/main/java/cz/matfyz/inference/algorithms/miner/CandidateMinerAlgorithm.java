package cz.matfyz.inference.algorithms.miner;

import cz.matfyz.inference.algorithms.Footprinter;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.PrimaryKeyCandidate;
import cz.matfyz.core.rsd.ReferenceCandidate;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import cz.matfyz.core.rsd.SubsetType;
import cz.matfyz.inference.algorithms.miner.functions.SuitableReferencePropertiesFilterFunction;
import org.apache.spark.api.java.JavaPairRDD;
import scala.Tuple2;
import cz.matfyz.inference.algorithms.miner.functions.ReferenceTupleToPairWithSubsetTypeMapFunction;

/*
 * When I got this class, the process() method was not returning anything.
 * I added the return statement (and the conversions from raw candidates)
 */
public class CandidateMinerAlgorithm implements Serializable {

    public Candidates process(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {

        Candidates candidates = new Candidates();

        List<AbstractInferenceWrapper> wrappers = new ArrayList<>();

        try {
            JavaRDD<PropertyHeuristics> all = null;

            for (String kind : kinds) {
                System.out.println("Current kind while processing candidates: " + kind);
                final var w = wrapper.copyForKind(kind);
                wrappers.add(w);
                w.startSession();
                JavaRDD<PropertyHeuristics> heuristics = Footprinter.INSTANCE.process(w);
                if (all == null)
                    all = heuristics;
                else
                    all.union(heuristics);
            }

            // remove later
            all.foreach(new VoidFunction<PropertyHeuristics>() {
                @Override
                public void call(PropertyHeuristics h) throws Exception {
                    System.out.println(h);
                }
            });

            // PrimaryKeyCandidates
            JavaRDD<PropertyHeuristics> primaryKeyCandidates = all.filter(
                new Function<PropertyHeuristics, Boolean>() {
                    @Override
                    public Boolean call(PropertyHeuristics heuristics) throws Exception {
                        return (heuristics.isRequired() && heuristics.isUnique());
                    }
                }
            );

            // remove later
            System.out.println("----------------------- Primary Key Candidates --------------------------------------");

            // remove later
            primaryKeyCandidates.foreach(new VoidFunction<PropertyHeuristics>() {
                @Override
                public void call(PropertyHeuristics h) throws Exception {
                    System.out.println(h);
                }
            });

            // ReferenceCandidates
            JavaRDD<PropertyHeuristics> suitableProperties = all.filter(
                new SuitableReferencePropertiesFilterFunction()
            );

            JavaRDD<PropertyHeuristics> suitablePrimaryKeys = primaryKeyCandidates.filter(
                new SuitableReferencePropertiesFilterFunction()
            );

            // first heuristics are primaryKey, second the referenced property, SubsetType is reference type
            JavaPairRDD<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType> referenceCandidates = suitablePrimaryKeys
                .cartesian(suitableProperties)
                .map(pair -> new Tuple2<>(pair._1, pair._2))
                .mapToPair(new ReferenceTupleToPairWithSubsetTypeMapFunction())
                .filter(pair -> pair._2 != SubsetType.EMPTY);

            System.out.println("----------------------- Reference Candidates --------------------------------------");
            List<Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType>>  rc = new ArrayList<>(referenceCandidates.collect());

            for (Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType> t: rc) {
                System.out.println(t);
            }


            // TODO: redundancy

            primaryKeyCandidates.collect().forEach(heuristics -> {
                PrimaryKeyCandidate pkCandidate = toPrimaryKeyCandidate(heuristics);
                candidates.pkCandidates.add(pkCandidate);
            });

            // Convert reference candidates to ReferenceCandidate and add to candidates
            rc.forEach(tuple -> {
                PropertyHeuristics referencing = tuple._1._1;
                PropertyHeuristics referred = tuple._1._2;
                SubsetType subsetType = tuple._2;
                ReferenceCandidate refCandidate = toReferenceCandidate(referencing, referred, subsetType);
                candidates.refCandidates.add(refCandidate);
            });

            return candidates;
        }
        finally {
            for (AbstractInferenceWrapper wrap : wrappers) {
                wrap.stopSession();
            }
        }
    }

    private PrimaryKeyCandidate toPrimaryKeyCandidate(PropertyHeuristics heuristics) {
        return new PrimaryKeyCandidate(heuristics, heuristics.getHierarchicalName(), false);
    }

    private ReferenceCandidate toReferenceCandidate(PropertyHeuristics referencing, PropertyHeuristics referred, SubsetType subsetType) {
        return new ReferenceCandidate(subsetType, referred, referencing, referred.getHierarchicalName(), referencing.getHierarchicalName(), false, false);
    }

}
