package cz.matfyz.abstractwrappers;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PropertyHeuristics;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractInferenceWrapper {

    protected final String kindName;
    private final SparkSettings sparkSettings;

    protected AbstractInferenceWrapper(String kindName, SparkSettings sparkSettings) {
        this.kindName = kindName;
        this.sparkSettings = sparkSettings;
    }

    // SparkSession is the main entry point for Spark functionality, SparkContext is the low-level object that does all the work.
    // There can be at most one SparkContext per JVM (and thus one SparkSession). We can "create" it multiple times, but it will always return the same instance.
    // So there is no point in building the session multiple times.
    // Also, there is no point in stopping the session - we would need to create a new one or something (idk, there's no explicit `restart` method).

    private @Nullable SparkSession sparkSession;

    protected SparkSession getSession() {
        if (sparkSession == null)
            sparkSession = buildSession();

        return sparkSession;
    }

    private SparkSession buildSession() {
        final var output = SparkSession.builder()
            .master(sparkSettings.master())
            .getOrCreate();

        output.sparkContext().setCheckpointDir(sparkSettings.checkpointDir());

        return output;
    }

    // JavaSparkContext doesn't have to be singleton because it's just a wrapper around SparkContext, which is singleton.

    private @Nullable JavaSparkContext context;

    protected JavaSparkContext getContext() {
        if (context == null)
            context = new JavaSparkContext(getSession().sparkContext());

        return context;
    }

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema();

    public abstract JavaRDD<PropertyHeuristics> loadPropertyData();

    public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

    public record SparkSettings(
        String master,
        String checkpointDir
    ) {}

}
