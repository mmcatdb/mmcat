package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBControlWrapper.class);

    static final String TYPE = "mongodb";

    private MongoDBProvider provider;
    
    public MongoDBControlWrapper(MongoDBProvider provider) {
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
            // Unfortunatelly, there isn't a way how to run the commands by the driver. So we have to use the shell. Make sure the mongosh is installed.
            String[] command = { "mongosh", provider.settings.getConnectionString(), path.toString() };

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

    @Override public MongoDBICWrapper getICWrapper() {
        return new MongoDBICWrapper();
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

}