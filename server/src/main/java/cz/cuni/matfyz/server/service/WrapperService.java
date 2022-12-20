package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.wrappermongodb.MongoDBDDLWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBPathWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBPullWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBPushWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBSettings;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLDDLWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLPathWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLPullWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLPushWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLSettings;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class WrapperService {
    
    public AbstractPullWrapper getPullWraper(Database database) {
        try {
            return switch (database.type) {
                case mongodb -> getMongoDBPullWrapper(database);
                case postgresql -> getPostgreSQLPullWrapper(database);
                default -> throw new WrapperNotFoundException(wrapperNotFoundText("Pull", database));
            };
        }
        catch (Exception exception) {
            throw new WrapperCreationErrorException("Pull wrapper for database " + database.id + " with JSON settings: " + database.settings + " can't be created due to following exception: " + exception.getMessage());
        }
    }

    public AbstractPathWrapper getPathWrapper(Database database) {
        return switch (database.type) {
            case mongodb -> new MongoDBPathWrapper();
            case postgresql -> new PostgreSQLPathWrapper();
            default -> throw new WrapperNotFoundException(wrapperNotFoundText("Path", database));
        };
    }

    public AbstractDDLWrapper getDDLWrapper(Database database) {
        return switch (database.type) {
            case mongodb ->  new MongoDBDDLWrapper();
            case postgresql ->  new PostgreSQLDDLWrapper();
            default ->  throw new WrapperNotFoundException(wrapperNotFoundText("DDL", database));
        };
    }

    public AbstractPushWrapper getPushWrapper(Database database) {
        return switch (database.type) {
            case mongodb ->  new MongoDBPushWrapper();
            case postgresql ->  new PostgreSQLPushWrapper();
            default ->  throw new WrapperNotFoundException(wrapperNotFoundText("Push", database));
        };
    }

    private String wrapperNotFoundText(String name, Database database) {
        return name + "wrapper for database " + database.id + " with JSON settings: " + database.settings + " not found.";
    }

    private Map<Id, MongoDBDatabaseProvider> mongoDBCache = new TreeMap<>();

    private MongoDBPullWrapper getMongoDBPullWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(database.id))
            mongoDBCache.put(database.id, createMongoDBProvider(database));        

        var wrapper = new MongoDBPullWrapper();
        var provider = mongoDBCache.get(database.id);
        wrapper.injectDatabaseProvider(provider);

        return wrapper;
    }

    private static MongoDBDatabaseProvider createMongoDBProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        var mapper = new ObjectMapper();
        var settings = mapper.treeToValue(database.settings, MongoDBSettings.class);

        return new MongoDBDatabaseProvider(settings);
    }

    private Map<Id, PostgreSQLConnectionProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLPullWrapper getPostgreSQLPullWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(database.id))
            postgreSQLCache.put(database.id, createPostgreSQLProvider(database));

        var wrapper = new PostgreSQLPullWrapper();
        var provider = postgreSQLCache.get(database.id);
        wrapper.injectConnectionProvider(provider);

        return wrapper;
    }

    private static PostgreSQLConnectionProvider createPostgreSQLProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        var mapper = new ObjectMapper();
        var settings = mapper.treeToValue(database.settings, PostgreSQLSettings.class);

        return new PostgreSQLConnectionProvider(settings);
    }

    public class WrapperCreationErrorException extends RuntimeException {

        public WrapperCreationErrorException(String errorMessage) {
            super(errorMessage);
        }

    }

    public class WrapperNotFoundException extends RuntimeException {

        public WrapperNotFoundException(String errorMessage) {
            super(errorMessage);
        }

    }

}
