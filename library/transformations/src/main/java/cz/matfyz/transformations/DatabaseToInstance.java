package cz.matfyz.transformations;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.utils.Statistics;
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
        Statistics.start(RUN_INTERVAL);

        final var finalQuery = query != null ? query : new KindNameQuery(mapping.kindName());

        final ForestOfRecords forest = pullWrapper.pullForest(mapping.accessPath(), finalQuery);

        Statistics.set(RECORDS_COUNTER, forest.size());

        Statistics.start(MTC_INTERVAL);
        MTCAlgorithm.run(mapping, currentInstance, forest);
        Statistics.end(MTC_INTERVAL);

        Statistics.end(RUN_INTERVAL);

        return currentInstance;
    }

    public static final String RUN_INTERVAL = "database-to-instance";
    public static final String MTC_INTERVAL = "mtc-algorithm";
    public static final String RECORDS_COUNTER = "pulled-records";

}
