package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.collector.AbstractWrapper.ConnectionData;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.wrappermongodb.collector.MongoWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.mongodb;
    }

    private final MongoDBProvider provider;

    public MongoDBControlWrapper(MongoDBProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statements) {
        for (final var statement : statements) {
            try {
                if (statement instanceof MongoDBCommandStatement commandStatement)
                    provider.getDatabase().runCommand(commandStatement.getCommand());
            }
            catch (MongoException e) {
                throw new ExecuteException(e, statements);
            }
        }
    }

    @Override public void execute(Path path) {
        try {
            // Unfortunately, there isn't a way how to run the commands by the driver. So we have to use the shell. Make sure the mongosh is installed.
            String[] command = { "mongosh", provider.settings.createConnectionString(), path.toString() };

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            process.waitFor();

            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));
        }
        catch (InterruptedException e) {
            throw new ExecuteException(e, path);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    @Override public MongoDBDDLWrapper getDDLWrapper() {
        return new MongoDBDDLWrapper();
    }

    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    @Override public MongoDBDMLWrapper getDMLWrapper() {
        return new MongoDBDMLWrapper();
    }

    @Override public MongoDBPullWrapper getPullWrapper() {
        return new MongoDBPullWrapper(provider);
    }

    @Override public MongoDBPathWrapper getPathWrapper() {
        return new MongoDBPathWrapper();
    }

    @Override public MongoDBQueryWrapper getQueryWrapper() {
        return new MongoDBQueryWrapper();
    }

    @Override public MongoDBInferenceWrapper getInferenceWrapper() {
        return new MongoDBInferenceWrapper(provider, getSparkSettings());
    }

    @Override public MongoWrapper getCollectorWrapper() {
        return new MongoWrapper(new ConnectionData(
            provider.settings.host(),
            Integer.parseInt(provider.settings.port()),
            getType().name(),
            provider.settings.database(),
            provider.settings.username(),
            provider.settings.password()));
    }
}
