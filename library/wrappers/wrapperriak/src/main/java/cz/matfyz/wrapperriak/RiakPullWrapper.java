package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.ExplainResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.basho.riak.client.api.commands.buckets.ListBuckets;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RiakPullWrapper implements AbstractPullWrapper {

    private final RiakProvider provider;

    public RiakPullWrapper(RiakProvider provider) {
        this.provider = provider;
    }

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        return null;
    }

    @Override public ExplainResult explainQuery(QueryStatement query) {
        return AbstractPullWrapper.super.explainQuery(query);
    }

    @Override public QueryResult executeQuery(QueryStatement query) {
        return null;
    }

    @Override public List<String> getKindNames() {
        try {
            var lb = new ListBuckets.Builder(provider.settings.bucketType()).build();
            var resp = provider.getClient().execute(lb);
            var output = new ArrayList<String>();

            for (var rec : resp)
                output.add(rec.getBucketNameAsString());

            return output;
        } catch (ExecutionException | InterruptedException e) {
            throw PullForestException.inner(e);
        }
    }

    @Override public DataResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filter) {
        return null;
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        return List.of();
    }

    @Override public DataResponse getQueryResult(QueryContent query) {
        return null;
    }
}
