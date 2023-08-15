package cz.matfyz.core.utils;

import cz.matfyz.core.exception.ConfigurationException;

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
        if (property == null)
            throw ConfigurationException.keyNotFound(key);

        return property;
    }

    public static boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static void loadProperties() {
        final Properties defaultProperties = getProperties("default.properties", null);
        properties = getProperties("application.properties", defaultProperties);
    }

    private static Properties getProperties(String fileName, Properties defaultProperties) {
        try {
            final Properties output = new Properties(defaultProperties);
            
            final var url = ClassLoader.getSystemResource(fileName);
            // If the file can't be found that's fine, provided we already have default properties.
            if (url == null && defaultProperties != null)
                return defaultProperties;

            final String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            final var configFile = new File(pathToFile);

            try (
                final var reader = new FileReader(configFile);
            ) {
                output.load(reader);
                return output;
            }
        }
        catch (Exception e) {
            throw ConfigurationException.notAvailable(e);
        }
    }

}
