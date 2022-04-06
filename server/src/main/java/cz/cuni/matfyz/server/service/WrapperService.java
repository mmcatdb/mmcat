package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Database;
import cz.cuni.matfyz.abstractWrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.server.Config;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPathWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPullWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPathWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPullWrapper;

import org.springframework.stereotype.Service;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class WrapperService {
    
    public AbstractPullWrapper getPullWraper(Database database) {
        switch (database.type) {
            case mongodb:
                return getMongoDBPullWrapper();
            case postgresql:
                return getPostgreSQLPullWrapper();
            default:
                return null;
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

    private MongoDBDatabaseProvider mongoDBDatabaseProvider;
    private MongoDBPullWrapper getMongoDBPullWrapper() {
        if (mongoDBDatabaseProvider == null) {
            mongoDBDatabaseProvider = new MongoDBDatabaseProvider(
                Config.get("data.mongodb.host"),
                Config.get("data.mongodb.port"),
                Config.get("data.mongodb.database"),
                Config.get("data.mongodb.authenticationDatabase"),
                Config.get("data.mongodb.username"),
                Config.get("data.mongodb.password")
            );
        }

        var wrapper = new MongoDBPullWrapper();
        wrapper.injectDatabaseProvider(mongoDBDatabaseProvider);

        return wrapper;
    }

    private PostgreSQLConnectionProvider postgreSQLConnectionProvider;
    private PostgreSQLPullWrapper getPostgreSQLPullWrapper() {
        if (postgreSQLConnectionProvider == null) {
            postgreSQLConnectionProvider = new PostgreSQLConnectionProvider(
                Config.get("data.postgresql.host"),
                Config.get("data.postgresql.port"),
                Config.get("data.postgresql.database"),
                Config.get("data.postgresql.username"),
                Config.get("data.postgresql.password")
            );
        }

        var wrapper = new PostgreSQLPullWrapper();
        wrapper.injectConnectionProvider(postgreSQLConnectionProvider);

        return wrapper;
    }

}
