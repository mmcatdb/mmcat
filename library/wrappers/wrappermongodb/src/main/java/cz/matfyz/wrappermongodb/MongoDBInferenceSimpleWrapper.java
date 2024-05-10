package cz.matfyz.wrappermongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;

public class MongoDBInferenceSimpleWrapper {
    MongoClient client;
    private static MongoDBInferenceSimpleWrapper instance;

    public static MongoDBInferenceSimpleWrapper getInstance() {
        if (instance == null) {
            instance = new MongoDBInferenceSimpleWrapper();
        }

        return instance;
    }

    private MongoDBInferenceSimpleWrapper() {}

    public void buildTestSession() {
        MongoClientOptions opts = new MongoClientOptions.Builder().maxConnectionIdleTime(86400000).build();
        client = new MongoClient(new ServerAddress("localhost", 27017), opts);
        client.startSession();
    }

    public void buildSession(String uri, String port, String user, String password) {
        MongoClientOptions opts = new MongoClientOptions.Builder().maxConnectionIdleTime(86400000).build();
        client = new MongoClient(new ServerAddress(uri, Integer.parseInt(port)), opts);
        client.startSession();
    }

    public void stopSession() {
        client.close();
    }

    public List<String> listDatabases() {
        List<String> res = new ArrayList<>();
        for (String s : client.listDatabaseNames()) {
            res.add(s);
        }
        return res;
    }

    public List<String> listKinds(String dbName) {
        List<String> res = new ArrayList<>();
        MongoDatabase database = client.getDatabase(dbName);
        for (String s : database.listCollectionNames()) {
            res.add(s);
        }
        return res;
    }
}
