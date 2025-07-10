package cz.matfyz.abstractwrappers;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.Share;

import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public abstract class AbstractInferenceWrapper {

    protected String kindName;

    protected final SparkSettings sparkSettings;
    protected SparkSession sparkSession;
    protected JavaSparkContext context;

    protected AbstractInferenceWrapper(SparkSettings sparkSettings) {
        this.sparkSettings = sparkSettings;
    }

    public AbstractInferenceWrapper copyForKind(String kindName) {
        AbstractInferenceWrapper copy = copy();
        copy.kindName = kindName;

        return copy;
    }

    protected abstract AbstractInferenceWrapper copy();

    public void startSession() {
        buildSession();
        context = new JavaSparkContext(sparkSession.sparkContext());
        context.setCheckpointDir(sparkSettings.checkpointDir());
    }

    protected void buildSession() {
        sparkSession = SparkSession.builder()
            .master(sparkSettings.master())
            .getOrCreate();
    }

    public void stopSession() {
        sparkSession.stop();
    }

    public abstract JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData);

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema();

    public abstract JavaPairRDD<String, PropertyHeuristics> loadPropertyData();

    public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs();

    // FIXME This is already defined on AbstractPullWrapper. Unify this.
    public abstract List<String> getKindNames();

    public record SparkSettings(
        String master,
        String checkpointDir
    ) {}

}
