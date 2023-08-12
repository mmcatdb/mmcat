package cz.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("postgresql")
public class DatabaseProperties {

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }
    
    public String database() {
        return database;
    }
    
    public String username() {
        return username;
    }
    
    public String password() {
        return password;
    }

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    
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