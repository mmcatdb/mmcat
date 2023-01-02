package cz.cuni.matfyz.transformations.mtctests;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.wrapperdummy.DummyPullWrapper;

import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MTCAlgorithmTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithmTests.class);

    private TestData data;

    private final AbstractPullWrapper pullWrapper = new DummyPullWrapper();

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
    }

    @Test
    public void basicTest() throws URISyntaxException {
        var schema = data.createInitialSchemaCategory();

        var databaseToInstance = new DatabaseToInstance();
        var mapping = data.createOrderTableMapping(schema);
        databaseToInstance.input(mapping, null, pullWrapper);
        var result = databaseToInstance.run();
        var category = result.data;

        LOGGER.info(category.toString());
    }

}
