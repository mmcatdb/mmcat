package cz.cuni.matfyz.server;

import static cz.cuni.matfyz.core.tests.TestDataUtils.addMorphism;
import static cz.cuni.matfyz.core.tests.TestDataUtils.addSchemaObject;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
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
public class EvolutionManagementTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionManagementTests.class);

    private TestData data;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private WrapperService wrapperService;

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
        Statistics.reset();
        //schema = data.createDefaultSchemaCategory();
        //order = schema.getObject(data.getOrderKey());
    }

    @Test
    public void joinMoveTest() throws Exception {
        //final int batchMultiplier = 1000;
        final int batchMultiplier = 10;
        final int repetitions = 5;
        final int[] batches = new int[] { 1, 2 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64 };
        //final int[] batches = new int[] { 48, 80, 96 };
        //final int[] batches = new int[] { 128 };
        final Id mongodbDatabaseId = new Id("4");

        Database database = databaseService.find(mongodbDatabaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();

        for (int i = 0; i < repetitions; i++) {
            LOGGER.info("Repetition: " + (i + 1));

            for (var batch : batches) {
                
                // Create mapping and schema
                Assertions.assertDoesNotThrow(() -> setMapping());
                final var schema = mapping.category();

                // Load data to instance
                Statistics.start(Interval.IMPORT_JOIN_MOVE);

                var process = new DatabaseToInstance();
                process.setLimit(batch * batchMultiplier);
                process.input(mapping, null, pullWrapper);

                var category = process.run().data;

                // JOIN
                Statistics.start(Interval.JOIN);
                join(schema, category);
                Statistics.end(Interval.JOIN);
                
                Statistics.start(Interval.MOVE);
                move(schema, category);
                Statistics.end(Interval.MOVE);
                Statistics.end(Interval.IMPORT_JOIN_MOVE);

                var message = "#" + Statistics.getInfo(Counter.JOIN_ROWS)
                    + ", " + Statistics.getInfo(Counter.MOVE_ROWS)
                    + ", " + Statistics.getInfo(Interval.JOIN)
                    + ", " + Statistics.getInfo(Interval.MOVE)
                    + ", " + Statistics.getInfo(Interval.IMPORT_JOIN_MOVE);

                LOGGER.info(message);
                Statistics.logInfo(Counter.PULLED_RECORDS);

                /*
                Statistics.logInfo(Interval.JOIN);
                Statistics.logInfo(Interval.MOVE);
                Statistics.logInfo(Interval.IMPORT_JOIN_MOVE);
                Statistics.logInfo(Counter.JOIN_ROWS);
                */
            }
        }

        //LOGGER.info(instance.toString());

        /*
        var finalMapping = data.createFinalMapping(schema);
        var instanceToDatabase = new InstanceToDatabase();
        instanceToDatabase.input(finalMapping, instance, ddlWrapper, dmlWrapper);
        var finalResult = instanceToDatabase.run();
        */
        //LOGGER.info(finalResult.data);
    }

    private void join(SchemaCategory schema, InstanceCategory category) {
        var fullAddress = addSchemaObject(schema, data.fullAddressKey, "fullAddress", ObjectIds.createValue());
        var address = schema.getObject(data.addressKey);
        var addressToFullAddressMorphism = addMorphism(schema, data.addressToFullAddress, address, fullAddress, Min.ONE);
        
        var fullAddressInstance = new InstanceObject(fullAddress);
        category.objects().put(fullAddressInstance.key(), fullAddressInstance);

        var addressInstance = category.getObject(data.addressKey);
        var addressToFullAddressMorphismInstance = createInstanceMorphism(category, addressToFullAddressMorphism);

        Statistics.set(Counter.JOIN_ROWS, addressInstance.allRowsToSet().size());
        for (var addressRow : addressInstance.allRowsToSet()) {
            // Get new value
            // TODO - tímto se vyhledává pouze v id daných řádků ... chtělo by to udělat funkci, která se automaticky podívá i do morfizmů
            // ideálně bychom měli tyto morfizmy uchovávat na řádcích
            var street = addressRow.getValue(data.addressToStreet);
            var city = addressRow.getValue(data.addressToCity);
            var fullAddressValue = joinFunction(street, city);

            // Create superId and row
            var superId = SuperIdWithValues.fromEmptySignature(fullAddressValue);
            InstanceObject.getOrCreateRowWithBaseMorphism(superId, addressRow, addressToFullAddressMorphismInstance);
        }
    }

    private void move(SchemaCategory schema, InstanceCategory category) {
        // Link order with full address
        var order = schema.getObject(data.orderKey);
        var fullAddress = schema.getObject(data.fullAddressKey);
        var orderToFullAddressMorphism = addMorphism(schema, data.orderToFullAddress, order, fullAddress, Min.ONE);

        var orderToFullAddressMorphismInstance = createInstanceMorphism(category, orderToFullAddressMorphism);

        Statistics.set(Counter.MOVE_ROWS, category.getObject(data.orderKey).allRowsToSet().size());

        var originalSignature = Signature.concatenate(data.orderToUser, data.userToAddress, data.addressToFullAddress);
        var morphismPath = category.getPath(originalSignature);
        
        for (var orderRow : category.getObject(data.orderKey).allRowsToSet()) {
            var optionalRow = orderRow.traverseThrough(morphismPath).stream().findFirst();
            var fullAddressRow = optionalRow.get();
            orderToFullAddressMorphismInstance.addMapping(new MappingRow(orderRow, fullAddressRow));
        }

        // Delete address to full address
        var addressToFullAddress = schema.getMorphism(data.addressToFullAddress);
        schema.deleteMorphism(addressToFullAddress);

        var addressToFullAddressInstance = category.getMorphism(addressToFullAddress);
        category.deleteMorphism(addressToFullAddressInstance);
    }

    private static InstanceMorphism createInstanceMorphism(InstanceCategory category, SchemaMorphism morphism) {
        var dom = category.getObject(morphism.dom());
        var cod = category.getObject(morphism.cod());

        var morphismInstance = new InstanceMorphism(morphism, dom, cod, category);
        category.morphisms().put(morphismInstance.signature(), morphismInstance);

        return morphismInstance;
    }

    private static String joinFunction(String street, String city) {
        return street + " --- " + city;
    }

    private Mapping mapping;

    private void setMapping() throws URISyntaxException {
        mapping = data.createInitialMapping();
    }

}
