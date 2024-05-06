/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference2.algorithms;

import cz.matfyz.inference2.algorithms.miner.functions.MapRawPropertyToKeyValuePairFunction;
import cz.matfyz.inference2.algorithms.miner.functions.MapReduceRawPropertiesFunction;
import cz.matfyz.inference2.algorithms.miner.functions.ProcessedPropertyFactory;
import cz.matfyz.inference2.algorithms.miner.functions.ReduceRawPropertiesFunction;
import cz.matfyz.inference2.algorithms.pba.functions.AbstractCombFunction;
import cz.matfyz.inference2.algorithms.pba.functions.AbstractSeqFunction;
import cz.matfyz.core.rsd2.*;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper2;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public enum FootprinterLegacy {
	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(FootprinterLegacy.class);

	public List<ProcessedProperty> process(AbstractInferenceWrapper2 wrapper, AbstractSeqFunction merge, AbstractCombFunction merge2,
										   boolean loadSchema, boolean loadData) {
		wrapper.buildSession();
		wrapper.initiateContext();

		try {
                        long start = System.currentTimeMillis();
                        
			JavaPairRDD<RawProperty, Share> propertiesToReduce = wrapper.loadProperties(loadSchema, loadData);
			
                        JavaPairRDD<RawProperty, Share> reducedProperties = propertiesToReduce.reduceByKey(
					new ReduceRawPropertiesFunction()
			);
                        
			JavaRDD<ProcessedProperty> properties = reducedProperties.map(
					new MapReduceRawPropertiesFunction()
			);
                        
			JavaPairRDD<String, ProcessedProperty> pairs = properties.mapToPair(
					new MapRawPropertyToKeyValuePairFunction()
			);
                        
			JavaPairRDD<String, Iterable<ProcessedProperty>> grouppedPairs = pairs.groupByKey();
                        
                        JavaPairRDD<String, ProcessedProperty> aggregatedProcessedProperties = grouppedPairs.aggregateByKey(
                                        ProcessedPropertyFactory.INSTANCE.empty(),
                                        merge,
                                        merge2
                        );

                        List<Tuple2<String, ProcessedProperty>> list = new ArrayList<>(aggregatedProcessedProperties.collect());

			if (list.isEmpty())
				return new ArrayList<>();

			List<ProcessedProperty> processedProperties = new ArrayList<>();
                        
			for (Tuple2<String, ProcessedProperty> processedProperty : list) {
				processedProperty._2.setHierarchicalName(processedProperty._1);
				processedProperties.add(processedProperty._2);
			}
                        
                        // remove later
                        // for(ProcessedProperty item : processedProperties) {
                        //    System.out.println(item.getHeuristics().toString());
                        //}
                        
                        long end = System.currentTimeMillis();
                        System.out.println("RESULT_TIME_FOOTPRINTER TOTAL: " + (end - start) + "ms");

			return processedProperties;
		} finally {
			wrapper.stopSession();
		}

	}

}
