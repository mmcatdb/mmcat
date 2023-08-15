package cz.matfyz.wrappermongodb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author jachymb.bartik
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class MongoDBSettings {

    private String host;
    private String port;
    private String authenticationDatabase;
    private String database;
    private String username;
    private String password;

    public MongoDBSettings(String host, String port, String authenticationDatabase, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.authenticationDatabase = authenticationDatabase;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getConnectionString() {
        return createConnectionStringFromCredentials();
    }

    public String getDatabase() {
        return database;
    }

    private String createConnectionStringFromCredentials() {
        return new StringBuilder()
            .append("mongodb://")
            .append(username)
            .append(":")
            .append(password)
            .append("@")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append("?authSource=")
            .append(authenticationDatabase)
            .toString();
    }

    // For JSON mapping
    public MongoDBSettings() {}

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}