package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.wrapperriak.commands.ChainRiakCommand;
import cz.matfyz.wrapperriak.commands.CreateAndActivateBucketTypeRiakCommand;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import com.basho.riak.client.api.commands.buckets.StoreBucketProperties;
import com.basho.riak.client.api.commands.search.StoreIndex;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.search.YokozunaIndex;

public class RiakDDLWrapper implements AbstractDDLWrapper {

    private final RiakProvider provider;

    public RiakDDLWrapper(RiakProvider provider) {
        this.provider = provider;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public void clear() {
        kindName = null;
    }

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        throw InvalidPathException.isSchemaless(Datasource.DatasourceType.riak, path);
    }

    @Override public RiakCommandStatement createDDLStatement() {
        // buckets are created automatically on data insertion,
        // but we will create the index and associate it with the bucket here

        String httpConnectionString = provider.settings.createHttpConnectionString();
        String bucketType = provider.settings.bucketType();
        String index = createIndexName(bucketType, kindName);

        String shellCommand = String.format(
            """
                curl -X PUT %s/search/index/%s \\
                && curl -X PUT %s/types/%s/buckets/%s/props \\
                    -H 'Content-Type: application/json' \\
                    -d '{ "props": { "search_index": "%s" }}'""",
            httpConnectionString, RiakUtils.encodeUriPathSegment(index, StandardCharsets.UTF_8),
            httpConnectionString, RiakUtils.encodeUriPathSegment(bucketType, StandardCharsets.UTF_8), RiakUtils.encodeUriPathSegment(kindName, StandardCharsets.UTF_8),
            index
        );

        var storeIndexCmd = new StoreIndex.Builder(new YokozunaIndex(index)).build();
        var storeBucketPropsCmd = new StoreBucketProperties.Builder(new Namespace(bucketType, kindName))
            .withSearchIndex(index)
            .build();
        var command = new ChainRiakCommand(storeIndexCmd, storeBucketPropsCmd);

        return new RiakCommandStatement(shellCommand, command);
    }

    /**
     * Returns a deterministic and unique index name for a bucket type-bucket pair.
     * @param bucketType name of the bucket type
     * @param bucket name of the bucket
     * @return name of the index
     */
    private String createIndexName(String bucketType, String bucket) {
        return String.format("%s_%s_%s_%s", bucketType.length(), bucket.length(), bucketType, bucket);
    }

    @Override public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        // TODO: Should we delete all key-value pairs for each bucket present in the script?
        // Listing all pairs within a bucket and deleting them one by one can be really expansive.
        // Neo4jDDLWrapper does not delete the data either and just drops the constraints.

        return List.of();
    }

    @Override public RiakCommandStatement createCreationStatement(String newDBName, String owner) {
        // Riak does not have the concept of databases or user roles as other database systems do,
        // but bucket types can be used as an additional namespace.
        // https://docs.riak.com/riak/kv/latest/using/reference/bucket-types/index.html#buckets-as-namespaces

        // However, the Riak Java Client does not support creating new bucket types,
        // so we must use the CLI interface instead, which will fail if riak-admin is not installed.

        String shellCommand = String.format("""
            riak-admin bucket-type create %s \\
            && riak-admin bucket-type activate %s""",
            newDBName, newDBName);
        CreateAndActivateBucketTypeRiakCommand command = new CreateAndActivateBucketTypeRiakCommand.Builder(newDBName).build();

        return new RiakCommandStatement(shellCommand, command);
    }
}
