package cz.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("database")
public class DatabaseProperties {

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public String host() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String port() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String database() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String username() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String password() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
