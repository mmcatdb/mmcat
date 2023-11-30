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
    private String username;
    private String password;

    private String basicDatabase;
    private String queryEvolutionDatabase;

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

    public String basicDatabase() {
        return basicDatabase;
    }

    public void setBasicDatabase(String basicDatabase) {
        this.basicDatabase = basicDatabase;
    }

    public String queryEvolutionDatabase() {
        return queryEvolutionDatabase;
    }

    public void setQueryEvolutionDatabase(String queryEvolutionDatabase) {
        this.queryEvolutionDatabase = queryEvolutionDatabase;
    }

}