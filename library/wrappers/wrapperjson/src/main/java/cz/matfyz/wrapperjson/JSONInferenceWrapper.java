package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;

import java.util.HashMap;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;


public class JSONInferenceWrapper extends AbstractInferenceWrapper {

    private SparkSession sparkSession;
    private JavaSparkContext context;
    
    private final String sparkMaster;
    private final String appName;
    private final String filePath = "C:\\Users\\alzbe\\Documents\\mff_mgr\\Diplomka\\Apps\\temp\\checkpoint"; //hard coded for now
    
    public JSONInferenceWrapper(String sparkMaster, String appName, String filePath) {
        this.sparkMaster = sparkMaster;
        this.appName = appName;
        //this.filePath = filePath;
         
    }
    
    @Override
    public void buildSession() {
        sparkSession = SparkSession.builder().master(sparkMaster)
                .appName(appName) // I probs need to add the file here?
                .getOrCreate();
        
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
        JavaRDD<Document> jsonLines = context.textFile(filePath);
        return jsonLines.map(new JSONObjectToRSDMapFunction());
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
