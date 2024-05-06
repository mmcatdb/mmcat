/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package cz.matfyz.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;
import cz.matfyz.inference.algorithms.miner.CandidateMinerAlgorithm;
//import cz.matfyz.inference2.algorithms.miner.CandidateMinerAlgorithmLegacy;
import cz.matfyz.inference.algorithms.pba.PropertyBasedAlgorithm;
//import cz.matfyz.inference2.algorithms.pba.PropertyBasedAlgorithmLegacy;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalCombFunction;
import cz.matfyz.inference.algorithms.pba.functions.DefaultLocalSeqFunction;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.core.rsd.ProcessedProperty; 
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;
import static org.apache.hadoop.crypto.key.KeyProvider.options;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import scala.Tuple2;

//// from the old version ///

import java.io.IOException;

import cz.matfyz.core.utils.InputStreamProvider;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JSONInferenceWrapper;
import cz.matfyz.wrappercsv.CSVInferenceWrapper;
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;


/**
 *
 * @author pavel.koupil
 */
public class MMInferOneInAll {

	public static final String PROPERTY_SPARK_MASTER = "baazizi.sparkMaster";
	private static final String sparkMaster = System.getProperty(PROPERTY_SPARK_MASTER, "local[*]");

	public static String appName;
    public static String uri;
    public static String databaseName;
    public static String collectionName;
    public static String schemaCatName;
    public static InputStreamProvider inputStreamProvider;
    public static DatasourceType datasourceType;
    public static String checkpointDir;

	public MMInferOneInAll input(String appName, String uri, String databaseName, String collectionName, String schemaCatName, InputStreamProvider inputStreamProvider, DatasourceType datasourceType) {
        MMInferOneInAll.appName = appName;
        MMInferOneInAll.uri = uri;
        MMInferOneInAll.databaseName = databaseName;
        MMInferOneInAll.collectionName = collectionName;
        MMInferOneInAll.schemaCatName = schemaCatName;
        MMInferOneInAll.inputStreamProvider = inputStreamProvider;
        MMInferOneInAll.datasourceType = datasourceType;   
        MMInferOneInAll.checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now
        return this;
    } 

	public CategoryMappingPair run() {
        try {
            return innerRun();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

	public static RecordSchemaDescription executeRBA(AbstractInferenceWrapper wrapper, boolean printSchema) {
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

	public static void executePBA(AbstractInferenceWrapper wrapper, boolean printSchema) {
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

        public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper) throws Exception {
            // TODO
            BloomFilter.setParams(10000, new BasicHashFunction());
            StartingEndingFilter.setParams(10000);
            CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
            Candidates candidates = candidateMiner.process(wrapper);

			return candidates;
        }
               

	public static CategoryMappingPair innerRun() throws Exception {

//		Logger.getLogger("org.apache.spark").setLevel(Level.INFO);
//		Logger.getLogger("org.sparkproject").setLevel(Level.INFO);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.apache.spark", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.sparkproject", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("io.netty", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.apache.hadoop", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("com.mongodb", LogLevel.WARN);
		LoggingSystem.get(MMInferOneInAll.class.getClassLoader()).setLogLevel("org.mongodb", LogLevel.WARN);

		AbstractInferenceWrapper wrapper; // = new MongoDBInferenceSchemaLessWrapper2(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);

			System.out.println("RESULT_TIME ----- ----- ----- ----- -----");

		switch(datasourceType) { 
			case mongodb:
				wrapper = new MongoDBInferenceSchemaLessWrapper(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);
				break;
	/* 		case json:
				wrapper = new JSONInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir);
				break;
			case csv:
				wrapper = new CSVInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir);
				break;*/
			default:
				wrapper = null;
				System.out.println("Forbidden input type used."); 
				break;
		}

		Candidates candidates = MMInferOneInAll.executeCandidateMiner(wrapper);

		System.out.println("Candidates");
		System.out.println(candidates);
		
		RecordSchemaDescription rsd = MMInferOneInAll.executeRBA(wrapper, true);
		// MMInferOneInAll.executeRBA(wrapper, printSchema.equalsIgnoreCase("print"));

		SchemaConverter scon = new SchemaConverter(rsd, schemaCatName, collectionName);

        CategoryMappingPair cmp = scon.convertToSchemaCategoryAndMapping();

        return cmp;                   
		

	}
}
