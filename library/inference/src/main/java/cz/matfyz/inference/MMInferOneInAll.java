/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
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
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper;
import cz.matfyz.core.rsd.utils.BloomFilter;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.utils.StartingEndingFilter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

//// from the old inference version ///

import cz.matfyz.core.utils.InputStreamProvider;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JsonInferenceWrapper;
import cz.matfyz.wrappercsv.CsvInferenceWrapper;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

//// for the new inference version ////
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
    public static String kindName;
	public static List<String> collectionNames;
    public static String schemaCatName;
    public static InputStreamProvider inputStreamProvider;
    public static DatasourceType datasourceType;
    public static String checkpointDir;

	// TODO: Make the input/class prettier
	public MMInferOneInAll input(String appName, String uri, String databaseName, String kindName, List<String> collectionNames, String schemaCatName, InputStreamProvider inputStreamProvider, DatasourceType datasourceType) {
        MMInferOneInAll.appName = appName;
        MMInferOneInAll.uri = uri;
        MMInferOneInAll.databaseName = databaseName;
        MMInferOneInAll.kindName = kindName;
		MMInferOneInAll.collectionNames = collectionNames;
        MMInferOneInAll.schemaCatName = schemaCatName;
        MMInferOneInAll.inputStreamProvider = inputStreamProvider;
        MMInferOneInAll.datasourceType = datasourceType;   
        MMInferOneInAll.checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; // hard coded for now
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


        public static Candidates executeCandidateMiner(AbstractInferenceWrapper wrapper, List<String> kinds) throws Exception {
            // TODO
            BloomFilter.setParams(10000, new BasicHashFunction());
            StartingEndingFilter.setParams(10000);
            CandidateMinerAlgorithm candidateMiner = new CandidateMinerAlgorithm();
            Candidates candidates = candidateMiner.process(wrapper, kinds);

			return candidates;
        }
               

	public static CategoryMappingPair innerRun() throws Exception {
		configureLogging();
		System.out.println("RESULT_TIME ----- ----- ----- ----- -----");
	
		Map<String, AbstractInferenceWrapper> wrappers = prepareWrappers();
	
		Map<String, RecordSchemaDescription> rsds = wrappers.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey,
									  entry -> MMInferOneInAll.executeRBA(entry.getValue(), true)));
		// or this is the written out version
		/*
		Map<String, RecordSchemaDescription> rsds = new HashMap<>();
		for (String collectionName : wrappers.keySet()) {
			RecordSchemaDescription r = MMInferOneInAll.executeRBA(wrappers.get(collectionName), true);
			rsds.put(collectionName, r);
		} */
	
		// TODO: review the merging, though
		RecordSchemaDescription rsd = mergeRecordSchemaDescriptions(rsds);
	
		SchemaConverter scon = new SchemaConverter(rsd, schemaCatName, kindName);
		return scon.convertToSchemaCategoryAndMapping();
	}

	private static Map<String, AbstractInferenceWrapper> prepareWrappers() throws IllegalArgumentException {
		Map<String, AbstractInferenceWrapper> wrappers = new HashMap<>();
		switch (datasourceType) {
			case mongodb:
				collectionNames.forEach(name ->
					wrappers.put(name, new MongoDBInferenceSchemaLessWrapper(sparkMaster, appName, uri, databaseName, name, checkpointDir)));
				break;
			case json:
				wrappers.put("single_collection", new JsonInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir));
				break;
			case csv:
				wrappers.put("single_collection", new CsvInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir));
				break;
			default:
				throw new IllegalArgumentException("Unsupported or undefined datasource type.");
		}
		return wrappers;
	}
	
	private static RecordSchemaDescription mergeRecordSchemaDescriptions(Map<String, RecordSchemaDescription> rsds) {
		if (rsds.size() == 1) {
			return rsds.values().iterator().next();
		}
		return mergeByName(rsds);
	}

	private static void configureLogging() {
		ClassLoader classLoader = MMInferOneInAll.class.getClassLoader();
		String[] loggers = {"org.apache.spark", "org.sparkproject", "io.netty", "org.apache.hadoop", "com.mongodb", "org.mongodb"};
		for (String logger : loggers) {
			LoggingSystem.get(classLoader).setLogLevel(logger, LogLevel.WARN);
		}
	}

	public static RecordSchemaDescription mergeByName(Map<String, RecordSchemaDescription> rsds) {
		RecordSchemaDescription complexRSD = null;
	
		// Traverse through all entries in rsds
		for (RecordSchemaDescription rsd : rsds.values()) {
			if (mergeChildren(rsd, rsds)) {
				complexRSD = rsd;
			}			
		}
		return complexRSD;
	}

		/**
	 * Recursively replace children of the given rsd if their names match any key in rsds.
	 */
	private static boolean mergeChildren(RecordSchemaDescription rsd, Map<String, RecordSchemaDescription> rsds) {
		//System.out.println("mergeChildren, rsd name: " + rsd.getName());
		if (rsd.getChildren() != null && !rsd.getChildren().isEmpty()) {
			ObjectArrayList<RecordSchemaDescription> newChildren = new ObjectArrayList<>();
			
			for (RecordSchemaDescription child : rsd.getChildren()) {
				if (rsds.containsKey(child.getName())) {
					//System.out.println("replacing child: " + child.getName());
					// replace the child with the corresponding RSD from rsds
					RecordSchemaDescription replacementRSD = rsds.get(child.getName());
					replacementRSD.setName(child.getName());
					mergeChildren(replacementRSD, rsds); // ensure to merge its children too
					newChildren.add(replacementRSD);
				} else {
					mergeChildren(child, rsds);
					newChildren.add(child);
				}
			}
	
			// update children of current rsd
			if (newChildren != rsd.getChildren()) {
				rsd.setChildren(newChildren);
				return true;
			}
			return false;
		}
		return false;
	} 

	/// Follow alternative merging methods ///
	/*
	public static RecordSchemaDescription mergeByNames(Map<String, RecordSchemaDescription> rsds) {
		RecordSchemaDescription complexRSD = new RecordSchemaDescription();

		for (String collectionName : rsds.keySet()) {
			RecordSchemaDescription rsd = rsds.get(collectionName);
			//rsdToAdd.setName(collectionName);
			ObjectArrayList<RecordSchemaDescription> children = complexRSD.getChildren();
			//children.add(rsdToAdd);
		}
		return complexRSD;
	} */


	/**
	 * WIP
	 * Merges two RSDs into one, creating a new root
	 */
	/*
	public static RecordSchemaDescription mergeToComplex(Map<String, RecordSchemaDescription> rsds) {
		RecordSchemaDescription complexRSD = new RecordSchemaDescription();
		complexRSD.setName("_");
		// changing the root name "_" to collectionName
		// be aware that I am not setting any additional fields, like shareFirst or shareTotal for the root
		for (String collectionName : rsds.keySet()) {
			RecordSchemaDescription rsdToAdd = rsds.get(collectionName);
			rsdToAdd.setName(collectionName);
			ObjectArrayList<RecordSchemaDescription> children = complexRSD.getChildren();
			children.add(rsdToAdd);
		}
		return complexRSD;
	} */

	/**
	 * WIP
	 * Merges 2 rsds w/o any condition
	 */
	/*
    public static RecordSchemaDescription mergeRSDs(RecordSchemaDescription rsd1, RecordSchemaDescription rsd2) {
        if (rsd1 == null || rsd2 == null) {
			System.out.println("returning just one");
            return rsd1 != null ? rsd1 : rsd2;
        }

        // RecordSchemaDescription mergedRSD = new RecordSchemaDescription(rsd1.name);
		RecordSchemaDescription mergedRSD = new RecordSchemaDescription();
		mergedRSD.setName(rsd1.getName());
        Map<String, RecordSchemaDescription> childMap = new HashMap<>();

        // Process all children from rsd1
        for (RecordSchemaDescription child : rsd1.getChildren()) {
			RecordSchemaDescription ch = new RecordSchemaDescription();
			ch.setName(child.getName());
            childMap.put(child.getName(), ch);
        }
        for (RecordSchemaDescription child : rsd1.getChildren()) {
            RecordSchemaDescription mergedChild = childMap.get(child.getName());
            mergeChildProperties(mergedChild, child);
        }
        for (RecordSchemaDescription child : rsd2.getChildren()) {
            if (childMap.containsKey(child.getName())) {
                RecordSchemaDescription mergedChild = childMap.get(child.getName());
                mergeChildProperties(mergedChild, child);
                mergedChild = mergeRSDs(mergedChild, child);
            } else {
				RecordSchemaDescription ch = new RecordSchemaDescription();
				ch.setName(child.getName());
                childMap.put(child.getName(), ch);
                mergeChildProperties(childMap.get(child.getName()), child);
            }
        }
		ObjectArrayList<RecordSchemaDescription> children = mergedRSD.getChildren();
		for (RecordSchemaDescription ch : childMap.values()) {
			children.add(ch);
		}
        mergedRSD.setChildren(children);
        return mergedRSD;
    }*/

    // helper method to merge properties of two RSDs
	/*
    private static void mergeChildProperties(RecordSchemaDescription target, RecordSchemaDescription source) {
        target.setUnique(target.getUnique() + source.getUnique());
        target.setShareTotal(target.getShareTotal() + source.getShareTotal());
        target.setShareFirst(target.getShareFirst() + source.getShareFirst());
    }*/

}
