package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.core.utils.InputStreamProvider;
import cz.matfyz.wrappercsv.inference.functions.CSVRecordToRSDMapFunction;

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

public class CsvInferenceWrapper extends AbstractInferenceWrapper {

    private SparkSession sparkSession;
    private JavaSparkContext context;

    private final String sparkMaster;
    private final String appName;
    private final InputStreamProvider inputStreamProvider;
    private final String checkpointDir;

    public CsvInferenceWrapper(String sparkMaster, String appName, InputStreamProvider inputStreamProvider, String checkpointDir) {
        this.sparkMaster = sparkMaster;
        this.appName = appName;
        this.inputStreamProvider = inputStreamProvider;
        this.checkpointDir = checkpointDir;
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
        JavaSparkContext newContext = new JavaSparkContext(sparkSession.sparkContext());
        newContext.setLogLevel("ERROR");
        newContext.setCheckpointDir(checkpointDir);

        List<Map<String, String>> lines = new ArrayList<>();
        boolean firstLine = false;
        String[] header = null;

        try (InputStream inputStream = inputStreamProvider.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming the file only uses commas as delimiter and not in any other way!
                String[] elements = line.split(",\\s*");
                if (!firstLine) {
                    header = elements;
                    firstLine = true;
                }
                else {
                    //assuming there is no data missing
                    Map<String, String> lineMap = new HashMap<String, String>();
                    for (int i = 0; i < elements.length; i++) {
                        lineMap.put(header[i], elements[i]);
                        //System.out.println("header: " + header[i] + " column valu: " + elements[i]);
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
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;

    }

    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    @Override
    public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertyData'");
    }

    @Override
    public AbstractInferenceWrapper copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

}
