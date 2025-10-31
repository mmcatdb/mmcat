package cz.matfyz.core.utils;

import cz.matfyz.core.exception.ConfigurationException;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private final String prefix;

    public Config() {
        this.prefix = "";
    }

    public Config(String namespace) {
        this.prefix = namespace + ".";
    }

    public static final Config GLOBAL = new Config();

    private static Properties properties;

    public String get(String key) {
        if (properties == null)
            loadProperties();

        final String prefixedKey = prefix + key;
        final String property = properties.getProperty(prefixedKey);
        if (property == null)
            throw ConfigurationException.keyNotFound(prefixedKey);

        return property;
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static void loadProperties() {
        final Properties defaultProperties = getProperties("default.properties", null);
        properties = getProperties("application.properties", defaultProperties);
    }

    private static Properties getProperties(String filename, Properties defaultProperties) {
        try {
            final Properties output = new Properties(defaultProperties);

            final var url = ClassLoader.getSystemResource(filename);
            // If the file can't be found that's fine, provided we already have default properties.
            if (url == null && defaultProperties != null)
                return defaultProperties;

            final String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            final var configFile = new File(pathToFile);

            try (
                var reader = new FileReader(configFile);
            ) {
                output.load(reader);
                return output;
            }
        }
        catch (Exception e) {
            throw ConfigurationException.notAvailable(filename, e);
        }
    }

}
