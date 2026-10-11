package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.wrapperriak.commands.ChainRiakCommand;
import cz.matfyz.wrapperriak.commands.CreateAndActivateBucketTypeRiakCommand;

import java.util.Collection;
import java.util.List;

import com.basho.riak.client.api.commands.search.StoreIndex;
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
        // buckets are created automatically on data insertion
        return RiakCommandStatement.createEmpty();
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

        // Furthermore Riak Yokozuna Search must be enabled, otherwise index creation will fail with
        // com.basho.riak.client.core.netty.RiakResponseException: Unknown message code: 56

        var storeIndexCmd = new StoreIndex.Builder(new YokozunaIndex(newDBName)).build();
        var createBucketCmd = new CreateAndActivateBucketTypeRiakCommand.Builder(newDBName)
            .withProps(String.format("{ \"props\": { \"search_index\": \"%s\" }}", newDBName))
            .build();
        // TODO: Would not it be better to change the method interface to return List<AbstractDDLWrapper>?
        // Creating ChainRiakCommand is a suboptimal workaround.
        ChainRiakCommand command = new ChainRiakCommand(storeIndexCmd, createBucketCmd);

        String shellCommand = String.format(
            """
                curl -X PUT %s/search/index/%s
                && riak-admin bucket-type create %s '{ "props": { "search_index": "%s" }}'
                && riak-admin bucket-type activate %s""",
            provider.settings.createHttpConnectionString(), newDBName,
            newDBName, newDBName,
            newDBName);

        return new RiakCommandStatement(shellCommand, command);
    }
}
