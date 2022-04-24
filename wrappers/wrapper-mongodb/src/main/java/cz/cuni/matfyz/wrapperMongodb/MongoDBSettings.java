package cz.cuni.matfyz.wrapperMongodb;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBSettings {

    private String host;
    private String port;
    private String database;
    private String authenticationDatabase;
    private String username;
    private String password;
    private String connectionString = null;

    public MongoDBSettings(String host, String port, String database, String authenticationDatabase, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.authenticationDatabase = authenticationDatabase;
        this.username = username;
        this.password = password;
    }

    public MongoDBSettings(String database, String connectionString) {
        this.database = database;
        this.connectionString = connectionString;
    }

    public String getConnectionString() {
        return connectionString != null ? connectionString : createConnectionStringFromCredentials();
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

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

}