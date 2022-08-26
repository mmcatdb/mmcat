package cz.cuni.matfyz.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public abstract class Config {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static Properties properties;

    private Config() {}

    public static String get(String key) {
        if (properties == null)
            loadProperties();

        String property = properties.getProperty(key);
        if (property == null)
            LOGGER.error("Property {} not found in configuration.", key);
        
        return property;
    }

    private static void loadProperties() {
        try {
            properties = new Properties();
            
            var url = ClassLoader.getSystemResource("application.properties");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            var configFile = new File(pathToFile);
            try (var reader = new FileReader(configFile)) {
                properties.load(reader);
            }
    
        }
        catch (URISyntaxException | IOException exception) {
            LOGGER.error(exception.toString());
        }
    }
}
