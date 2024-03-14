/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.wrappermongodb;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;

import cz.matfyz.wrappermongodb.inference.functions.MongoRawPropertyPairFunction;
import cz.matfyz.wrappermongodb.inference.functions.MongoRecordToDataRawPropertyFlatMapFunction;
import cz.matfyz.wrappermongodb.inference.functions.MongoRecordToFullRawPropertyFlatMapFunction;
import cz.matfyz.wrappermongodb.inference.functions.MongoRecordToPairFunction;
import cz.matfyz.wrappermongodb.inference.functions.MongoRecordToRSDMapFunction;
import cz.matfyz.wrappermongodb.inference.functions.MongoRecordToSchemaRawPropertyFlatMapFunction;
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
import org.bson.Document;

/**
 *
 * @author pavel.koupil
 */

public class MongoDBInferenceSchemaLessWrapper extends AbstractInferenceWrapper {

    private SparkSession sparkSession;
    private JavaSparkContext context;

    private final String sparkMaster;
    private final String appName;
    private final String uri;
    private final String databaseName;

    private final String checkpointDir;

    public MongoDBInferenceSchemaLessWrapper(String sparkMaster, String appName, String uri, String databaseName, String collectionName, String checkpointDir) {
        this.sparkMaster = sparkMaster;
        this.appName = appName;
        this.uri = uri;
        this.databaseName = databaseName;
        this.kindName = collectionName;
        this.checkpointDir = checkpointDir;
    }

    @Override
    public void buildSession() {
        sparkSession = SparkSession.builder().master(sparkMaster)
                .appName(appName)//"JSON Schema Inference, Universal Multi-Model Approach"
                .config("spark.mongodb.input.uri", "mongodb://" + uri + "/")
                .config("spark.mongodb.input.database", databaseName)
                .config("spark.mongodb.input.collection", kindName)
                //                .config("spark.sql.streaming.checkpointLocation", "/home/contos/temp/checkpoint")
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
    public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        //System.out.println("Using loadProperties()");
        JavaMongoRDD<Document> records = loadRecords();
        if (loadSchema && loadData) {
            return records
                    .flatMap(new MongoRecordToFullRawPropertyFlatMapFunction(databaseName + "." + kindName))
                    .mapToPair(new MongoRawPropertyPairFunction());
        } else if (loadData) {
            return records
                    .flatMap(new MongoRecordToDataRawPropertyFlatMapFunction(databaseName + "." + kindName))
                    .mapToPair(new MongoRawPropertyPairFunction());
        } else if (loadSchema) {
            return records
                    .flatMap(new MongoRecordToSchemaRawPropertyFlatMapFunction(databaseName + "." + kindName))
                    .mapToPair(new MongoRawPropertyPairFunction());
        } else {
            return null;
            // TODO! MAPUJE SE VZDY SCHEMA? MAPUJI SE VZDY DATA?
        }
    }

    @Override
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        //System.out.println("Using loadRSDs()");
        JavaMongoRDD<Document> records = loadRecords();
        return records.map(new MongoRecordToRSDMapFunction());
    }

    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        //System.out.println("Using loadRSDPairs()");
        JavaMongoRDD<Document> records = loadRecords();
        return records.mapToPair(new MongoRecordToPairFunction());
    }

    private JavaMongoRDD<Document> loadRecords() {
        //System.out.println("Using loadRecords()");
        JavaSparkContext newContext = new JavaSparkContext(sparkSession.sparkContext());
        newContext.setLogLevel("ERROR");
        newContext.setCheckpointDir(checkpointDir);
        Map<String, String> readOverrides = new HashMap<>();
        readOverrides.put("collection", kindName);
        ReadConfig readConfig = ReadConfig.create(newContext).withOptions(readOverrides);
        return MongoSpark.load(newContext, readConfig);
    }

}
