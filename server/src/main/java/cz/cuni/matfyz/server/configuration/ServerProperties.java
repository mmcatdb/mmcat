package cz.cuni.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("server")
public class ServerProperties {

    public Integer port() {
        return port;
    }

    public String origin() {
        return origin;
    }

    public boolean executeModels() {
        return executeModels;
    }

    private Integer port;
    private String origin;
    private boolean executeModels = true;

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setExecuteModels(boolean executeModels) {
        this.executeModels = executeModels;
    }

}
