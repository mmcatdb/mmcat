package cz.cuni.matfyz.wrapperPostgresql;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLSettings {

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public PostgreSQLSettings(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getConnectionString() {
        return createConnectionStringFromCredentials();
    }

    private String createConnectionStringFromCredentials(){
        return new StringBuilder()
            .append("jdbc:postgresql://")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append("?user=")
            .append(username)
            .append("&password=")
            .append(password)
            .toString();
    }

    // For JSON mapping
    public PostgreSQLSettings() {}

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
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