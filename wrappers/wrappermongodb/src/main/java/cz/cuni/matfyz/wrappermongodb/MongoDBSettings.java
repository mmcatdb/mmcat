package cz.cuni.matfyz.wrappermongodb;

/**
 * @author jachymb.bartik
 */
public class MongoDBSettings {

    private String host;
    private String port;
    private String database;
    private String authenticationDatabase;
    private String username;
    private String password;

    public MongoDBSettings(String host, String port, String database, String authenticationDatabase, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.authenticationDatabase = authenticationDatabase;
        this.username = username;
        this.password = password;
    }

    public String getConnectionString() {
        return createConnectionStringFromCredentials();
    }

    public String getDatabase() {
        return database;
    }

    // It should be possible something like this:
    // mongosh mongodb://<username>:<password>@<host>:<port>/<database>?authSource=<authenticationDatabase>
    // Meaning that it would be possible to specify the database here without the need for the getDatabase() function.
    // However, the java mongo client still needs the database so there's probably no way around it.

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

}