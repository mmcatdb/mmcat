package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameFilterQuery;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.querying.ExplainResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.basho.riak.client.api.commands.buckets.ListBuckets;
import com.basho.riak.client.api.commands.search.Search;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RiakPullWrapper implements AbstractPullWrapper {

    private final RiakProvider provider;

    public RiakPullWrapper(RiakProvider provider) {
        this.provider = provider;
    }

    private Search getRiakCommand(QueryContent query) {
        if (query instanceof final KindNameQuery kindNameQuery)
            return fromKindName(kindNameQuery);

        if (query instanceof final KindNameFilterQuery kindNameFilterQuery)
            return fromKindNameFilter(kindNameFilterQuery);

        if (query instanceof final RiakQuery riakQuery)
            return fromRiakQuery(riakQuery);

        throw PullForestException.invalidQuery(this, query);
    }
    
    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        try {
            final var forest = new ForestOfRecords();
            final var command = getRiakCommand(query);
            final var response = provider.getClient().execute(command);

            for (var result : response.getAllResults()) {
                var rootRecord = new RootRecord();
                addKeysToRecord(rootRecord, path, result);
                forest.addRecord(rootRecord);
            }

            return forest;
        }
        catch (ExecutionException | InterruptedException e) {
            throw PullForestException.inner(e);
        }
    }

    private void addKeysToRecord(ComplexRecord record, ComplexProperty path, Map<String, List<String>> result) {
        result.forEach((key, values) -> {
            // internal yokozuna field
            if (key.startsWith("_yz_"))
                return;
            if (values == null || values.isEmpty())
                return;

            final var property = path.findSubpathByName(key);
            if (property == null)
                return;

            // All values are currently stored as strings in mmcat
            // and values indexed with suffix "_s" (in the default schema) are not multi-valued
            var value = values.getFirst();

            if (property.name() instanceof DynamicName)
                record.addDynamicRecordWithValue(property, key, value);
            else
                record.addSimpleRecord(property.signature(), value);
        });
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
