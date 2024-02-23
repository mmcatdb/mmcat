package cz.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("server")
public class ServerProperties {

    private Integer port;
    private String origin;
    private Boolean executeModels;

    public Integer port() {
        return port;
    }

    public String origin() {
        return origin;
    }

    public boolean executeModels() {
        return executeModels;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setExecuteModels(Boolean executeModels) {
        this.executeModels = executeModels;
    }

}
