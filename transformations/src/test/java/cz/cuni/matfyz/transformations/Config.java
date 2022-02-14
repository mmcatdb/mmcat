package cz.cuni.matfyz.transformations;

import java.util.Properties;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
abstract class Config
{
    private static Properties properties;
    private static Logger logger = LoggerFactory.getLogger(Config.class);

    private Config() {}

    public static String get(String key)
    {
        if (properties == null)
            loadProperties();

        return properties.getProperty(key);
    }

    private static void loadProperties()
    {
        try
        {
            properties = new Properties();
            
            var url = ClassLoader.getSystemResource("config.properties");
            String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
            var configFile = new File(fileName);
            var reader = new FileReader(configFile);
    
            properties.load(reader);
        }
        catch (URISyntaxException exception)
        {
            logger.error(exception.toString());
        }
        catch (FileNotFoundException exception)
        {
            logger.error(exception.toString());
        }
        catch (IOException exception)
        {
            logger.error(exception.toString());
        }
    }
}
