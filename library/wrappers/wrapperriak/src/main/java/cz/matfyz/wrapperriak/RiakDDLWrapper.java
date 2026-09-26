package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.wrapperriak.commands.CreateBucketTypeRiakCommand;

import java.util.Collection;
import java.util.List;

public class RiakDDLWrapper implements AbstractDDLWrapper {

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

    // TODO: Should we use Solr/Yokozuna Riak search?

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

        String shellCommand = String.format("riak-admin bucket-type create %s && riak-admin bucket-type activate %s", newDBName, newDBName);
        CreateBucketTypeRiakCommand command = new CreateBucketTypeRiakCommand.Builder(newDBName).build();

        return new RiakCommandStatement(shellCommand, command);
    }
}
