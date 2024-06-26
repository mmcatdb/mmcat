package cz.matfyz.inference.algorithms.miner;

import cz.matfyz.inference.algorithms.Footprinter;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.PropertyHeuristics;
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

public class CandidateMinerAlgorithm implements Serializable {

    public Candidates process(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {

        wrapper.buildSession();
        wrapper.initiateContext();

        List<AbstractInferenceWrapper> wrappers = new ArrayList<>();

        try {
            //List<String> kinds = new ArrayList<>();
            //kinds.add("apps");
            //kinds.add("reviews");
            // kinds.add("imdb4k");
            //kinds.add("wikidata40");

            JavaRDD<PropertyHeuristics> all = null;

            for (String kind : kinds) {
                AbstractInferenceWrapper w = wrapper.copy();
                wrappers.add(w);
                w.kindName = kind;
                w.buildSession();
                w.initiateContext();
                JavaRDD<PropertyHeuristics> heuristics = Footprinter.INSTANCE.process(w);
                if (all == null)
                    all = heuristics;
                else
                    all.union(heuristics);
            }

            System.out.println("RDD print all:");

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

            return new Candidates();
        }
        finally {
            for (AbstractInferenceWrapper wrap : wrappers) {
                wrap.stopSession();
            }
        }
    }

}
