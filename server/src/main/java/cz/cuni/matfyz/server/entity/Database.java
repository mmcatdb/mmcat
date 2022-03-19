package cz.cuni.matfyz.server.entity;

import org.json.JSONObject;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.server.Config;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPullWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class Database extends Entity {

    public final String type;
    public final String label;

    private Database(Integer id, String type, String label) {
        super(id);
        this.type = type;
        this.label = label;
    }

    // TODO
    public static Database fromJSON(Integer id, String jsonValue) {
        try
        {
            var json = new JSONObject(jsonValue);

            String type = json.getString("type");
            String label = json.getString("label");

            return new Database(id, type, label);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return null;
    }

    public AbstractPullWrapper getPullWraper() {
        if (type != "mongodb")
            return null;

        return getMongodbPullWrapper();
    }

    private AbstractPullWrapper getMongodbPullWrapper() {
        var databaseProvider = new MongoDBDatabaseProvider(
            Config.get("data.mongodb.host"),
            Config.get("data.mongodb.port"),
            Config.get("data.mongodb.database"),
            Config.get("data.mongodb.username"),
            Config.get("data.mongodb.password")
        );

        databaseProvider.buildDatabase();
        var wrapper = new MongoDBPullWrapper();
        wrapper.injectDatabaseProvider(databaseProvider);

        return wrapper;
    }
    
}