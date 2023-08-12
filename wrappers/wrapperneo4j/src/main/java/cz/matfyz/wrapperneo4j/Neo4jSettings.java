package cz.matfyz.wrapperneo4j;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;

/**
 * @author jachymb.bartik
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Neo4jSettings {

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public Neo4jSettings(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getConnectionString() {
        return createConnectionStringFromCredentials();
    }

    private String createConnectionStringFromCredentials() {
        return new StringBuilder()
            .append("bolt://")
            .append(host)
            .append(":")
            .append(port)
            .toString();
    }

    public AuthToken getAuthToken() {
        return AuthTokens.basic(username, password);
    }

    // For JSON mapping
    public Neo4jSettings() {}

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}