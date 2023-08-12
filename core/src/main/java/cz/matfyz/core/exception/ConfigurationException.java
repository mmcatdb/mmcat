package cz.matfyz.core.exception;

import java.io.Serializable;

/**
 * A wrapper class for all exceptions other than ours (i.e., other than those that extend the NamedException).
 * @author jachymb.bartik
 */
public class ConfigurationException extends NamedException {
    
    private ConfigurationException(String name, Serializable data, Exception exception) {
        super("configuration." + name, data, exception);
    }

    public static ConfigurationException keyNotFound(String key) {
        return new ConfigurationException("keyNotFound", key, null);
    }

    public static ConfigurationException notAvailable(Exception exception) {
        return new ConfigurationException("notAvailable", null, exception);
    }

}
