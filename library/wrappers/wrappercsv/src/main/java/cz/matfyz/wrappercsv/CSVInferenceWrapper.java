package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.core.utils.InputStreamProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;



public class CSVInferenceWrapper extends AbstractInferenceWrapper {

    private SparkSession sparkSession;
    private JavaSparkContext context;
    
    private final String sparkMaster;
    private final String appName;
    private final InputStreamProvider inputStreamProvider;
    private final String checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now
    
    public CSVInferenceWrapper(String sparkMaster, String appName, InputStreamProvider inputStreamProvider) {
        this.sparkMaster = sparkMaster;
        this.appName = appName;
        this.inputStreamProvider = inputStreamProvider;     
    }
    
    @Override
    public void buildSession() {
        sparkSession = SparkSession.builder().master(sparkMaster)
                .appName(appName) // I probs need to add the file here?
                .getOrCreate();
        context = new JavaSparkContext(sparkSession.sparkContext());
        context.setLogLevel("ERROR");
        
    }

    @Override
    public void stopSession() {
            sparkSession.stop();      
    }
    
    @Override
    public void initiateContext() {
        context = new JavaSparkContext(sparkSession.sparkContext());
        context.setLogLevel("ERROR");        
    }

    @Override
    //dummy implemenatio, because I think I dont need it now
    public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        return null;
    }


    @Override
    // assuming that the first line of the csv is the header
    // also assuming the csv is comma delimited
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        JavaRDD<Map<String, String>> csvDocuments = loadDocuments();
        return csvDocuments.map(new CSVRecordToRSDMapFunction());
    }
    
    public JavaRDD<Map<String, String>> loadDocuments() {
        List<Map<String, String>> lines = new ArrayList<>();
        boolean firstLine = false;
        String[] header = null;
        
        try (InputStream inputStream = inputStreamProvider.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println("json file line: " + line);
                String[] elements = line.split(",\\s*");
                if (!firstLine) {
                    header = elements;
                    //header = new ArrayList<>(Arrays.asList(elements));
                    firstLine = true;                            
                }
                else {
                    //assuming there is no data missing 
                    Map<String, String> lineMap = new HashMap<String, String>();
                    for (int i = 0; i < elements.length; i++) {
                        lineMap.put(header[i], elements[i]);
                    }
                    lines.add(lineMap);
                }             
            }
        } catch (IOException e) {
            System.err.println("Error processing input stream: " + e.getMessage());
            return context.emptyRDD();
        }            
        JavaRDD<Map<String, String>> csvDocuments = context.parallelize(lines);
        return csvDocuments;
    }

    @Override
    //dummy implemenatio, because I think I dont need it now
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;

    }

}
