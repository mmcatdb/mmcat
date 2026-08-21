package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.*;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiakControlWrapper extends BaseControlWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiakControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.riak;
    }

    private final RiakProvider provider;
    private final String datasourceIdentifier;

    public RiakControlWrapper(RiakProvider provider, String datasourceIdentifier) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
        this.datasourceIdentifier = datasourceIdentifier;
    }

    @Override public void execute(Collection<AbstractStatement> statements) throws ExecuteException {
        for (final var statement : statements) {
            try {
                if (statement instanceof RiakCommandStatement commandStatement) {
                    LOGGER.info("Execute Riak statement:\n{}", commandStatement.getContent());
                    provider.getClient().execute(commandStatement.getCommand());
                }
            }
            catch (ExecutionException | InterruptedException e) {
                throw new ExecuteException(e, statements);
            }
        }
    }

    @Override public void execute(Path path) {
        try {
            // Riak does not have a standard scripting language like SQL or Cypher.
            // An Erlang script can be simply run using the `riak escript` command,
            // but it still requires the Riak Erlang Client to be installed.
            // Furthermore, users are likely more familiar with Shell than Erlang.
            final String[] command = new String[] { "sh", path.toString(), provider.settings.createHttpConnectionString(), provider.settings.bucketType() };

            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(command);
            process.waitFor();

            try (BufferedReader bufferReader = process.inputReader()) {
                LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));
            }
        }
        catch (InterruptedException | IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    @Override public AbstractDDLWrapper getDDLWrapper() {
        return new RiakDDLWrapper();
    }

    @Override public AbstractICWrapper getICWrapper() {
        return null;
    }

    @Override public RiakDMLWrapper getDMLWrapper() {
        return new RiakDMLWrapper(provider);
    }

    @Override public AbstractPullWrapper getPullWrapper() {
        return null;
    }

    @Override public AbstractPathWrapper getPathWrapper() {
        return null;
    }

    @Override public AbstractQueryWrapper getQueryWrapper() {
        return null;
    }

    @Override public AbstractInferenceWrapper getInferenceWrapper(String kindName) {
        return null;
    }

    @Override public AbstractCollectorWrapper getCollectorWrapper() {
        return null;
    }
}
