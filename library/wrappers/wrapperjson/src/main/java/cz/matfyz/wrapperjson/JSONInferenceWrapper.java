package cz.matfyz.wrapperjson;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;


public class JSONInferenceWrapper extends AbstractInferenceWrapper {

    private SparkSession sparkSession;
    private JavaSparkContext context;
    
    private final String sparkMaster;
    private final String appName;
    private final InputStreamProvider inputStreamProvider;
    private final String checkpointDir = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now
    
    public JSONInferenceWrapper(String sparkMaster, String appName, InputStreamProvider inputStreamProvider) {
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
    // assuming that in the json file one line represent one object
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        //System.out.println("loadRSDs() method run");
        JavaRDD<Document> jsonDocuments = loadDocuments();
        return jsonDocuments.map(new JSONRecordToRSDMapFunction());
    }
    
    public JavaRDD<Document> loadDocuments() {
        //System.out.println("loadDocuments() method run");
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = inputStreamProvider.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println("json file line: " + line);
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error processing input stream: " + e.getMessage());
            return context.emptyRDD();
        }            
        JavaRDD<String> jsonLines = context.parallelize(lines);
        JavaRDD<Document> jsonDocuments = jsonLines.map(Document::parse);
        return jsonDocuments;
    }
    
    
    private RecordSchemaDescription convertJsonToRSD(String line) {
        return null;
    }

    @Override
    //dummy implemenatio, because I think I dont need it now
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;

    }

}
