package cz.cuni.matfyz.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@ConfigurationProperties("server")
public class ServerProperties {

    public String port() {
        return port;
    }

    public String origin() {
        return origin;
    }

    private String port;
    private String origin;

    public void setPort(String port) {
        this.port = port;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
