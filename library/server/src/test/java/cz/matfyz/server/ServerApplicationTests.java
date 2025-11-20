package cz.matfyz.server;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.MappingEntity;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.server.service.WrapperService;
import cz.matfyz.transformations.DatabaseToInstance;
import cz.matfyz.transformations.InstanceToDatabase;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
class ServerApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerApplicationTests.class);

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    @Autowired
    private WrapperService wrapperService;

    @BeforeEach
    public void setup() {
        UniqueIdGenerator.makeDeterministic();
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
        final Id postgresqlDatasourceId = new Id("5");
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

        final Id mongodbDatasourceId = new Id("4");
        final Id mongodbMappingId = new Id("9");

        for (int i = 0; i < repetitions; i++) {
            LOGGER.info("Repetition: " + (i + 1));

            for (var batch : batches) {
                //LOGGER.info("Batch: " + batch);

                message = "";

                InstanceCategory instance = null;
                for (final var mappingId : postgresqlMappingIds) {
                    instance = importMapping(instance, mappingId, postgresqlDatasourceId, batch * batchMultiplier);
                }

                exportMapping(instance, mongodbMappingId, mongodbDatasourceId);

                LOGGER.info("\n" + message);
            }
        }

        LOGGER.info("Finished");
    }

    private InstanceCategory importMapping(@Nullable InstanceCategory instance, Id mappingId, Id datasourceId, int records) throws Exception {
        final var datasourceEntity = datasourceRepository.find(datasourceId);
        final var mappingEntity = mappingRepository.find(mappingId);

        final var datasource = datasourceEntity.toDatasource();
        final var mapping = createMapping(mappingEntity, datasource);

        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasourceEntity).getPullWrapper();

        final var prevInstance = instance != null ? instance : new InstanceBuilder(mapping.category()).build();
        final var query = new KindNameQuery(mapping.kindName(), records, null);
        final var nextInstance = new DatabaseToInstance().input(mapping, prevInstance, pullWrapper, query).run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getCounterInfo(DatabaseToInstance.RECORDS_COUNTER)
            + ", " + Statistics.getIntervalInfo(DatabaseToInstance.MTC_INTERVAL)
            + ", " + Statistics.getIntervalInfo(DatabaseToInstance.RUN_INTERVAL);

        Statistics.reset();

        return nextInstance;
    }

    /**
     * @deprecated This doesn't work as intended anymore. The reason is that now, all mappings are processed at once. Therefore this method does the same thing (export all mappings) for each mapping.
     */
    private void exportMapping(InstanceCategory instance, Id mappingId, Id datasourceId) throws Exception {
        final var datasourceEntity = datasourceRepository.find(datasourceId);
        final var mappingEntity = mappingRepository.find(mappingId);

        final var datasource = datasourceEntity.toDatasource();
        final var mapping = createMapping(mappingEntity, datasource);

        final var control = wrapperService.getControlWrapper(datasourceEntity);
        final AbstractDDLWrapper ddlWrapper = control.getDDLWrapper();
        final AbstractDMLWrapper dmlWrapper = control.getDMLWrapper();
        final AbstractICWrapper icWrapper = control.getICWrapper();

        final var process = new InstanceToDatabase();
        process.input(List.of(mapping), instance, ddlWrapper, dmlWrapper, icWrapper);
        process.run();

        message += "#" + mapping.kindName()
            + ", " + Statistics.getCounterInfo(InstanceToDatabase.STATEMENTS_COUNTER)
            + ", " + Statistics.getIntervalInfo(InstanceToDatabase.CTM_INTERVAL)
            + ", " + Statistics.getIntervalInfo(InstanceToDatabase.RUN_INTERVAL);

        Statistics.reset();
    }

    private Mapping createMapping(MappingEntity entity, Datasource datasource) {
        final var categoryEntity = categoryRepository.find(entity.categoryId);
        final var category = categoryEntity.toSchemaCategory();

        return entity.toMapping(datasource, category);
    }

}
