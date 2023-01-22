package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.statements.AbstractStatement;

import java.util.Collection;

import com.mongodb.MongoException;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBControlWrapper implements AbstractControlWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBControlWrapper.class);

    private DatabaseProvider databaseProvider;
    
    public MongoDBControlWrapper(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    @Override
    public boolean execute(Collection<AbstractStatement> statements) {
        for (final var statement : statements) {
            try {
                if (statement instanceof MongoDBCommandStatement commandStatement)
                    databaseProvider.getDatabase().runCommand(commandStatement.getCommand());
            }
            catch (MongoException exception) {
                LOGGER.error("MongoDB exception: ", exception);
            }
        }
        
        return true;
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
}