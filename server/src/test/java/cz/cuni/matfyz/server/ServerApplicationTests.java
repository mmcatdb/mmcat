package cz.cuni.matfyz.server;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.server.builder.SchemaBuilder;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.LogicalModelService;
import cz.cuni.matfyz.server.service.MappingService;
import cz.cuni.matfyz.server.service.SchemaCategoryService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.transformations.algorithms.UniqueIdProvider;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author jachymb.bartik
 */
@SpringBootTest
class ServerApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerApplicationTests.class);

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SchemaCategoryService categoryService;

    @Autowired
    private WrapperService wrapperService;

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        Statistics.reset();
    }

    private String message = "";

    @Test
    void importExportTest() throws Exception {
        final int repetitions = 5;
        final int[] batches = new int[] { 1 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64 };
        //final int[] batches = new int[] { 128 };
        //final int[] batches = new int[] { 256 };
        //final int[] batches = new int[] { 48, 80, 96 };
        final int postgresqlDatabaseId = 5;
        final int[] postgresqlMappingIds = new int[] { 3, 4, 5, 6, 7, 8 };
        /*
            app_customer
            app_contact
            app_customer_contact
            app_order
            app_product
            app_order_item
            order
         */

        final int mongodbDatabaseId = 4;
        final int mongodbMappingId = 9;

        for (int i = 0; i < repetitions; i++) {
            LOGGER.info("Repetition: " + (i + 1));

            for (var batch : batches) {
                //LOGGER.info("Batch: " + batch);

                message = "";

                InstanceCategory instance = null;
                for (var mappingId : postgresqlMappingIds) {
                    instance = importMapping(instance, mappingId, postgresqlDatabaseId, batch * 1000);
                }

                exportMapping(instance, mongodbMappingId, mongodbDatabaseId);

                LOGGER.info("\n" + message);
            }
        }

        LOGGER.info("Finished");
    }

    private InstanceCategory importMapping(InstanceCategory instance, int mappingId, int databaseId, int records) throws Exception {
        var mappingWrapper = mappingService.find(mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(databaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);

        var process = new DatabaseToInstance();
        process.setLimit(records);
        process.input(mapping, instance, pullWrapper);

        var result = process.run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getInfo(Counter.PULLED_RECORDS)
            + ", " + Statistics.getInfo(Interval.MTC_ALGORIGHM)
            + ", " + Statistics.getInfo(Interval.DATABASE_TO_INSTANCE);

        Statistics.reset();

        return result.data;
    }

    private void exportMapping(InstanceCategory instance, int mappingId, int databaseId) throws Exception {
        var mappingWrapper = mappingService.find(mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(databaseId);
        AbstractDDLWrapper ddlWrapper = wrapperService.getDDLWrapper(database);
        AbstractPushWrapper pushWrapper = wrapperService.getPushWrapper(database);

        var process = new InstanceToDatabase();
        process.input(mapping, instance, ddlWrapper, pushWrapper);

        process.run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getInfo(Counter.CREATED_STATEMENTS)
            + ", " + Statistics.getInfo(Interval.CTM_ALGORIGHM)
            + ", " + Statistics.getInfo(Interval.INSTANCE_TO_DATABASE);

        Statistics.reset();
    }

    private Mapping createMapping(MappingWrapper mappingWrapper) {
        var logicalModel = logicalModelService.find(mappingWrapper.logicalModelId);
        var categoryWrapper = categoryService.find(logicalModel.categoryId);

        var mapping = new SchemaBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
        
        return mapping;
    }

}
