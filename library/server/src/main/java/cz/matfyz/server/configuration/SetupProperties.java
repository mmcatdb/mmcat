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
