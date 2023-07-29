package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.abstractwrappers.exception.ExecuteException;

import java.util.Collection;

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

    private DatabaseProvider databaseProvider;
    
    public MongoDBControlWrapper(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void execute(Collection<AbstractStatement> statements) {
        for (final var statement : statements) {
            try {
                if (statement instanceof MongoDBCommandStatement commandStatement)
                    databaseProvider.getDatabase().runCommand(commandStatement.getCommand());
            }
            catch (MongoException e) {
                throw new ExecuteException(e, statements);
            }
        }
    }

    @Override
    public AbstractDDLWrapper getDDLWrapper() {
        return new MongoDBDDLWrapper();
    }

    @Override
    public AbstractICWrapper getICWrapper() {
        return new MongoDBICWrapper();
    }

    @Override
    public AbstractDMLWrapper getDMLWrapper() {
        return new MongoDBDMLWrapper();
    }

    @Override
    public AbstractPullWrapper getPullWrapper() {
        return new MongoDBPullWrapper(databaseProvider);
    }

    @Override
    public AbstractPathWrapper getPathWrapper() {
        return new MongoDBPathWrapper();
    }

    @Override
    public AbstractQueryWrapper getQueryWrapper() {
        return new MongoDBQueryWrapper();
    }

}