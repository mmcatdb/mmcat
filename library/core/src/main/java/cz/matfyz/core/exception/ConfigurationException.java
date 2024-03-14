package cz.matfyz.core.exception;

import java.io.Serializable;

/**
 * Some configuration files / keys are missing.
 * @author jachymb.bartik
 */
public class ConfigurationException extends NamedException {

    private ConfigurationException(String name, Serializable data, Exception exception) {
        super("configuration." + name, data, exception);
    }

    public static ConfigurationException keyNotFound(String key) {
        return new ConfigurationException("keyNotFound", key, null);
    }

    public static ConfigurationException notAvailable(String file, Exception exception) {
        return new ConfigurationException("notAvailable", file, exception);
    }

}
