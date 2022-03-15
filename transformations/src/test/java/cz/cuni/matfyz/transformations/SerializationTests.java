package cz.cuni.matfyz.transformations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jachymb.bartik
 */
public class SerializationTests
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationTests.class);

    @Test
    public void createExampleSchemaCategory()
    {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();

        //LOGGER.info(schema.toJSON().toString());
    }

    @Test
    public void createBasicAccessPath()
    {
        var data = new TestData();
        var path = data.path_order();

        LOGGER.info(path.toJSON().toString());
    }
}
