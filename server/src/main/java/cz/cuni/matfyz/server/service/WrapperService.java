package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractWrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPathWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPullWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPushWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBSettings;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPathWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPullWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPushWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLSettings;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class WrapperService {
    
    public AbstractPullWrapper getPullWraper(Database database) throws WrapperNotFoundException, WrapperCreationErrorException {
        try {
            switch (database.type) {
                case mongodb:
                    return getMongoDBPullWrapper(database);
                case postgresql:
                    return getPostgreSQLPullWrapper(database);
                default:
                    throw new WrapperNotFoundException("Pull wrapper for database " + database.id + " with JSON settings: " + database.settings + " not found.");
            }
        }
        catch (Exception exception) {
            throw new WrapperCreationErrorException("Pull wrapper for database " + database.id + " with JSON settings: " + database.settings + " can't be created due to following exception: " + exception.getMessage());
        }
    }

    public AbstractPathWrapper getPathWrapper(Database database) {
        switch (database.type) {
            case mongodb:
                return new MongoDBPathWrapper();
            case postgresql:
                return new PostgreSQLPathWrapper();
            default:
                return null;
        }
    }

    public AbstractPushWrapper getPushWrapper(Database database) {
        switch (database.type) {
            case mongodb:
                return new MongoDBPushWrapper();
            case postgresql:
                return new PostgreSQLPushWrapper();
            default:
                return null;
        }
    }

    private Map<Integer, MongoDBDatabaseProvider> mongoDBCache = new TreeMap<>();

    private MongoDBPullWrapper getMongoDBPullWrapper(Database database) throws Exception {
        if (!mongoDBCache.containsKey(database.id))
            mongoDBCache.put(database.id, createMongoDBProvider(database));        

        var wrapper = new MongoDBPullWrapper();
        var provider = mongoDBCache.get(database.id);
        wrapper.injectDatabaseProvider(provider);

        return wrapper;
    }

    private static MongoDBDatabaseProvider createMongoDBProvider(Database database) throws Exception {
        var mapper = new ObjectMapper();
        var settings = mapper.treeToValue(database.settings, MongoDBSettings.class);

        return new MongoDBDatabaseProvider(settings);
    }

    private Map<Integer, PostgreSQLConnectionProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLPullWrapper getPostgreSQLPullWrapper(Database database) throws Exception {
        if (!postgreSQLCache.containsKey(database.id))
            postgreSQLCache.put(database.id, createPostgreSQLProvider(database));

        var wrapper = new PostgreSQLPullWrapper();
        var provider = postgreSQLCache.get(database.id);
        wrapper.injectConnectionProvider(provider);

        return wrapper;
    }

    private static PostgreSQLConnectionProvider createPostgreSQLProvider(Database database) throws Exception {
        var mapper = new ObjectMapper();
        var settings = mapper.treeToValue(database.settings, PostgreSQLSettings.class);

        return new PostgreSQLConnectionProvider(settings);
    }

    public class WrapperCreationErrorException extends Exception {

        public WrapperCreationErrorException(String errorMessage) {
            super(errorMessage);
        }

    }

    public class WrapperNotFoundException extends Exception {

        public WrapperNotFoundException(String errorMessage) {
            super(errorMessage);
        }

    }

}
