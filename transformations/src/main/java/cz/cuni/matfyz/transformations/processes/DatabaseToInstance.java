package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.exception.NamedException;
import cz.cuni.matfyz.core.exception.OtherException;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;

/**
 * @author jachym.bartik
 */
public class DatabaseToInstance {

    private Mapping mapping;
    private InstanceCategory currentInstance;
    private AbstractPullWrapper pullWrapper;

    public DatabaseToInstance input(Mapping mapping, InstanceCategory currentInstance, AbstractPullWrapper pullWrapper) {
        this.mapping = mapping;
        this.currentInstance = currentInstance;
        this.pullWrapper = pullWrapper;

        return this;
    }

    private Integer limit = null;

    public DatabaseToInstance setLimit(Integer limit) {
        this.limit = limit;

        return this;
    }

    public InstanceCategory run() {
        try {
            return innerRun();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private InstanceCategory innerRun() throws Exception {
        Statistics.start(Interval.DATABASE_TO_INSTANCE);

        ForestOfRecords forest;
        forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().limit(limit).buildWithKindName(mapping.kindName()));

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

        return instance;
    }
}
