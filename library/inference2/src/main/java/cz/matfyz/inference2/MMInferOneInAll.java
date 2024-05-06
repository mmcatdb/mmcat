/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package cz.matfyz.inference2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;
import cz.matfyz.inference2.algorithms.miner.CandidateMinerAlgorithm;
//import cz.matfyz.inference2.algorithms.miner.CandidateMinerAlgorithmLegacy;
import cz.matfyz.inference2.algorithms.pba.PropertyBasedAlgorithm;
//import cz.matfyz.inference2.algorithms.pba.PropertyBasedAlgorithmLegacy;
import cz.matfyz.inference2.algorithms.pba.functions.DefaultLocalCombFunction;
import cz.matfyz.inference2.algorithms.pba.functions.DefaultLocalSeqFunction;
import cz.matfyz.inference2.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference2.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference2.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd2.ProcessedProperty;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper2;
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper2;
import cz.matfyz.core.rsd2.utils.BloomFilter;
import cz.matfyz.core.rsd2.utils.BasicHashFunction;
import cz.matfyz.core.rsd2.Candidates;
import cz.matfyz.core.rsd2.utils.StartingEndingFilter;
import java.util.List;
import static org.apache.hadoop.crypto.key.KeyProvider.options;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import scala.Tuple2;

/**
 *
 * @author pavel.koupil
 */
public class MMInferOneInAll {

	public static final String PROPERTY_SPARK_MASTER = "baazizi.sparkMaster";
	private static final String sparkMaster = System.getProperty(PROPERTY_SPARK_MASTER, "local[*]");

	public static void executeRBA(AbstractInferenceWrapper2 wrapper, boolean printSchema) {
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
	}

	public static void executePBA(AbstractInferenceWrapper2 wrapper, boolean printSchema) {
		PropertyBasedAlgorithm pba = new PropertyBasedAlgorithm();

		DefaultLocalSeqFunction seqFunction = new DefaultLocalSeqFunction();
		DefaultLocalCombFunction combFunction = new DefaultLocalCombFunction();

		long start = System.currentTimeMillis();
		RecordSchemaDescription rsd = pba.process(wrapper, seqFunction, combFunction);

//		RecordSchemaDescription rsd2 = finalize.process();		// TODO: SLOUCIT DOHROMADY!
		long end = System.currentTimeMillis();

		if (printSchema) {
			System.out.print("RESULT_PROPERTY_BA: ");
			System.out.println(rsd == null ? "NULL" : rsd);
		}

		System.out.println("RESULT_TIME_PROPERTY_BA TOTAL: " + (end - start) + "ms");
	}

/* 	public static void executePBALegacy(AbstractInferenceWrapper2 wrapper, boolean stop) {
		PropertyBasedAlgorithmLegacy pba = new PropertyBasedAlgorithmLegacy();

		DefaultLocalSeqFunction seqFunction = new DefaultLocalSeqFunction();
		DefaultLocalCombFunction combFunction = new DefaultLocalCombFunction();

		long start = System.currentTimeMillis();
		RecordSchemaDescription rsd = pba.process(wrapper, seqFunction, combFunction, stop);
//		RecordSchemaDescription rsd2 = finalize.process();		// TODO: SLOUCIT DOHROMADY!
		long end = System.currentTimeMillis();

		System.out.print("RESULT_PROPERTY_LEGACY: ");
		System.out.println(rsd == null ? "NULL" : rsd);
		System.out.println("RESULT_TIME_LEGACY TOTAL: " + (end - start) + "ms");
	}*/

        public static void executeCandidateMiner(AbstractInferenceWrapper2 wrapper) throws Exception {
            // TODO
            BloomFilter.setParams(10000, new BasicHashFunction());
            StartingEndingFilter.setParams(10000);
            CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
            Candidates candidates = candidateMiner.process(wrapper);
        }
               
/*         public static void executeCandidateMinerLegacy(AbstractInferenceWrapper2 wrapper) throws Exception {
		BloomFilter.setParams(10000, new BasicHashFunction());
                StartingEndingFilter.setParams(10000);
                CandidateMinerAlgorithmLegacy candidateMiner = new CandidateMinerAlgorithmLegacy();
                
                long start = System.currentTimeMillis();
                Candidates candidates = candidateMiner.process(wrapper);
                
                System.out.println("RESULT_CANDIDATE_MINER_LEGACY: ");
                System.out.println(candidates.toString());
                long end = System.currentTimeMillis();
		System.out.println("RESULT_TIME_LEGACY TOTAL: " + (end - start) + "ms");
        }*/

	public static void main(String... args) throws Exception {

//		Logger.getLogger("org.apache.spark").setLevel(Level.INFO);
//		Logger.getLogger("org.sparkproject").setLevel(Level.INFO);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.apache.spark", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.sparkproject", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("io.netty", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.apache.hadoop", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("com.mongodb", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.mongodb", LogLevel.WARN);

		if (args != null /*& args.length == 3*/) {
                    
                        // ------------------------
                        // Scanner scanner = new Scanner(System.in);
                        // scanner.nextLine();
                        // scanner.close();
                        // ------------------------
                    
			String appName = "JSON Schema Inference, Record Based Algorithm";
			String uri = "localhost:27017";
			String databaseName = args[1]; // contos
			String collectionName = args[2]; // wikidata128k
			String checkpointDir = args[0]; // /home/simekjan/temp/checkpoint
			String printSchema;
			if (args.length >= 4) {
				printSchema = args[3];
			} else {
				printSchema = "NO";
			}
			AbstractInferenceWrapper2 wrapper = new MongoDBInferenceSchemaLessWrapper2(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);

       			System.out.println("RESULT_TIME ----- ----- ----- ----- -----");
                        
			// MMInferOneInAll.executeRBA(wrapper, printSchema.equalsIgnoreCase("print"));

                        // MMInferOneInAll.executePBALegacy(wrapper, false);
			// MMInferOneInAll.executePBA(wrapper, true);
                        
                        // MMInferOneInAll.executeCandidateMinerLegacy(wrapper);
                        System.out.println("-------------------------------------konec-legacy----------------------------------");
                        MMInferOneInAll.executeCandidateMiner(wrapper);
		}

	}
}
