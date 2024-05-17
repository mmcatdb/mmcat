package cz.matfyz.abstractwrappers;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.Share;

import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

public abstract class AbstractInferenceWrapper {

    public String kindName;

    public abstract void buildSession();

    public abstract void stopSession();

    public abstract JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData);

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema();

    public abstract JavaPairRDD<String, PropertyHeuristics> loadPropertyData();

    public abstract void initiateContext();

    public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs();

    public abstract AbstractInferenceWrapper copy();

    /**
     * This is a default implementation for files (e.g., json, csv) that have only one "kind". It needs to be overriden for proper databases.
     */
    public List<String> getKindNames() {
        return singleCollection;
    }

    private final List<String> singleCollection = List.of("single-collection");

    public record SparkSettings(
        String master,
        String checkpointDir
    ) {}

}
