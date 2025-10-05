package cz.matfyz.wrappermongodb;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.wrappermongodb.inference.MapMongoDocument;
import cz.matfyz.wrappermongodb.inference.MongoRecordToRawPropertyFlatMap;
import cz.matfyz.wrappermongodb.inference.RecordToHeuristicsMap;
import cz.matfyz.wrappermongodb.inference.RecordToPropertiesMap;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.spark.sql.connector.config.ReadConfig;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

public class MongoDBInferenceWrapper extends AbstractInferenceWrapper {

    private final MongoDBProvider provider;

    private String collectionName() {
        return kindName;
    }

    public MongoDBInferenceWrapper(MongoDBProvider provider, SparkSettings sparkSettings) {
        super(sparkSettings);
        this.provider = provider;
    }

    @Override public MongoDBInferenceWrapper copy() {
        return new MongoDBInferenceWrapper(this.provider, this.sparkSettings);
    }

    @Override public void buildSession() {
        // TODO the whole session management should be handled by the MongoDBProvider
        sparkSession = SparkSession.builder().master(sparkSettings.master())
            .config("spark.mongodb.input.uri", provider.settings.createSparkConnectionString())
            .config("spark.mongodb.input.database", provider.settings.database())
            .config("spark.mongodb.input.collection", kindName)
            .getOrCreate();


        // TODO
        // SparkSession spark = SparkSession.builder()
        //     .appName("MongoSparkExample")
        //     .config("spark.mongodb.read.connection.uri", "mongodb://localhost:27017/mydb.myCollection")
        //     .config("spark.mongodb.write.connection.uri", "mongodb://localhost:27017/mydb.myCollection")
        //     .getOrCreate();
    }

    @Override public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        // TODO! MAPUJE SE VZDY SCHEMA? MAPUJI SE VZDY DATA?
        if (!loadSchema && !loadData)
            return null;

        final var records = loadRecords();

        return records
            .flatMap(t -> MongoRecordToRawPropertyFlatMap.process(collectionName(), t, loadSchema, loadData))
            .mapToPair(MongoDBInferenceWrapper::creatPropertyPair);
    }

    private static Tuple2<RawProperty, Share> creatPropertyPair(RawProperty t) {
        return new Tuple2<>(t, new Share(t.getCount(), t.getFirst()));
    }

    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
        final var records = loadRecords();
        return records.map(MapMongoDocument::process);
    }

    @Override public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        final var records = loadRecords();
        return records.mapToPair(t -> new Tuple2<>(RecordSchemaDescription.ROOT_SYMBOL, MapMongoDocument.process(t)));
    }

    private JavaRDD<Row> loadRecords() {
        // Map<String, String> readOverrides = new HashMap<>();
        // readOverrides.put("collection", kindName);
        // ReadConfig readConfig = ReadConfig.create(context).withOptions(readOverrides);

        // return MongoSpark.load(context, readConfig);

        // TODO specify collection

        return sparkSession.read().format("mongodb").load().toJavaRDD();
    }

    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        final var records = loadRecords();

        return records
            .flatMap(new RecordToPropertiesMap(collectionName()))
            .mapToPair(t -> new Tuple2<>(t.getName(), t));
    }

    @Override public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        final var records = loadRecords();

        return records.flatMapToPair(new RecordToHeuristicsMap(collectionName()));
    }

    @Override public List<String> getKindNames() {
        return provider.getDatabase().listCollectionNames().into(new ArrayList<>());
    }

}
