package cz.matfyz.transformations.processes;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.Statistics.Counter;
import cz.matfyz.core.utils.Statistics.Interval;
import cz.matfyz.transformations.algorithms.MTCAlgorithm;

public class DatabaseToInstance {

    private Mapping mapping;
    private InstanceCategory currentInstance;
    private AbstractPullWrapper pullWrapper;
    private KindNameQuery query = null;

    public DatabaseToInstance input(Mapping mapping, InstanceCategory currentInstance, AbstractPullWrapper pullWrapper) {
        this.mapping = mapping;
        this.currentInstance = currentInstance;
        this.pullWrapper = pullWrapper;

        return this;
    }

    public DatabaseToInstance input(Mapping mapping, InstanceCategory currentInstance, AbstractPullWrapper pullWrapper, KindNameQuery query) {
        this.query = query;

        return this.input(mapping, currentInstance, pullWrapper);
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

        final var finalQuery = query != null ? query : new KindNameQuery(mapping.kindName());

        final ForestOfRecords forest = pullWrapper.pullForest(mapping.accessPath(), finalQuery);

        Statistics.set(Counter.PULLED_RECORDS, forest.size());

        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceBuilder(mapping.category()).build();

        final var tform = new MTCAlgorithm();
        tform.input(mapping, instance, forest);

        Statistics.start(Interval.MTC_ALGORIGHM);
        tform.algorithm();
        Statistics.end(Interval.MTC_ALGORIGHM);
        Statistics.end(Interval.DATABASE_TO_INSTANCE);

        return instance;
    }

}
