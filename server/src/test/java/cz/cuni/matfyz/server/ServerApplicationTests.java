package cz.cuni.matfyz.server;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.server.builder.MappingBuilder;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.LogicalModelService;
import cz.cuni.matfyz.server.service.MappingService;
import cz.cuni.matfyz.server.service.SchemaCategoryService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;

import java.util.List;

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
        //final int batchMultiplier = 1000;
        final int batchMultiplier = 10;
        final int repetitions = 5;
        final int[] batches = new int[] { 1 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64 };
        //final int[] batches = new int[] { 128 };
        //final int[] batches = new int[] { 256 };
        //final int[] batches = new int[] { 48, 80, 96 };
        final Id postgresqlDatabaseId = new Id("5");
        final List<Id> postgresqlMappingIds = List.of(3, 4, 5, 6, 7, 8).stream().map(number -> new Id("" + number)).toList();
        /*
            app_customer
            app_contact
            app_customer_contact
            app_order
            app_product
            app_order_item
            order
         */

        final Id mongodbDatabaseId = new Id("4");
        final Id mongodbMappingId = new Id("9");

        for (int i = 0; i < repetitions; i++) {
            LOGGER.info("Repetition: " + (i + 1));

            for (var batch : batches) {
                //LOGGER.info("Batch: " + batch);

                message = "";

                InstanceCategory instance = null;
                for (var mappingId : postgresqlMappingIds) {
                    instance = importMapping(instance, mappingId, postgresqlDatabaseId, batch * batchMultiplier);
                }

                exportMapping(instance, mongodbMappingId, mongodbDatabaseId);

                LOGGER.info("\n" + message);
            }
        }

        LOGGER.info("Finished");
    }

    private InstanceCategory importMapping(InstanceCategory instance, Id mappingId, Id databaseId, int records) throws Exception {
        var mappingWrapper = mappingService.find(mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(databaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();

        var newInstance = new DatabaseToInstance()
            .input(mapping, instance, pullWrapper, PullQuery.withLimit(records).fromKindName(mapping.kindName()))
            .run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getInfo(Counter.PULLED_RECORDS)
            + ", " + Statistics.getInfo(Interval.MTC_ALGORIGHM)
            + ", " + Statistics.getInfo(Interval.DATABASE_TO_INSTANCE);

        Statistics.reset();

        return newInstance;
    }

    private void exportMapping(InstanceCategory instance, Id mappingId, Id databaseId) throws Exception {
        var mappingWrapper = mappingService.find(mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(databaseId);
        final var control = wrapperService.getControlWrapper(database);
        AbstractDDLWrapper ddlWrapper = control.getDDLWrapper();
        AbstractDMLWrapper dmlWrapper = control.getDMLWrapper();
        AbstractICWrapper icWrapper = control.getICWrapper();

        var process = new InstanceToDatabase();
        process.input(mapping, List.of(mapping), instance, ddlWrapper, dmlWrapper, icWrapper);
        process.run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getInfo(Counter.CREATED_STATEMENTS)
            + ", " + Statistics.getInfo(Interval.CTM_ALGORIGHM)
            + ", " + Statistics.getInfo(Interval.INSTANCE_TO_DATABASE);

        Statistics.reset();
    }

    private Mapping createMapping(MappingWrapper mappingWrapper) {
        var logicalModel = logicalModelService.find(mappingWrapper.logicalModelId());
        var categoryWrapper = categoryService.find(logicalModel.categoryId);

        var mapping = new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
        
        return mapping;
    }

}
