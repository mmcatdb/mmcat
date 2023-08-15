package cz.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("setup")
public class SetupProperties {

    private Boolean isInDocker;
    private String postgresqlPort;
    private String mongodbPort;
    private String neo4jPort;
    private String database;
    private String username;
    private String password;

    public Boolean isInDocker() {
        return isInDocker;
    }
    
    public void setIsInDocker(Boolean isInDocker) {
        this.isInDocker = isInDocker;
    }

    public String postgresqlPort() {
        return postgresqlPort;
    }

    public void setPostgresqlPort(String postgresqlPort) {
        this.postgresqlPort = postgresqlPort;
    }

    public String mongodbPort() {
        return mongodbPort;
    }
    
    public void setMongodbPort(String mongodbPort) {
        this.mongodbPort = mongodbPort;
    }
    
    public String neo4jPort() {
        return neo4jPort;
    }
    
    public void setNeo4jPort(String neo4jPort) {
        this.neo4jPort = neo4jPort;
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

    public String database() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}