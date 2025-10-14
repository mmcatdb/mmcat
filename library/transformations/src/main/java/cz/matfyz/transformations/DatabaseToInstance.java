package cz.matfyz.transformations;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.Statistics.Counter;
import cz.matfyz.core.utils.Statistics.Interval;
import cz.matfyz.transformations.algorithms.MTCAlgorithm;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DatabaseToInstance {

    private Mapping mapping;
    private InstanceCategory currentInstance;
    private AbstractPullWrapper pullWrapper;
    private @Nullable KindNameQuery query = null;

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

        Statistics.start(Interval.MTC_ALGORITHM);
        MTCAlgorithm.run(mapping, currentInstance, forest);
        Statistics.end(Interval.MTC_ALGORITHM);

        Statistics.end(Interval.DATABASE_TO_INSTANCE);

        return currentInstance;
    }

}
