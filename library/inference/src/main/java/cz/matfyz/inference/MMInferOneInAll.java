/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package cz.matfyz.inference;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.matfyz.core.utils.InputStreamProvider;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.inference.algorithms.rba.Finalize;
import cz.matfyz.inference.algorithms.rba.RecordBasedAlgorithm;
import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.inference.algorithms.rba.functions.DefaultLocalReductionFunction;
import cz.matfyz.inference.schemaconversion.SchemaConverter;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JSONInferenceWrapper;
import cz.matfyz.wrappercsv.CSVInferenceWrapper;
import cz.matfyz.wrappermongodb.MongoDBInferenceSchemaLessWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

/**
 *
 * @author pavel.koupil
 */
public class MMInferOneInAll {

    public static final String PROPERTY_SPARK_MASTER = "baazizi.sparkMaster";
    private static final String sparkMaster = System.getProperty(PROPERTY_SPARK_MASTER, "local[*]");
    public static  String appName;
    public static String uri;
    public static String databaseName;
    public static String collectionName;
    public static String schemaCatName;
    public static InputStreamProvider inputStreamProvider;
    public static String inputType;
    public static String checkpointDir;

    public MMInferOneInAll input(String appName, String uri, String databaseName, String collectionName, String schemaCatName, InputStreamProvider inputStreamProvider, String inputType) {
        MMInferOneInAll.appName = appName;
        MMInferOneInAll.uri = uri;
        MMInferOneInAll.databaseName = databaseName;
        MMInferOneInAll.collectionName = collectionName;
        MMInferOneInAll.schemaCatName = schemaCatName;
        MMInferOneInAll.inputStreamProvider = inputStreamProvider;
        MMInferOneInAll.inputType = inputType;   
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
    public static CategoryMappingPair innerRun() throws IOException {

        RecordBasedAlgorithm rba = new RecordBasedAlgorithm();
        // might have to move this, but I need to add logic, on whether I am using data from db or straight from file
        AbstractInferenceWrapper wrapper;

        switch(inputType) { //right now using String, but i think this will get fixed, once we combine the two sources of input
            case "Database":
                wrapper = new MongoDBInferenceSchemaLessWrapper(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);
                break;
            case "JsonFile":
                wrapper = new JSONInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir);
                break;
            case "CsvFile":
                wrapper = new CSVInferenceWrapper(sparkMaster, appName, inputStreamProvider, checkpointDir);
                break;
            default:
                wrapper = null;
                System.out.println("Forbidden input type used."); 
                break;
        }
        
        AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();
        Finalize finalize = null;
        long start = System.currentTimeMillis();
        RecordSchemaDescription rsd = rba.process(wrapper, merge, finalize);
        long end = System.currentTimeMillis();

        SchemaConverter scon = new SchemaConverter(rsd, schemaCatName);

        CategoryMappingPair cmp = scon.convertToSchemaCategoryAndMapping();

        return cmp;

        /*
        // *Serialize wrapper to a json* (make sure to use jackson)
        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(scw);
        System.out.println("This is the SchemaCategoryWrapper serialized: " + jsonPayload);

        // *Send the wrapper as a request to mmcat*
        // 1) *With HttpURLConnection*
        //URL from the frontend in the .env file
        String appAUrl = "http://localhost:3201/api/v1/schema-categories/store"; */

    }
    public static CategoryMappingPair main(String[] args) throws IOException {
      String sparkMaster = "localhost";
      String appName = "JSON Schema Inference, Record Based Algorithm";
      String uri = "localhost:3205";
      String databaseName = args[1];
      String collectionName = args[2];
      String checkpointDir = args[0]; 

      //String checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now

      RecordBasedAlgorithm rba = new RecordBasedAlgorithm();

      AbstractInferenceWrapper wrapper = new MongoDBInferenceSchemaLessWrapper(sparkMaster, appName, uri, databaseName, collectionName, checkpointDir);
      AbstractRSDsReductionFunction merge = new DefaultLocalReductionFunction();
      Finalize finalize = null;
      long start = System.currentTimeMillis();
      RecordSchemaDescription rsd = rba.process(wrapper, merge, finalize);
      long end = System.currentTimeMillis();

      SchemaConverter scon = new SchemaConverter(rsd, schemaCatName);

      CategoryMappingPair cmp = scon.convertToSchemaCategoryAndMapping();
      System.out.println("It all went good");
      return cmp;
  }

}
