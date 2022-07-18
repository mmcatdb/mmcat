package cz.cuni.matfyz.transformations.MTCTests;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.transformations.algorithms.UniqueIdProvider;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDDLWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPushWrapper;

import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class MTCAlgorithmTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithmTests.class);

    private TestData data;

    private final AbstractPullWrapper pullWrapper = new DummyPullWrapper();
    private final AbstractDDLWrapper ddlWrapper = new MongoDBDDLWrapper();
    private final AbstractPushWrapper pushWrapper = new MongoDBPushWrapper();

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
        var instance = result.data;

        LOGGER.info(instance.toString());
	}

}
