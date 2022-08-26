package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class DatabaseToInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseToInstance.class);

    private Mapping mapping;
    private InstanceCategory currentInstance;
    private AbstractPullWrapper pullWrapper;

    public void input(Mapping mapping, InstanceCategory currentInstance, AbstractPullWrapper pullWrapper) {
        this.mapping = mapping;
        this.currentInstance = currentInstance;
        this.pullWrapper = pullWrapper;
    }

    private Integer limit = null;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public DataResult<InstanceCategory> run() {

        Statistics.start(Interval.DATABASE_TO_INSTANCE);

        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().limit(limit).buildWithKindName(mapping.kindName()));
        }
        catch (Exception exception) {
            LOGGER.error("Pull forest failed.", exception);
            return new DataResult<>(null, "Pull forest failed.");
        }

        Statistics.set(Counter.PULLED_RECORDS, forest.size());

        InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        var transformation = new MTCAlgorithm();
        transformation.input(mapping, instance, forest);

        Statistics.start(Interval.MTC_ALGORIGHM);
        transformation.algorithm();
        Statistics.end(Interval.MTC_ALGORIGHM);
        Statistics.end(Interval.DATABASE_TO_INSTANCE);

        return new DataResult<>(instance);
    }

}
