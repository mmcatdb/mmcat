package cz.matfyz.wrappermongodb;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.wrappermongodb.inference.MapMongoDBDocument;
import cz.matfyz.wrappermongodb.inference.RecordToHeuristicsMap;
import cz.matfyz.wrappermongodb.inference.RecordToPropertiesMap;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import scala.Tuple2;

public class MongoDBInferenceWrapper extends AbstractInferenceWrapper {

    private final MongoDBProvider provider;

    public MongoDBInferenceWrapper(MongoDBProvider provider, String kindName, SparkSettings sparkSettings) {
        super(kindName, sparkSettings);
        this.provider = provider;
    }

    private static final String READ_PREFIX = "spark.mongodb.read.";

    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
        return loadRecords().map(MapMongoDBDocument::process);
    }

    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        return loadRecords()
            .flatMap(new RecordToPropertiesMap(kindName))
            .mapToPair(t -> new Tuple2<>(t.getName(), t));
    }

    @Override public JavaRDD<PropertyHeuristics> loadPropertyData() {
        return loadRecords().flatMap(new RecordToHeuristicsMap(kindName));
    }

    public JavaRDD<Row> loadRecords() {
        return getSession().read().format("mongodb")
            // .option(READ_PREFIX + "connection.uri", provider.settings.createSparkConnectionString())
            .option(READ_PREFIX + "connection.uri", provider.settings.createConnectionString())
            // .option(READ_PREFIX + "database", provider.settings.database())
            .option(READ_PREFIX + "collection", kindName)
            .load()
            .toJavaRDD();
    }

}
