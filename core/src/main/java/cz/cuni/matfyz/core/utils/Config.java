package cz.cuni.matfyz.core.utils;

import cz.cuni.matfyz.core.exception.ConfigurationException;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author jachymb.bartik
 */
public abstract class Config {
    
    private static Properties properties;

    private Config() {}

    public static String get(String key) {
        if (properties == null)
            loadProperties();

        String property = properties.getProperty(key);
        if (property == null) {
            throw ConfigurationException.keyNotFound(key);
        }

        return property;
    }

    private static void loadProperties() {
        try {
            properties = new Properties();
            
            var url = ClassLoader.getSystemResource("application.properties");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            var configFile = new File(pathToFile);

            try (
                var reader = new FileReader(configFile);
            ) {
                properties.load(reader);
            }
        }
        catch (Exception e) {
            throw ConfigurationException.notAvailable(e);
        }
    }
}
